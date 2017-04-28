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

import com.github.mauricio.async.db.Configuration;
import com.github.mauricio.async.db.Connection;
import com.github.mauricio.async.db.SSLConfiguration;
import com.github.mauricio.async.db.pool.AsyncObjectPool;
import com.github.mauricio.async.db.pool.ConnectionPool;
import com.github.mauricio.async.db.pool.ObjectFactory;
import com.github.mauricio.async.db.pool.PoolConfiguration;
import io.netty.buffer.PooledByteBufAllocator;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;
import scala.Option;
import scala.collection.Map$;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.runtime.AbstractFunction1;
import scala.util.Try;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * Base class for the SQL client.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public abstract class BaseSQLClient<C extends Connection> {

  protected final Vertx vertx;
  protected final ExecutionContext ec;

  private final ConnectionPool<C> pool;

  public BaseSQLClient(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.ec = VertxEventLoopExecutionContext.create(vertx);

    pool = new ConnectionPool<>(connectionFactory(config), new PoolConfiguration(
      config.getInteger("maxPoolSize", 10),
      config.getLong("maxIdle", 4L),
      config.getInteger("maxQueueSize", 10),
      config.getLong("validationInterval", 5000L)
      ), ec);
  }

  protected abstract ObjectFactory<C> connectionFactory(JsonObject config);

  protected abstract SQLConnection wrap(C conn, ConnectionPool<C> pool);

  public void getConnection(Handler<AsyncResult<SQLConnection>> handler) {
    final ExecutionContext ec = VertxEventLoopExecutionContext.create(vertx);

    pool.take()
      .onComplete(new AbstractFunction1<Try<C>, Void>() {
        @Override
        public Void apply(Try<C> v1) {
          if (v1.isSuccess()) {
            handler.handle(Future.succeededFuture(wrap(v1.get(), pool)));
          } else {
            handler.handle(Future.failedFuture(v1.failed().get()));
          }
          return null;
        }
      }, ec);
  }

  public void close(Handler<AsyncResult<Void>> handler) {
    pool.close()
      .onComplete(new AbstractFunction1<Try<AsyncObjectPool<C>>, Void>() {
        @Override
        public Void apply(Try<AsyncObjectPool<C>> v1) {
          if (v1.isSuccess()) {
            if (handler != null) {
              handler.handle(Future.succeededFuture());
            }
          } else {
            if (handler != null) {
              handler.handle(Future.failedFuture(v1.failed().get()));
            }
          }
          return null;
        }
      }, ec);
  }

  public void close() {
    close(null);
  }

  static Configuration getConfiguration(
    String defaultHost,
    int defaultPort,
    String defaultDatabase,
    String defaultUser,
    String defaultPassword,
    String defaultCharset,
    long defaultConnectTimeout,
    long defaultTestTimeout,
    JsonObject config) {

    String host = config.getString("host", defaultHost);
    int port = config.getInteger("port", defaultPort);
    String username = config.getString("username", defaultUser);
    String password = config.getString("password", defaultPassword);
    String database = config.getString("database", defaultDatabase);
    Charset charset = Charset.forName(config.getString("charset", defaultCharset));
    long connectTimeout = config.getLong("connectTimeout", defaultConnectTimeout);
    long testTimeout = config.getLong("testTimeout", defaultTestTimeout);
    Long queryTimeout = config.getLong("queryTimeout");
    Option<Duration> queryTimeoutOption = (queryTimeout == null) ?
      Option.empty() : Option.apply(Duration.apply(queryTimeout, TimeUnit.MILLISECONDS));

    return new Configuration(
      username,
      host,
      port,
      Option.apply(password),
      Option.apply(database),
      SSLConfiguration.apply(Map$.MODULE$.empty()),
      charset,
      16777216,
      PooledByteBufAllocator.DEFAULT,
      Duration.apply(connectTimeout, TimeUnit.MILLISECONDS),
      Duration.apply(testTimeout, TimeUnit.MILLISECONDS),
      queryTimeoutOption);
  }
}
