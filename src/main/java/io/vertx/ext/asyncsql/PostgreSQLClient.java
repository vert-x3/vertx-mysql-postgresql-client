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
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.impl.ClientHelper;

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
   * Create a PostgreSQL client which maintains its own pool.
   *
   * @param vertx   the Vert.x instance
   * @param context the Vert.x context to use
   * @param config  the configuration
   * @return the client
   */
  static AsyncSQLClient createNonShared(Vertx vertx, Context context, JsonObject config) {
    return ClientHelper.getOrCreate(vertx, context, config, UUID.randomUUID().toString(), false);
  }

  /**
   * Create a PostgreSQL client which maintains its own pool. It uses the
   * {@link Context} retrieved using {@link Vertx#currentContext()}.
   *
   * @param vertx  the Vert.x instance
   * @param config the configuration
   * @return the client
   */
  static AsyncSQLClient createNonShared(Vertx vertx, JsonObject config) {
    return createNonShared(vertx, Vertx.currentContext(), config);
  }

  /**
   * Create a PostgreSQL client which shares its pool with any other MySQL clients created with the same
   * pool name
   *
   * @param vertx    the Vert.x instance
   * @param context  the Vert.x context to use
   * @param config   the configuration
   * @param poolName the pool name
   * @return the client
   */
  static AsyncSQLClient createShared(Vertx vertx, Context context, JsonObject config, String poolName) {
    return ClientHelper.getOrCreate(vertx, context, config, poolName, false);
  }

  /**
   * Create a PostgreSQL client which shares its pool with any other MySQL clients created with the same
   * pool name. It uses the {@link Context} retrieved using {@link Vertx#currentContext()}.
   *
   * @param vertx    the Vert.x instance
   * @param config   the configuration
   * @param poolName the pool name
   * @return the client
   */
  static AsyncSQLClient createShared(Vertx vertx, JsonObject config, String poolName) {
    return createShared(vertx, Vertx.currentContext(), config, poolName);
  }

  /**
   * Like {@link #createShared(io.vertx.core.Vertx, JsonObject, String)} but with the default pool name
   *
   * @param vertx   the Vert.x instance
   * @param context the Vert.x context to use
   * @param config  the configuration
   * @return the client
   */
  static AsyncSQLClient createShared(Vertx vertx, Context context, JsonObject config) {
    return ClientHelper.getOrCreate(vertx, context, config, DEFAULT_DS_NAME, false);
  }

  /**
   * Like {@link #createShared(io.vertx.core.Vertx, JsonObject, String)} but with the default pool name. It uses the
   * {@link Context} retrieved using {@link Vertx#currentContext()}.
   *
   * @param vertx  the Vert.x instance
   * @param config the configuration
   * @return the client
   */
  static AsyncSQLClient createShared(Vertx vertx, JsonObject config) {
    return createShared(vertx, Vertx.currentContext(), config);
  }


}
