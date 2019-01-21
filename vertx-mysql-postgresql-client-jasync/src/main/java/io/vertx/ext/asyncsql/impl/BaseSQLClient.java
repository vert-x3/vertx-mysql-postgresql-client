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

import com.github.jasync.sql.db.Configuration;
import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.ConnectionPoolConfiguration;
import com.github.jasync.sql.db.SSLConfiguration;
import com.github.jasync.sql.db.pool.PoolConfiguration;
import io.netty.buffer.PooledByteBufAllocator;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool;
import io.vertx.ext.sql.SQLConnection;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Base class for the SQL client.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public abstract class BaseSQLClient {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());
  protected final Vertx vertx;

  protected final JsonObject globalConfig;
  private long testTimeout;

  public BaseSQLClient(Vertx vertx, JsonObject globalConfig) {
    this.vertx = vertx;
    this.globalConfig = globalConfig;
  }

  protected abstract AsyncConnectionPool pool();

  protected abstract SQLConnection createFromPool(Connection conn, AsyncConnectionPool pool, Vertx vertx);

  public void getConnection(Handler<AsyncResult<SQLConnection>> handler) {
    pool().take(ar -> {
      if (ar.succeeded()) {
        final AsyncConnectionPool pool = pool();
        handler.handle(Future.succeededFuture(createFromPool(ar.result(), pool, vertx)));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  public void close(Handler<AsyncResult<Void>> handler) {
    log.info("Stopping async SQL client " + this);
    pool().close(ar -> {
        if (ar.succeeded()) {
          if (handler != null) {
            handler.handle(Future.succeededFuture());
          }
        } else {
          if (handler != null) {
            handler.handle(Future.failedFuture(ar.cause()));
          }
        }
      }
    );
  }

  public void close() {
    close(null);
  }

  protected ConnectionPoolConfiguration getConnectionConfiguration(
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
    Map<String, String> sslConfig = buildSslConfig(config);

    log.info("Creating configuration for " + host + ":" + port);
    return new ConnectionPoolConfiguration(
      host,
      port,
      database,
      username,
      password,
      0 /*maxActiveConnections, unused*/,
      0 /*maxIdleTime, unused*/,
      0 /*maxPendingQueries, unused*/,
      0 /*connectionValidationInterval*/,
      connectTimeout,
      testTimeout,
      queryTimeout,
      vertx.nettyEventLoopGroup(),
      vertx.nettyEventLoopGroup(), /*executor: in non-blocking world, we should only have one event loop group*/
      new SSLConfiguration(sslConfig),
      charset);
  }

  private Map<String, String> buildSslConfig(JsonObject config) {
    Map<String, String> sslConfig = new HashMap<String, String>();
    if (config.getString("sslMode") != null) {
      sslConfig.put("sslmode", config.getString("sslMode"));
    }
    if (config.getString("sslRootCert") != null) {
      sslConfig.put("sslrootcert", config.getString("sslRootCert"));
    }
    return sslConfig;
  }

}
