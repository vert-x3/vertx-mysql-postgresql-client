package io.vertx.ext.asyncsql.impl;

import com.github.mauricio.async.db.Connection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.asyncsql.helper.ScalaHelper;
import io.vertx.ext.asyncsql.helper.ScalaHelper$;
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

import scala.concurrent.ExecutionContext;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
public class AsyncSQLConnectionImpl implements SQLConnection {

  private boolean inTransaction = false;
  private boolean inAutoCommit = true;

  private final Connection connection;
  private final AsyncConnectionPool pool;

  public AsyncSQLConnectionImpl(Connection connection, AsyncConnectionPool pool, ExecutionContext executionContext) {
    this.connection = connection;
    this.pool = pool;
  }

  @Override
  public SQLConnection setAutoCommit(boolean autoCommit, Handler<AsyncResult<Void>> handler) {
    Future<Void> future;
    if (inTransaction && autoCommit) {
      inTransaction = false;
      future = ScalaHelper$.toVertxFuture(connection.sendQuery("COMMIT"));
    } else {
      future = Future.succeededFuture();
    }

    inAutoCommit = autoCommit;

    future.setHandler(handler);

    return this;
  }

  @Override
  public SQLConnection execute(String sql, Handler<AsyncResult<Void>> handler) {
    beginTransactionIfNeeded().flatMap((res) -> {
      connection.sendQuery(sql);
    }).onComplete(doneVoid(handler));

    return this;
  }

  @Override
  public SQLConnection query(String sql, Handler<AsyncResult<ResultSet>> handler) {
    beginTransactionIfNeeded().flatMap((res) -> {
      connection.sendQuery(sql);
    }).onComplete(done(handler, queryResultToUpdateResult));

    return this;
  }

  @Override
  public SQLConnection queryWithParams(String sql, JsonArray params, Handler<AsyncResult<ResultSet>> handler) {
    beginTransactionIfNeeded().flatMap((res) -> {
      connection.sendPreparedStatement(sql, params.getList());
    }).onComplete(done(handler, queryResultToUpdateResult));

    return this;
  }

  @Override
  public SQLConnection update(String sql, Handler<AsyncResult<UpdateResult>> handler) {
    return null;
  }

  @Override
  public SQLConnection updateWithParams(String sql, JsonArray jsonArray, Handler<AsyncResult<UpdateResult>> handler) {
    return null;
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {

  }

  @Override
  public SQLConnection commit(Handler<AsyncResult<Void>> handler) {
    return null;
  }

  @Override
  public SQLConnection rollback(Handler<AsyncResult<Void>> handler) {
    return null;
  }

  private final Function<QueryResult, UpdateResult> queryResultToUpdateResult = (QueryResult qr) -> {
    return new UpdateResult(qr.rowsAffected.toInt, qr.rows.map((rs) -> {
      new JsonArray(rs.columnNames.toList.asJava);
    }).getOrElse(new JsonArray()));
  };

  private scala.concurrent.Future<Void> beginTransactionIfNeeded() {
    if (!inAutoCommit && !inTransaction) {
      inTransaction = true;
      return connection.sendQuery("BEGIN");
    } else {
      return Future.successful();
    }
  }

  private SQLConnection endAndStartTransaction(String command, Handler<AsyncResult<Void>> handler) {
    if (inTransaction) {
      inTransaction = false;
      Future<Void> future = ScalaHelper.toVertxFuture(connection.sendQuery(command).flatMap((res) -> {
        connection.sendQuery("BEGIN");
      }).map((res) -> {
        inTransaction = true;
        return null;
      }));

      future.onComplete(doneVoid(handler));
    } else {
      handler.handle(Future.failedFuture("Not in a transaction currently"));
    }

    return this;
  }

  private <T> Function<scala.util.Try<T>, Void> doneVoid(Handler<AsyncResult<Void>> handler) {
    return (Try<T> theTry) -> {
      if (theTry.isSuccess()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(theTry.cause()));
      }
    };
  }

  private <T> Function<scala.util.Try<QueryResult>, T> done(Handler<AsyncResult<Void>> handler, Function<QueryResult, T> f) {
    return (Try<QueryResult> theTry) -> {
      if (theTry.isSuccess()) {
        handler.handle(Future.succeededFuture(f(theTry.success())));
      } else {
        handler.handle(Future.failedFuture(theTry.cause()));
      }
    };
  }
}
