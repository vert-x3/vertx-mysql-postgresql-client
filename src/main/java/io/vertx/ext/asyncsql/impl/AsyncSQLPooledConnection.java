package io.vertx.ext.asyncsql.impl;

import com.github.mauricio.async.db.Connection;
import com.github.mauricio.async.db.QueryResult;
import com.github.mauricio.async.db.pool.AsyncObjectPool;
import com.github.mauricio.async.db.pool.ConnectionPool;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import scala.collection.JavaConversions;
import scala.concurrent.ExecutionContext;
import scala.runtime.AbstractFunction1;
import scala.util.Try;

import java.util.List;

class AsyncSQLPooledConnection<C extends Connection> {

  protected final ConnectionPool<C> pool;
  protected final ExecutionContext executionContext;

  AsyncSQLPooledConnection(ConnectionPool<C> pool, ExecutionContext executionContext) {
    this.pool = pool;
    this.executionContext = executionContext;
  }

  public void take(Handler<AsyncResult<C>> handler) {
    pool.take()
      .onComplete(new AbstractFunction1<Try<C>, Void>() {
        @Override
        public Void apply(Try<C> v1) {
          if (v1.isSuccess()) {
            handler.handle(Future.succeededFuture(v1.get()));
          } else {
            handler.handle(Future.failedFuture(v1.failed().get()));
          }
          return null;
        }
      }, executionContext);
  }

  public void giveBack(C connection, Handler<AsyncResult<Void>> handler) {
    pool.giveBack(connection)
      .onComplete(new AbstractFunction1<Try<AsyncObjectPool<C>>, Void>() {
        @Override
        public Void apply(Try<AsyncObjectPool<C>> v1) {
          if (v1.isSuccess()) {
            handler.handle(Future.succeededFuture());
          } else {
            handler.handle(Future.failedFuture(v1.failed().get()));
          }
          return null;
        }
      }, executionContext);
  }

  public void sendStatementAndFulfill(final Connection conn, final String sql, final List<Object> params, final Handler<AsyncResult<Void>> handler) {
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

  public void sendStatement(final Connection conn, final String sql, final List<Object> params, final Handler<AsyncResult<QueryResult>> handler) {
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
