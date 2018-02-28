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

package io.vertx.ext.asyncsql.impl.pool;

import com.github.mauricio.async.db.Configuration;
import com.github.mauricio.async.db.Connection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.impl.ScalaUtils;
import io.vertx.ext.asyncsql.impl.VertxEventLoopExecutionContext;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manages a pool of connection.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public abstract class AsyncConnectionPool {

  public static final int DEFAULT_MAX_POOL_SIZE = 10;
  public static final int DEFAULT_MAX_CONNECTION_RETRIES = 0;       // No connection retries by default
  public static final int DEFAULT_CONNECTION_RETRY_DELAY = 5_000;   // 5 seconds between retries by default

  private static final Logger logger = LoggerFactory.getLogger(AsyncConnectionPool.class);

  private final int maxPoolSize;
  private final int maxConnectionRetries;
  private final int connectionRetryDelay;

  protected final Configuration connectionConfig;
  protected final Vertx vertx;

  private int poolSize = 0;
  private final Deque<Connection> availableConnections = new ArrayDeque<>();
  private final Deque<Handler<AsyncResult<Connection>>> waiters = new ArrayDeque<>();

  public AsyncConnectionPool(Vertx vertx, JsonObject globalConfig, Configuration connectionConfig) {
    this.vertx = vertx;
    this.maxPoolSize = globalConfig.getInteger("maxPoolSize", DEFAULT_MAX_POOL_SIZE);
    this.maxConnectionRetries = globalConfig.getInteger("maxConnectionRetries", DEFAULT_MAX_CONNECTION_RETRIES);
    this.connectionRetryDelay = globalConfig.getInteger("connectionRetryDelay", DEFAULT_CONNECTION_RETRY_DELAY);
    this.connectionConfig = connectionConfig;
  }

  protected abstract Connection create();

  private synchronized void createConnection(Handler<AsyncResult<Connection>> handler) {
    poolSize += 1;
    createAndConnect(new Handler<AsyncResult<Connection>>() {
      int retries = 0;
      @Override
      public void handle(AsyncResult<Connection> connectionResult) {
        if (connectionResult.succeeded()) {
          handler.handle(connectionResult);
        } else if (maxConnectionRetries < 0 || retries < maxConnectionRetries) {
          retries++;
          logger.debug("Error creating connection. Waiting " + connectionRetryDelay + " ms for retry " +
            retries + (maxConnectionRetries >= 0 ? " of " + maxConnectionRetries : ""));
          vertx.setTimer(connectionRetryDelay, timerId ->
            createAndConnect(this) // Try to connect again using this handler
          );
        } else {
          poolSize -= 1;
          notifyWaitersAboutAvailableConnection();
          handler.handle(connectionResult);
        }
      }
    });
  }

  private synchronized void createAndConnect(Handler<AsyncResult<Connection>> handler) {
    try {
      create()
        .connect()
        .onComplete(ScalaUtils.toFunction1(handler), VertxEventLoopExecutionContext.create(vertx));
    } catch (Throwable e) {
      logger.info("creating a connection went wrong", e);
      handler.handle(Future.failedFuture(e));
    }
  }

  private synchronized void waitForAvailableConnection(Handler<AsyncResult<Connection>> handler) {
    waiters.add(handler);
  }

  private synchronized void createOrWaitForAvailableConnection(Handler<AsyncResult<Connection>> handler) {
    if (poolSize < maxPoolSize) {
      createConnection(handler);
    } else {
      waitForAvailableConnection(handler);
    }
  }

  public synchronized void take(Handler<AsyncResult<Connection>> handler) {
    Connection connection = availableConnections.poll();
    if (connection == null) {
      createOrWaitForAvailableConnection(handler);
    } else {
      if (connection.isConnected()) {
        handler.handle(Future.succeededFuture(connection));
      } else {
        poolSize -= 1;
        take(handler);
      }
    }
  }

  private synchronized void notifyWaitersAboutAvailableConnection() {
    Handler<AsyncResult<Connection>> handler = waiters.poll();
    if (handler != null) {
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
    if (handler != null) {
      handler.handle(Future.succeededFuture());
    }
  }
}
