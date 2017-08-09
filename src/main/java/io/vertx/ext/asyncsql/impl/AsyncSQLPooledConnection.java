package io.vertx.ext.asyncsql.impl;

import com.github.mauricio.async.db.Connection;
import com.github.mauricio.async.db.QueryResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool;
import io.vertx.ext.sql.SQLConnection;
import scala.collection.JavaConversions;
import scala.concurrent.ExecutionContext;
import scala.runtime.AbstractFunction1;
import scala.util.Try;

import java.util.List;

abstract class AsyncSQLPooledConnection implements SQLConnection {

  private final AsyncConnectionPool pool;
  private final ExecutionContext executionContext;

  protected final Connection conn;

  AsyncSQLPooledConnection(AsyncConnectionPool pool, Connection connection, ExecutionContext executionContext) {
    this.pool = pool;
    this.conn = connection;
    this.executionContext = executionContext;
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    pool.giveBack(conn);
    handler.handle(Future.succeededFuture());
  }

  void disconnect(Handler<AsyncResult<Void>> handler) {
    conn.disconnect()
      .onComplete(new AbstractFunction1<Try<Connection>, Void>() {
        @Override
        public Void apply(Try<Connection> v1) {
          if (v1.isSuccess()) {
            handler.handle(Future.succeededFuture());
          } else {
            handler.handle(Future.failedFuture(v1.failed().get()));
          }
          return null;
        }
      }, executionContext);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C> C unwrap() {
    return (C) conn;
  }

  public void sendStatementAndFulfill(final String sql, final List<Object> params, final Handler<AsyncResult<Void>> handler) {
    final scala.concurrent.Future<QueryResult> statement;

    if (params == null) {
      statement = conn.sendQuery(sql);
    } else {
      statement = conn.sendPreparedStatement(sql, JavaConversions.asScalaBuffer(params).toList());
    }

    statement
      .onComplete(new AbstractFunction1<Try<QueryResult>, Void>() {
        @Override
        public Void apply(Try<QueryResult> v1) {
          if (v1.isSuccess()) {
            handler.handle(Future.succeededFuture());
          } else {
            handler.handle(Future.failedFuture(v1.failed().get()));
          }
          return null;
        }
      }, executionContext);
  }

  public void sendStatement(final String sql, final List<Object> params, final Handler<AsyncResult<QueryResult>> handler) {
    final scala.concurrent.Future<QueryResult> statement;

    if (params == null) {
      statement = conn.sendQuery(sql);
    } else {
      statement = conn.sendPreparedStatement(sql, JavaConversions.asScalaBuffer(params).toList());
    }

    statement
      .onComplete(new AbstractFunction1<Try<QueryResult>, Void>() {
        @Override
        public Void apply(Try<QueryResult> v1) {
          if (v1.isSuccess()) {
            handler.handle(Future.succeededFuture(v1.get()));
          } else {
            handler.handle(Future.failedFuture(v1.failed().get()));
          }
          return null;
        }
      }, executionContext);
  }
}
