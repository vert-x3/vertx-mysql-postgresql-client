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

import com.github.mauricio.async.db.pool.ConnectionPool;
import com.github.mauricio.async.db.pool.ObjectFactory;
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection;
import com.github.mauricio.async.db.postgresql.pool.PostgreSQLConnectionFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLConnection;
import scala.concurrent.ExecutionContext;

/**
 * Implementation of the {@link BaseSQLClient} for PostGreSQL.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PostgreSQLClientImpl extends BaseSQLClient<PostgreSQLConnection> {

  public PostgreSQLClientImpl(Vertx vertx, JsonObject config) {
    super(vertx, config);
  }

  @Override
  protected ObjectFactory<PostgreSQLConnection> connectionFactory(JsonObject config) {
    final ExecutionContext ec = VertxEventLoopExecutionContext.create(vertx);

    return new PostgreSQLConnectionFactory(
      getConfiguration(
        PostgreSQLClient.DEFAULT_HOST,
        PostgreSQLClient.DEFAULT_PORT,
        PostgreSQLClient.DEFAULT_DATABASE,
        PostgreSQLClient.DEFAULT_USER,
        PostgreSQLClient.DEFAULT_PASSWORD,
        PostgreSQLClient.DEFAULT_CHARSET,
        PostgreSQLClient.DEFAULT_CONNECT_TIMEOUT,
        PostgreSQLClient.DEFAULT_TEST_TIMEOUT,
        config),
      vertx.nettyEventLoopGroup().next(),
      ec
    );
  }

  @Override
  protected SQLConnection wrap(PostgreSQLConnection conn, ConnectionPool<PostgreSQLConnection> pool) {
    return new PostgreSQLConnectionImpl(vertx, conn, pool, ec);
  }
}
