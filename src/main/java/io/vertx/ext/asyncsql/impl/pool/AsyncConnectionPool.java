package io.vertx.ext.asyncsql.impl.pool;

import com.github.mauricio.async.db.Configuration;
import com.github.mauricio.async.db.Connection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AsyncConnectionPool {

  private final int maxPoolSize;

  private static final Logger logger = LoggerFactory.getLogger(AsyncConnectionPool.class);
  protected final Configuration configuration;
  protected final Vertx vertx;

  private int poolSize = 0;
  private final List<Connection> availableConnections = new ArrayList<>();
  private final List<Handler<AsyncResult<Connection>>> waiters = new ArrayList<>();

  public AsyncConnectionPool(Vertx vertx, int maxPoolSize, Configuration configuration) {
    this.vertx = vertx;
    this.maxPoolSize = maxPoolSize;
    this.configuration = configuration;
  }

  protected abstract Connection create();

  private synchronized void createConnection(Future<Connection> future) {
    poolSize += 1;
    try {
      Connection connection = create();
      future.complete(connection);
    } catch (Throwable e) {
      logger.info("creating a connection went wrong", e);
      poolSize -= 1;
      future.fail(e);
    }
  }

  private synchronized void waitForAvailableConnection(Handler<AsyncResult<Connection>> handler) {
    waiters.add(handler);
  }

  private synchronized void createOrWaitForAvailableConnection(Handler<AsyncResult<Connection>> handler) {
    if (poolSize < maxPoolSize) {
      Future<Connection> future = Future.future();
      future.setHandler(handler);
      createConnection(future);
    } else {
      waitForAvailableConnection(handler);
    }
  }

  public synchronized void take(Handler<AsyncResult<Connection>> handler) {
    if (availableConnections.isEmpty()) {
      createOrWaitForAvailableConnection(handler);
    } else {
      Connection connection = availableConnections.remove(0);
      if (connection.isConnected()) {
        handler.handle(Future.succeededFuture(connection));
      } else {
        poolSize -= 1;
        take(handler);
      }
    }
  }

  private synchronized void notifyWaitersAboutAvailableConnection() {
    if (!waiters.isEmpty()) {
      Handler<AsyncResult<Connection>> handler = waiters.remove(0);
      take(handler);
    }
  }

  public synchronized void giveBack(Connection connection) {
    if (connection.isConnected()) {
      availableConnections.add(connection);
    } else {
      poolSize -= 1;
    }
    notifyWaitersAboutAvailableConnection();
  }

  public synchronized void close() {
    availableConnections.forEach(Connection::disconnect);
  }

  public synchronized void close(Handler<AsyncResult<Void>> handler) {
    close();
    handler.handle(null);
  }

  public interface ExecuteWithConnection<ResultType> {
    ResultType execute(AsyncResult<Connection> connection);
  }

  public <T> void withConnection(ExecuteWithConnection<AsyncResult<T>> operation) {
    take(ar -> {
      if (ar.failed()) {
        logger.error("Cannot execute 'withConnection' block - cannot get a connection", ar.cause());
        operation.execute(Future.failedFuture(ar.cause()));
      } else {
        final Connection connection = ar.result();
        operation.execute(Future.succeededFuture(connection));
        giveBack(connection);
      }
    });
  }

  public <T> void withConnection(ExecuteWithConnection<AsyncResult<T>> operation, Connection connection) {
    operation.execute(Future.succeededFuture(connection));
  }

}