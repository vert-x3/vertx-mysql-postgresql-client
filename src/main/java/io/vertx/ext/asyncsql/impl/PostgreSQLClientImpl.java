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
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool;
import io.vertx.ext.asyncsql.impl.pool.PostgresqlAsyncConnectionPool;
import io.vertx.ext.sql.SQLConnection;
import scala.concurrent.ExecutionContext;

/**
 * Implementation of the {@link BaseSQLClient} for PostGreSQL.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PostgreSQLClientImpl extends BaseSQLClient {

  private final PostgresqlAsyncConnectionPool pool;

  public PostgreSQLClientImpl(Vertx vertx, JsonObject globalConfig) {
    super(vertx, globalConfig);
    pool = new PostgresqlAsyncConnectionPool(vertx, globalConfig, getConnectionConfiguration(
        PostgreSQLClient.DEFAULT_HOST,
        PostgreSQLClient.DEFAULT_PORT,
        PostgreSQLClient.DEFAULT_DATABASE,
        PostgreSQLClient.DEFAULT_USER,
        PostgreSQLClient.DEFAULT_PASSWORD,
        PostgreSQLClient.DEFAULT_CHARSET,
        PostgreSQLClient.DEFAULT_CONNECT_TIMEOUT,
        PostgreSQLClient.DEFAULT_TEST_TIMEOUT,
        globalConfig));
  }

  @Override
  protected AsyncConnectionPool pool() {
    return pool;
  }

  @Override
  protected SQLConnection createFromPool(Connection conn, AsyncConnectionPool pool, ExecutionContext ec) {
    return new PostgreSQLConnectionImpl(conn, pool, ec);
  }
}
