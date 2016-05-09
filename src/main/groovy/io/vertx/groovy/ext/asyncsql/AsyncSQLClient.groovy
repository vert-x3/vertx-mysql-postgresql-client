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
import io.vertx.core.json.JsonObject
import io.vertx.groovy.ext.sql.SQLConnection
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * Represents an asynchronous SQL client
*/
@CompileStatic
public class AsyncSQLClient {
  private final def io.vertx.ext.asyncsql.AsyncSQLClient delegate;
  public AsyncSQLClient(Object delegate) {
    this.delegate = (io.vertx.ext.asyncsql.AsyncSQLClient) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Close the client and release all resources.
   * Note that closing is asynchronous.
   */
  public void close() {
    delegate.close();
  }
  /**
   * Close the client and release all resources.
   * Call the handler when close is complete.
   * @param whenDone handler that will be called when close is complete
   */
  public void close(Handler<AsyncResult<Void>> whenDone) {
    delegate.close(whenDone);
  }
  /**
   * Returns a connection that can be used to perform SQL operations on. It's important to remember to close the
   * connection when you are done, so it is returned to the pool.
   * @param handler the handler which is called when the <code>JdbcConnection</code> object is ready for use.
   */
  public void getConnection(Handler<AsyncResult<SQLConnection>> handler) {
    delegate.getConnection(handler != null ? new Handler<AsyncResult<io.vertx.ext.sql.SQLConnection>>() {
      public void handle(AsyncResult<io.vertx.ext.sql.SQLConnection> ar) {
        if (ar.succeeded()) {
          handler.handle(io.vertx.core.Future.succeededFuture(InternalHelper.safeCreate(ar.result(), io.vertx.groovy.ext.sql.SQLConnection.class)));
        } else {
          handler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
}
