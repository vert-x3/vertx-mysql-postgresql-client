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
import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.impl.ScalaUtils;
import io.vertx.ext.asyncsql.impl.VertxEventLoopExecutionContext;
import scala.concurrent.ExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a pool of connection.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public abstract class AsyncConnectionPool {

  private final int maxPoolSize;

  private static final Logger logger = LoggerFactory.getLogger(AsyncConnectionPool.class);
  protected final Configuration configuration;
  protected final Vertx vertx;
  protected final ExecutionContext executionContext;

  private int poolSize = 0;
  private final List<Connection> availableConnections = new ArrayList<>();
  private final List<Handler<AsyncResult<Connection>>> waiters = new ArrayList<>();

  public AsyncConnectionPool(Vertx vertx, Context context, int maxPoolSize, Configuration configuration) {
    this.vertx = vertx;
    this.maxPoolSize = maxPoolSize;
    this.configuration = configuration;
    this.executionContext = VertxEventLoopExecutionContext.create(context);
  }

  protected abstract Connection create();

  private synchronized void createConnection(Handler<AsyncResult<Connection>> handler) {
    poolSize += 1;
    try {
      Connection connection = create();
      connection
          .connect().onComplete(ScalaUtils.toFunction1(handler), executionContext);
    } catch (Throwable e) {
      logger.info("creating a connection went wrong", e);
      poolSize -= 1;
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
    if (handler != null) {
      handler.handle(Future.succeededFuture());
    }
  }

  public ExecutionContext executionContext() {
    return executionContext;
  }
}
