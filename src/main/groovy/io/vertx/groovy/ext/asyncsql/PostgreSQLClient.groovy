/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.groovy.ext.asyncsql;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.groovy.core.Vertx
import io.vertx.core.json.JsonObject
/**
 * Represents an PostgreSQL client
*/
@CompileStatic
public class PostgreSQLClient extends AsyncSQLClient {
  private final def io.vertx.ext.asyncsql.PostgreSQLClient delegate;
  public PostgreSQLClient(Object delegate) {
    super((io.vertx.ext.asyncsql.PostgreSQLClient) delegate);
    this.delegate = (io.vertx.ext.asyncsql.PostgreSQLClient) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Create a PostgreSQL client which maintains its own pool.
   * @param vertx the Vert.x instance
   * @param config the configuration
   * @return the client
   */
  public static AsyncSQLClient createNonShared(Vertx vertx, Map<String, Object> config) {
    def ret= InternalHelper.safeCreate(io.vertx.ext.asyncsql.PostgreSQLClient.createNonShared((io.vertx.core.Vertx)vertx.getDelegate(), config != null ? new io.vertx.core.json.JsonObject(config) : null), io.vertx.groovy.ext.asyncsql.AsyncSQLClient.class);
    return ret;
  }
  /**
   * Create a PostgreSQL client which shares its pool with any other MySQL clients created with the same
   * pool name.
   * @param vertx the Vert.x instance
   * @param config the configuration
   * @param poolName the pool name
   * @return the client
   */
  public static AsyncSQLClient createShared(Vertx vertx, Map<String, Object> config, String poolName) {
    def ret= InternalHelper.safeCreate(io.vertx.ext.asyncsql.PostgreSQLClient.createShared((io.vertx.core.Vertx)vertx.getDelegate(), config != null ? new io.vertx.core.json.JsonObject(config) : null, poolName), io.vertx.groovy.ext.asyncsql.AsyncSQLClient.class);
    return ret;
  }
  /**
   * Like {@link io.vertx.groovy.ext.asyncsql.PostgreSQLClient#createShared} but with the default pool name
   * @param vertx the Vert.x instance
   * @param config the configuration
   * @return the client
   */
  public static AsyncSQLClient createShared(Vertx vertx, Map<String, Object> config) {
    def ret= InternalHelper.safeCreate(io.vertx.ext.asyncsql.PostgreSQLClient.createShared((io.vertx.core.Vertx)vertx.getDelegate(), config != null ? new io.vertx.core.json.JsonObject(config) : null), io.vertx.groovy.ext.asyncsql.AsyncSQLClient.class);
    return ret;
  }
}
