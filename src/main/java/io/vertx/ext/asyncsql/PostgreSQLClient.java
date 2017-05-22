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

package io.vertx.ext.asyncsql;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.impl.ClientHelper;
import io.vertx.ext.sql.SQLClient;

import java.util.UUID;

/**
 * Represents an PostgreSQL client
 *
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface PostgreSQLClient extends AsyncSQLClient {

  /**
   * The default name used for the PostGreSQL pool.
   */
  String DEFAULT_DS_NAME = "DEFAULT_POSTGRESQL_DS";

  /**
   * The default host.
   */
  String DEFAULT_HOST = "localhost";

  /**
   * The default port.
   */
  int DEFAULT_PORT = 5432;

  /**
   * The default database name.
   */
  String DEFAULT_DATABASE = "testdb";

  /**
   * The default database user.
   */
  String DEFAULT_USER = "vertx";

  /**
   * The default user password.
   */
  String DEFAULT_PASSWORD = "password";

  /**
   * The default charset.
   */
  String DEFAULT_CHARSET = "UTF-8";

  /**
   * The default timeout for connect.
   */
  long DEFAULT_CONNECT_TIMEOUT = 10000L;

  /**
   * The default timeout for tests.
   */
  long DEFAULT_TEST_TIMEOUT = 10000L;


  /**
   * Create a PostgreSQL client which maintains its own pool.
   *
   * @param vertx  the Vert.x instance
   * @param config the configuration
   * @return the client
   */
  static AsyncSQLClient createNonShared(Vertx vertx, JsonObject config) {
    return ClientHelper.getOrCreate(vertx, config, UUID.randomUUID().toString(), false);
  }

  /**
   * Create a PostgreSQL client which shares its pool with any other MySQL clients created with the same pool name.
   *
   * @param vertx    the Vert.x instance
   * @param config   the configuration
   * @param poolName the pool name
   * @return the client
   */
  static AsyncSQLClient createShared(Vertx vertx, JsonObject config, String poolName) {
    return ClientHelper.getOrCreate(vertx, config, poolName, false);
  }


  /**
   * Like {@link #createShared(io.vertx.core.Vertx, JsonObject, String)} but with the default pool name
   *
   * @param vertx  the Vert.x instance
   * @param config the configuration
   * @return the client
   */
  static AsyncSQLClient createShared(Vertx vertx, JsonObject config) {
    return ClientHelper.getOrCreate(vertx, config, DEFAULT_DS_NAME, false);
  }


}
