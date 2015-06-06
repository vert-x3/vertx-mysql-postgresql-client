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

package io.vertx.rxjava.ext.asyncsql;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.rxjava.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 *
 * Represents an asynchronous MySQL client
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.asyncsql.MySQLClient original} non RX-ified interface using Vert.x codegen.
 */

public class MySQLClient extends AsyncSQLClient {

  final io.vertx.ext.asyncsql.MySQLClient delegate;

  public MySQLClient(io.vertx.ext.asyncsql.MySQLClient delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * Create a MySQL client which maintains its own pool.
   * @param vertx the Vert.x instance
   * @param config the configuration
   * @return the client
   */
  public static AsyncSQLClient createNonShared(Vertx vertx, JsonObject config) { 
    AsyncSQLClient ret= AsyncSQLClient.newInstance(io.vertx.ext.asyncsql.MySQLClient.createNonShared((io.vertx.core.Vertx) vertx.getDelegate(), config));
    return ret;
  }

  /**
   * Create a MySQL client which shares its data source with any other MySQL clients created with the same
   * data source name
   * @param vertx the Vert.x instance
   * @param config the configuration
   * @param poolName the pool name
   * @return the client
   */
  public static AsyncSQLClient createShared(Vertx vertx, JsonObject config, String poolName) { 
    AsyncSQLClient ret= AsyncSQLClient.newInstance(io.vertx.ext.asyncsql.MySQLClient.createShared((io.vertx.core.Vertx) vertx.getDelegate(), config, poolName));
    return ret;
  }

  /**
   * Like {@link io.vertx.rxjava.ext.asyncsql.MySQLClient#createShared} but with the default pool name
   * @param vertx the Vert.x instance
   * @param config the configuration
   * @return the client
   */
  public static AsyncSQLClient createShared(Vertx vertx, JsonObject config) { 
    AsyncSQLClient ret= AsyncSQLClient.newInstance(io.vertx.ext.asyncsql.MySQLClient.createShared((io.vertx.core.Vertx) vertx.getDelegate(), config));
    return ret;
  }


  public static MySQLClient newInstance(io.vertx.ext.asyncsql.MySQLClient arg) {
    return new MySQLClient(arg);
  }
}
