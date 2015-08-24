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
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection;
import com.github.mauricio.async.db.postgresql.column.PostgreSQLColumnDecoderRegistry;
import com.github.mauricio.async.db.postgresql.column.PostgreSQLColumnEncoderRegistry;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

/**
 * Implementation of the {@link AsyncConnectionPool} for PostGresSQL.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PostgresqlAsyncConnectionPool extends AsyncConnectionPool {

  public PostgresqlAsyncConnectionPool(Vertx vertx, int maxPoolSize, Configuration configuration) {
    super(vertx, maxPoolSize, configuration);
  }

  @Override
  protected Connection create() {
    return new PostgreSQLConnection(
        configuration,
        PostgreSQLColumnEncoderRegistry.Instance(),
        PostgreSQLColumnDecoderRegistry.Instance(),
        vertx.nettyEventLoopGroup().next(),
        executionContext);
  }

}
