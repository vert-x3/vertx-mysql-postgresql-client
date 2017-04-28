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

import com.github.mauricio.async.db.mysql.MySQLConnection;
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory;
import com.github.mauricio.async.db.pool.ConnectionPool;
import com.github.mauricio.async.db.pool.ObjectFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLConnection;
import scala.concurrent.ExecutionContext;

/**
 * Implementation of the {@link BaseSQLClient} for MYSQL.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MYSQLClientImpl extends BaseSQLClient<MySQLConnection> {

  public MYSQLClientImpl(Vertx vertx, JsonObject config) {
    super(vertx, config);
  }

  @Override
  protected ObjectFactory<MySQLConnection> connectionFactory(JsonObject config) {
    return new MySQLConnectionFactory(getConfiguration(
      MySQLClient.DEFAULT_HOST,
      MySQLClient.DEFAULT_PORT,
      MySQLClient.DEFAULT_DATABASE,
      MySQLClient.DEFAULT_USER,
      MySQLClient.DEFAULT_PASSWORD,
      MySQLClient.DEFAULT_CHARSET,
      MySQLClient.DEFAULT_CONNECT_TIMEOUT,
      MySQLClient.DEFAULT_TEST_TIMEOUT,
      config));
  }

  @Override
  protected SQLConnection wrap(MySQLConnection conn, ConnectionPool<MySQLConnection> pool) {
    return new MySQLConnectionImpl(vertx, conn, pool, ec);
  }
}
