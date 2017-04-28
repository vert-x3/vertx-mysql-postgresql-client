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
import com.github.mauricio.async.db.pool.AsyncObjectPool;
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
import scala.util.Try;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of {@link SQLConnection} using the {@link ConnectionPool}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
abstract class AsyncSQLConnectionImpl<C extends Connection> implements SQLConnection {

  private final Vertx vertx;
  private final C connection;
  private final ConnectionPool<C> pool;
  private final ExecutionContext executionContext;

  // inTransaction can only be true when in !inAutoCommit, on switch the value should be false
  // which means that the first statement to be executed should start a transaction
  private final AtomicBoolean inTransaction = new AtomicBoolean();
  // inAutoCommit true is the default, when set to false it means that the
  // user is controlling the transition isolation level of the connection.
  //
  // By default the isolation level is defined by the db engine and only
  // override when the transactionIsolationLevel is set in the options.
  private final AtomicBoolean inAutoCommit = new AtomicBoolean(true);

  protected TransactionIsolation transactionIsolation;
  protected SQLOptions options;

  AsyncSQLConnectionImpl(Vertx vertx, C connection, ConnectionPool<C> pool, ExecutionContext executionContext) {
    this.vertx = vertx;
    this.connection = connection;
    this.pool = pool;
    this.executionContext = executionContext;
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

  private void sendStatementAndFulfill(final String stmt, final Handler<AsyncResult<Void>> handler) {
    sendStatementAndFulfill(stmt, false, handler);
  }

  private void sendStatementAndFulfill(final String stmt, final boolean endTransaction, final Handler<AsyncResult<Void>> handler) {
    connection
      .sendQuery(stmt)
      .onComplete(new AbstractFunction1<Try<QueryResult>, Void>() {
        @Override
        public Void apply(Try<QueryResult> v1) {
          if (v1.isSuccess()) {
            if (endTransaction) {
              inTransaction.set(false);
            }
            handler.handle(Future.succeededFuture());
          } else {
            handler.handle(Future.failedFuture(v1.failed().get()));
          }
          return null;
        }
      }, executionContext);
  }

  /**
   * If the connection is in autoCommit mode and there is no transaction running then we start one
   */
  private void beginTransactionIfNeeded(Handler<AsyncResult<Void>> handler) {
    if (!inAutoCommit.get() && inTransaction.compareAndSet(false, true)) {
      sendStatementAndFulfill(getStartTransactionStatement(), handler);
    } else {
      handler.handle(Future.succeededFuture());
    }
  }

  @Override
  public SQLConnection setAutoCommit(boolean autoCommit, Handler<AsyncResult<Void>> handler) {
    // skip for same state
    if (inAutoCommit.get() != autoCommit) {
      if (inAutoCommit.compareAndSet(false, autoCommit)) {
        // switch to automatic mode
        if (inTransaction.get()) {
          // issue COMMIT if inTransaction
          return commit(handler);
        }
      } else if (inAutoCommit.compareAndSet(true, autoCommit)) {
        // switch to manual mode

        // reset the in transaction flag
        inTransaction.set(false);
      }
    }

    handler.handle(Future.succeededFuture());
    return this;
  }

  @Override
  public SQLConnection setTransactionIsolation(TransactionIsolation transactionIsolation, Handler<AsyncResult<Void>> handler) {
    this.transactionIsolation = transactionIsolation;

    String statement = this.transactionIsolation != null ? getSetIsolationLevelStatement() : null;

    if (statement != null) {
      sendStatementAndFulfill(statement, handler);
    } else {
      handler.handle(Future.succeededFuture());
    }

    return this;
  }

  @Override
  public SQLConnection getTransactionIsolation(Handler<AsyncResult<TransactionIsolation>> handler) {
    beginTransactionIfNeeded(v -> {
      connection
        .sendQuery(getGetIsolationLevelStatement())
        .onComplete(new AbstractFunction1<Try<QueryResult>, Void>() {
          @Override
          public Void apply(Try<QueryResult> v1) {
            if (v1.isSuccess()) {
              final Option<com.github.mauricio.async.db.ResultSet> rows = v1.get().rows();
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
            } else {
              handler.handle(Future.failedFuture(v1.failed().get()));
            }
            return null;
          }
        }, executionContext);
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
    beginTransactionIfNeeded(v -> runStatement(jdbcToSqlCall(sql), null, handleAsyncQueryResultToResultSet(handler)));
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
      final long timerId = vertx.setTimer(options.getQueryTimeout(), t -> {
        handler.handle(Future.failedFuture("Query timeout"));
      });

      final scala.concurrent.Future<QueryResult> statement;

      if (params == null) {
        statement = connection.sendQuery(sql);
      } else {
        statement = connection.sendPreparedStatement(sql, ScalaUtils.toScalaList(params));
      }

      statement
        .onComplete(ScalaUtils.toFunction1(result -> {
          // cancel the running timer
          vertx.cancelTimer(timerId);
          // proceed
          handler.handle(result);
        }), executionContext);
    } else {

      (params != null ? connection.sendPreparedStatement(sql, ScalaUtils.toScalaList(params)) : connection.sendQuery(sql))
        .onComplete(ScalaUtils.toFunction1(handler), executionContext);
    }
  }


  @Override
  public SQLConnection execute(String sql, Handler<AsyncResult<Void>> handler) {
    beginTransactionIfNeeded(v -> runStatement(sql, null, ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    }));

    return this;
  }

  @Override
  public SQLConnection query(String sql, Handler<AsyncResult<ResultSet>> handler) {
    beginTransactionIfNeeded(v -> runStatement(sql, null, handleAsyncQueryResultToResultSet(handler)));
    return this;
  }

  @Override
  public SQLConnection queryStream(String sql, Handler<AsyncResult<SQLRowStream>> handler) {
    beginTransactionIfNeeded(v -> runStatement(sql, null, handleAsyncQueryResultToRowStream(handler)));
    return this;
  }

  @Override
  public SQLConnection queryWithParams(String sql, JsonArray params, Handler<AsyncResult<ResultSet>> handler) {
    beginTransactionIfNeeded(v -> runStatement(sql, params.getList(), handleAsyncQueryResultToResultSet(handler)));
    return this;
  }

  @Override
  public SQLConnection queryStreamWithParams(String sql, JsonArray params, Handler<AsyncResult<SQLRowStream>> handler) {
    beginTransactionIfNeeded(v -> runStatement(sql, params.getList(), handleAsyncQueryResultToRowStream(handler)));
    return this;
  }

  @Override
  public SQLConnection update(String sql, Handler<AsyncResult<UpdateResult>> handler) {
    beginTransactionIfNeeded(v -> runStatement(sql, null, handleAsyncUpdateResultToResultSet(handler)));
    return this;
  }

  @Override
  public SQLConnection updateWithParams(String sql, JsonArray params, Handler<AsyncResult<UpdateResult>> handler) {
    beginTransactionIfNeeded(v -> runStatement(sql, params.getList(), handleAsyncUpdateResultToResultSet(handler)));
    return this;
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    // recreate the commit on close behavior common to most JDBC drivers
    if (inTransaction.get()) {
      commit(commit -> {
        // reset the state to auto commit
        inAutoCommit.set(true);
        // give it back to the pool
        pool.giveBack(connection)
          .onComplete(new AbstractFunction1<Try<AsyncObjectPool<C>>, Void>() {
            @Override
            public Void apply(Try<AsyncObjectPool<C>> v1) {
              if (v1.isSuccess()) {
                // notify
                handler.handle(commit);
              } else {
                handler.handle(Future.failedFuture(v1.failed().get()));
              }
              return null;
            }
          }, executionContext);
      });
    } else {
      // reset the state to auto commit
      inAutoCommit.set(true);
      // give it back to the pool
      pool.giveBack(connection)
        .onComplete(new AbstractFunction1<Try<AsyncObjectPool<C>>, Void>() {
          @Override
          public Void apply(Try<AsyncObjectPool<C>> v1) {
            if (v1.isSuccess()) {
              // notify
              handler.handle(Future.succeededFuture());
            } else {
              handler.handle(Future.failedFuture(v1.failed().get()));
            }
            return null;
          }
        }, executionContext);
    }
  }

  @Override
  public void close() {
    close((ar) -> {
      // Do nothing by default.
    });
  }

  @Override
  public SQLConnection commit(Handler<AsyncResult<Void>> handler) {
    if (inTransaction.get()) {
      sendStatementAndFulfill("COMMIT", true, handler);
    } else {
      handler.handle(Future.failedFuture(new IllegalStateException("Not in transaction currently")));
    }
    return this;
  }

  @Override
  public SQLConnection rollback(Handler<AsyncResult<Void>> handler) {
    if (inTransaction.get()) {
      sendStatementAndFulfill("ROLLBACK", true, handler);
    } else {
      handler.handle(Future.failedFuture(new IllegalStateException("Not in transaction currently")));
    }
    return this;
  }

  @Override
  public SQLConnection batch(List<String> sqlStatements, Handler<AsyncResult<List<Integer>>> handler) {
    beginTransactionIfNeeded(v -> {
      if (options != null && options.getQueryTimeout() > 0) {
        final long timerId = vertx.setTimer(options.getQueryTimeout(), t -> {
          handler.handle(Future.failedFuture("Query timeout"));
        });

        batch(0, timerId, new ArrayList<>(sqlStatements.size()), sqlStatements, handler);
      } else {
        batch(0, -1, new ArrayList<>(sqlStatements.size()), sqlStatements, handler);
      }
    });

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
    connection
      .sendQuery(sqlStatements.get(idx))
      .onComplete(ScalaUtils.toFunction1(res -> {
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
      }), executionContext);
  }

  @Override
  public SQLConnection batchWithParams(String sqlStatement, List<JsonArray> args, Handler<AsyncResult<List<Integer>>> handler) {
    beginTransactionIfNeeded(v -> {
      if (options != null && options.getQueryTimeout() > 0) {
        final long timerId = vertx.setTimer(options.getQueryTimeout(), t -> {
          handler.handle(Future.failedFuture("Query timeout"));
        });

        batch(0, timerId, new ArrayList<>(args.size()), sqlStatement, args, handler);
      } else {
        batch(0, -1, new ArrayList<>(args.size()), sqlStatement, args, handler);
      }
    });

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
    connection
      .sendPreparedStatement(statement, ScalaUtils.toScalaList(params))
      .onComplete(ScalaUtils.toFunction1(res -> {
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
      }), executionContext);
  }

  @Override
  public SQLConnection batchCallableWithParams(String sqlStatement, List<JsonArray> inArgs, List<JsonArray> outArgs, Handler<AsyncResult<List<Integer>>> handler) {
    // No idea how to implement this
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C> C unwrap() {
    return (C) connection;
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
