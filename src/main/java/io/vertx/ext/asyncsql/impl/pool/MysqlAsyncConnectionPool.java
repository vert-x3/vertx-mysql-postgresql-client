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
import com.github.mauricio.async.db.mysql.MySQLConnection;
import com.github.mauricio.async.db.mysql.util.CharsetMapper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.impl.VertxEventLoopExecutionContext;

/**
 * Implementation of the {@link AsyncConnectionPool} for MySQL.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MysqlAsyncConnectionPool extends AsyncConnectionPool {

  public MysqlAsyncConnectionPool(Vertx vertx, JsonObject globalConfig, Configuration connectionConfig) {
    super(vertx, globalConfig, connectionConfig);
  }

  @Override
  protected Connection create() {
    return new MySQLConnection(connectionConfig, CharsetMapper.Instance(),
        vertx.nettyEventLoopGroup().next(),
        VertxEventLoopExecutionContext.create(vertx)
    );
  }

}
