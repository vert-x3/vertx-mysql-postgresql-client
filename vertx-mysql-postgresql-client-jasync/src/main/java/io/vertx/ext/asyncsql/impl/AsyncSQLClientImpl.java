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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
public class AsyncSQLClientImpl implements AsyncSQLClient {

  private final BaseSQLClient baseClient;

  public AsyncSQLClientImpl(Vertx vertx, JsonObject config, boolean mysql) {
    if (mysql) {
      baseClient = new MYSQLClientImpl(vertx, config);
    } else {
      baseClient = new PostgreSQLClientImpl(vertx, config);
    }
  }

  @Override
  public void close() {
    baseClient.close(null);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    baseClient.close(completionHandler);
  }

  @Override
  public SQLClient getConnection(Handler<AsyncResult<SQLConnection>> handler) {
    baseClient.getConnection(handler);
    return this;
  }

}
