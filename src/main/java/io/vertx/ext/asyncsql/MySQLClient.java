/*
 * Copyright 2014 Red Hat, Inc.
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

package io.vertx.ext.asyncsql;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.impl.AsyncSQLClientImpl;

/**
 *
 * Represents an asynchronous MySQL client
 *
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@VertxGen
public interface MySQLClient extends AsyncSQLClient {

  /**
   * Create a MySQL service
   *
   * @param vertx  the Vert.x instance
   * @param config  the config
   * @return the service
   */
  static AsyncSQLClient createMySqlService(Vertx vertx, JsonObject config) {
    return new AsyncSQLClientImpl(vertx, config, true);
  }


}
