/*
 *  Copyright 2015 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.asyncsql.impl;

import com.github.mauricio.async.db.Connection;
import com.github.mauricio.async.db.QueryResult;
import com.github.mauricio.async.db.RowData;
import com.github.mauricio.async.db.pool.ConnectionPool;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.*;
import scala.Option;
import scala.concurrent.ExecutionContext;
import scala.runtime.AbstractFunction1;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of {@link SQLConnection} using the {@link ConnectionPool}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
abstract class AsyncSQLConnectionImpl<C extends Connection> extends AsyncSQLPooledConnection<C> implements SQLConnection {

  private final Vertx vertx;

  protected TransactionIsolation transactionIsolation;
  protected SQLOptions options;

  // By default this will be null, this means that we're in autoCommit mode.
  // The reason behind this is that the underlying driver will automatically pool each statement for us.
  //
  // When this variable is not null we're not in autoCommit and will probably want to run a batch of
  // statements or a transaction.
  private volatile C connection;

  AsyncSQLConnectionImpl(Vertx vertx, ConnectionPool<C> pool, ExecutionContext executionContext) {
    super(pool, executionContext);

    this.vertx = vertx;
  }

  private boolean isAutoCommit() {
    return connection == null;
  }

  private Connection getConnection() {
    if (isAutoCommit()) {
      return pool;
    }

    return connection;
  }

  /**
   * Returns a vendor specific start transaction statement
   */
  protected abstract String getStartTransactionStatement();

  /**
   * Returns a vendor specific start transaction statement
   */
  protected abstract String getSetIsolationLevelStatement();

  /**
   * Returns a vendor specific start transaction statement
   */
  protected abstract String getGetIsolationLevelStatement();

  @Override
  public SQLConnection setAutoCommit(boolean autoCommit, Handler<AsyncResult<Void>> handler) {
    // skip for same state
    if (isAutoCommit() != autoCommit) {
      // auto commit is false
      if (connection != null) {
        // issue COMMIT as per JDBC API
        sendStatementAndFulfill(connection, "COMMIT", null, commit -> {
          if (commit.succeeded()) {
            // give back the connection to the pool
            giveBack(connection, giveBack -> {
              // switch to auto commit
              if (giveBack.succeeded()) {
                connection = null;
              }
              handler.handle(giveBack);
            });
          } else {
            handler.handle(commit);
          }
        });

        return this;
      } else {
        // switch to manual mode
        take(take -> {
          if (take.succeeded()) {
            connection = take.result();
            String isolationLevelStatement = this.transactionIsolation != null ? getSetIsolationLevelStatement() : null;
            // check if we need to switch the default isolation level
            if (isolationLevelStatement != null) {
              sendStatementAndFulfill(connection, isolationLevelStatement, null, isolationLevel -> {
                if (isolationLevel.succeeded()) {
                  // we can now start the transaction
                  sendStatementAndFulfill(connection, getStartTransactionStatement(), null, handler);
                } else {
                  handler.handle(Future.failedFuture(isolationLevel.cause()));
                }
              });
            } else {
              // start transaction
              sendStatementAndFulfill(connection, getStartTransactionStatement(), null, handler);
            }
          } else {
            handler.handle(Future.failedFuture(take.cause()));
          }
        });

        return this;
      }
    }

    handler.handle(Future.succeededFuture());
    return this;
  }

  @Override
  public SQLConnection setTransactionIsolation(TransactionIsolation transactionIsolation, Handler<AsyncResult<Void>> handler) {
    if (isAutoCommit()) {
      this.transactionIsolation = transactionIsolation;
      handler.handle(Future.succeededFuture());
    } else {
      handler.handle(Future.failedFuture("Cannot change isolation level in a transaction"));
    }

    return this;
  }

  @Override
  public SQLConnection getTransactionIsolation(Handler<AsyncResult<TransactionIsolation>> handler) {
    sendStatement(getConnection(), getGetIsolationLevelStatement(), null, res -> {
      if (res.succeeded()) {
        final Option<com.github.mauricio.async.db.ResultSet> rows = res.result().rows();
        if (!rows.isDefined()) {
          handler.handle(Future.failedFuture("no results"));
        } else {
          final Option<RowData> row = rows.get().headOption();
          if (!row.isDefined()) {
            handler.handle(Future.failedFuture("no results"));
          } else {
            try {
              TransactionIsolation isolation = TransactionIsolation.from((String) row.get().apply(0));
              handler.handle(Future.succeededFuture(isolation));
            } catch (RuntimeException e) {
              handler.handle(Future.failedFuture(e));
            }
          }
        }
      }
    });

    return this;
  }

  private static final Pattern CALL = Pattern.compile("=?\\s*call\\s+", Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);

  private String jdbcToSqlCall(String sql) {
    // transform the jdbc like statement into a sql statement

    // remove the enclosing brackets
    int start = sql.indexOf('{');
    if (start != -1) {
      sql = sql.substring(start + 1);
    }
    int end = sql.lastIndexOf('}');
    if (end != -1) {
      sql = sql.substring(0, end);
    }

    // locate "call"
    Matcher matcher = CALL.matcher(sql);
    if (matcher.find()) {
      // replace will SELECT
      sql = matcher.replaceFirst("SELECT ");
    }

    // as of this point we should have some sql statement
    return sql;
  }

  @Override
  public SQLConnection call(String sql, Handler<AsyncResult<ResultSet>> handler) {
    runStatement(jdbcToSqlCall(sql), null, handleAsyncQueryResultToResultSet(handler));
    return this;
  }

  @Override
  public SQLConnection callWithParams(String sql, JsonArray params, JsonArray outputs, Handler<AsyncResult<ResultSet>> resultHandler) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public SQLConnection setOptions(SQLOptions options) {
    this.options = options;
    return this;
  }

  private void runStatement(final String sql, final List<Object> params, final Handler<AsyncResult<QueryResult>> handler) {

    if (options != null && options.getQueryTimeout() > 0) {
      final long timerId = vertx.setTimer(options.getQueryTimeout(), t -> handler.handle(Future.failedFuture("Query timeout")));

      sendStatement(getConnection(), sql, params, statement -> {
        // cancel the running timer
        vertx.cancelTimer(timerId);
        // proceed
        handler.handle(statement);
      });

    } else {
      sendStatement(getConnection(), sql, params, handler);
    }
  }


  @Override
  public SQLConnection execute(String sql, Handler<AsyncResult<Void>> handler) {
    runStatement(sql, null, ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });

    return this;
  }

  @Override
  public SQLConnection query(String sql, Handler<AsyncResult<ResultSet>> handler) {
    runStatement(sql, null, handleAsyncQueryResultToResultSet(handler));
    return this;
  }

  @Override
  public SQLConnection queryStream(String sql, Handler<AsyncResult<SQLRowStream>> handler) {
    runStatement(sql, null, handleAsyncQueryResultToRowStream(handler));
    return this;
  }

  @Override
  public SQLConnection queryWithParams(String sql, JsonArray params, Handler<AsyncResult<ResultSet>> handler) {
    runStatement(sql, params.getList(), handleAsyncQueryResultToResultSet(handler));
    return this;
  }

  @Override
  public SQLConnection queryStreamWithParams(String sql, JsonArray params, Handler<AsyncResult<SQLRowStream>> handler) {
    runStatement(sql, params.getList(), handleAsyncQueryResultToRowStream(handler));
    return this;
  }

  @Override
  public SQLConnection update(String sql, Handler<AsyncResult<UpdateResult>> handler) {
    runStatement(sql, null, handleAsyncUpdateResultToResultSet(handler));
    return this;
  }

  @Override
  public SQLConnection updateWithParams(String sql, JsonArray params, Handler<AsyncResult<UpdateResult>> handler) {
    runStatement(sql, params.getList(), handleAsyncUpdateResultToResultSet(handler));
    return this;
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    // recreate the commit on close behavior common to most JDBC drivers
    setAutoCommit(true, handler);
  }

  @Override
  public void close() {
    close((ar) -> {
      // Do nothing by default.
    });
  }

  @Override
  public SQLConnection commit(Handler<AsyncResult<Void>> handler) {
    if (!isAutoCommit()) {
      sendStatementAndFulfill(connection, "COMMIT", null, handler);
    } else {
      handler.handle(Future.failedFuture(new IllegalStateException("Not in transaction currently")));
    }
    return this;
  }

  @Override
  public SQLConnection rollback(Handler<AsyncResult<Void>> handler) {
    if (!isAutoCommit()) {
      sendStatementAndFulfill(connection, "ROLLBACK", null, handler);
    } else {
      handler.handle(Future.failedFuture(new IllegalStateException("Not in transaction currently")));
    }
    return this;
  }

  @Override
  public SQLConnection batch(List<String> sqlStatements, Handler<AsyncResult<List<Integer>>> handler) {
    if (options != null && options.getQueryTimeout() > 0) {
      final long timerId = vertx.setTimer(options.getQueryTimeout(), t -> {
        handler.handle(Future.failedFuture("Query timeout"));
      });

      batch(0, timerId, new ArrayList<>(sqlStatements.size()), sqlStatements, handler);
    } else {
      batch(0, -1, new ArrayList<>(sqlStatements.size()), sqlStatements, handler);
    }

    return this;
  }

  private void batch(final int idx, final long timerId, final List<Integer> results, List<String> sqlStatements, Handler<AsyncResult<List<Integer>>> handler) {
    if (idx == sqlStatements.size()) {
      // stop condition
      if (timerId != -1) {
        // cancel the running timer
        vertx.cancelTimer(timerId);
      }
      handler.handle(Future.succeededFuture(results));
      return;
    }

    // run the next statement
    sendStatement(getConnection(), sqlStatements.get(idx), null, res -> {
      if (res.succeeded()) {
        results.add(idx, (int) res.result().rowsAffected());
        // next
        batch(idx + 1, timerId, results, sqlStatements, handler);
      } else {
        if (timerId != -1) {
          // cancel the running timer
          vertx.cancelTimer(timerId);
        }
        handler.handle(Future.failedFuture(res.cause()));
      }
    });
  }

  @Override
  public SQLConnection batchWithParams(String sqlStatement, List<JsonArray> args, Handler<AsyncResult<List<Integer>>> handler) {
    if (options != null && options.getQueryTimeout() > 0) {
      final long timerId = vertx.setTimer(options.getQueryTimeout(), t -> {
        handler.handle(Future.failedFuture("Query timeout"));
      });

      batch(0, timerId, new ArrayList<>(args.size()), sqlStatement, args, handler);
    } else {
      batch(0, -1, new ArrayList<>(args.size()), sqlStatement, args, handler);
    }

    return this;
  }

  private void batch(final int idx, final long timerId, final List<Integer> results, String statement, List<JsonArray> args, Handler<AsyncResult<List<Integer>>> handler) {
    if (idx == args.size()) {
      // stop condition
      if (timerId != -1) {
        // cancel the running timer
        vertx.cancelTimer(timerId);
      }
      handler.handle(Future.succeededFuture(results));
      return;
    }

    List<Object> params = args.get(idx).getList();
    // run the next statement
    sendStatement(getConnection(), statement, params, res -> {
      if (res.succeeded()) {
        results.add(idx, (int) res.result().rowsAffected());
        // next
        batch(idx + 1, timerId, results, statement, args, handler);
      } else {
        if (timerId != -1) {
          // cancel the running timer
          vertx.cancelTimer(timerId);
        }
        handler.handle(Future.failedFuture(res.cause()));
      }
    });
  }

  @Override
  public SQLConnection batchCallableWithParams(String sqlStatement, List<JsonArray> inArgs, List<JsonArray> outArgs, Handler<AsyncResult<List<Integer>>> handler) {
    // No idea how to implement this
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C> C unwrap() {
    return (C) getConnection();
  }

  private Handler<AsyncResult<QueryResult>> handleAsyncQueryResultToResultSet(Handler<AsyncResult<ResultSet>> handler) {
    return ar -> {
      if (ar.succeeded()) {
        try {
          handler.handle(Future.succeededFuture(queryResultToResultSet(ar.result())));
        } catch (Throwable e) {
          handler.handle(Future.failedFuture(e));
        }
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    };
  }

  private Handler<AsyncResult<QueryResult>> handleAsyncQueryResultToRowStream(Handler<AsyncResult<SQLRowStream>> handler) {
    return ar -> {
      if (ar.succeeded()) {
        try {
          handler.handle(Future.succeededFuture(new AsyncSQLRowStream(ar.result())));
        } catch (Throwable e) {
          handler.handle(Future.failedFuture(e));
        }
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    };
  }

  private ResultSet queryResultToResultSet(QueryResult qr) {
    final Option<com.github.mauricio.async.db.ResultSet> rows = qr.rows();
    if (!rows.isDefined()) {
      return new ResultSet(Collections.emptyList(), Collections.emptyList(), null);
    } else {
      final List<String> names = ScalaUtils.toJavaList(rows.get().columnNames().toList());
      final List<JsonArray> arrays = rowDataSeqToJsonArray(rows.get());
      return new ResultSet(names, arrays, null);
    }
  }

  private Handler<AsyncResult<QueryResult>> handleAsyncUpdateResultToResultSet(Handler<AsyncResult<UpdateResult>> handler) {
    return ar -> {
      if (ar.succeeded()) {
        try {
          handler.handle(Future.succeededFuture(queryResultToUpdateResult(ar.result())));
        } catch (Throwable e) {
          handler.handle(Future.failedFuture(e));
        }
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    };
  }

  protected abstract UpdateResult queryResultToUpdateResult(QueryResult qr);

  private List<JsonArray> rowDataSeqToJsonArray(com.github.mauricio.async.db.ResultSet set) {
    List<JsonArray> list = new ArrayList<>();
    set.foreach(new AbstractFunction1<RowData, Void>() {
      @Override
      public Void apply(RowData row) {
        list.add(ScalaUtils.rowToJsonArray(row));
        return null;
      }
    });
    return list;
  }
}
