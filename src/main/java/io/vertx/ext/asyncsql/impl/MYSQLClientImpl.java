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

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool;
import io.vertx.ext.asyncsql.impl.pool.MysqlAsyncConnectionPool;

/**
 * Implementation of the {@link BaseSQLClient} for MYSQL.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MYSQLClientImpl extends BaseSQLClient {

  private final MysqlAsyncConnectionPool pool;

  public MYSQLClientImpl(Vertx vertx, Context context,
                         JsonObject config) {
    super(vertx, config);
    pool = new MysqlAsyncConnectionPool(vertx, context, maxPoolSize, getConfiguration(
        MySQLClient.DEFAULT_HOST,
        MySQLClient.DEFAULT_PORT,
        MySQLClient.DEFAULT_DATABASE,
        MySQLClient.DEFAULT_USER,
        MySQLClient.DEFAULT_PASSWORD,
        config));
  }

  @Override
  protected AsyncConnectionPool pool() {
    return pool;
  }
}
