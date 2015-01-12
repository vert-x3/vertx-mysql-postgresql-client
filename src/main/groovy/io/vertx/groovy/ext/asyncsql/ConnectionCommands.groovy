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
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@CompileStatic
public interface ConnectionCommands {
  public Object getDelegate();
  void startTransaction(Handler<AsyncResult<Void>> handler);
  void commit(Handler<AsyncResult<Void>> resultHandler);
  void rollback(Handler<AsyncResult<Void>> resultHandler);
  void close(Handler<AsyncResult<Void>> handler);

  static final java.util.function.Function<io.vertx.ext.asyncsql.ConnectionCommands, ConnectionCommands> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.asyncsql.ConnectionCommands arg -> new ConnectionCommandsImpl(arg);
  };
}

@CompileStatic
class ConnectionCommandsImpl implements ConnectionCommands {
  final def io.vertx.ext.asyncsql.ConnectionCommands delegate;
  public ConnectionCommandsImpl(io.vertx.ext.asyncsql.ConnectionCommands delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Starts a transaction on this connection.
   *
   * @param handler Tells the caller whether the transaction command was successful.
   */
  public void startTransaction(Handler<AsyncResult<Void>> handler) {
    ((io.vertx.ext.asyncsql.ConnectionCommands) this.delegate).startTransaction(handler);
  }
  /**
   * Commits a transaction.
   *
   * @param resultHandler Callback if commit succeeded.
   */
  public void commit(Handler<AsyncResult<Void>> resultHandler) {
    ((io.vertx.ext.asyncsql.ConnectionCommands) this.delegate).commit(resultHandler);
  }
  /**
   * Rolls back a transaction.
   *
   * @param resultHandler Callback if rollback succeeded.
   */
  public void rollback(Handler<AsyncResult<Void>> resultHandler) {
    ((io.vertx.ext.asyncsql.ConnectionCommands) this.delegate).rollback(resultHandler);
  }
  /**
   * Frees the connection and puts it back into the pool.
   *
   * @param handler Callback to show whether it succeeded.
   */
  public void close(Handler<AsyncResult<Void>> handler) {
    ((io.vertx.ext.asyncsql.ConnectionCommands) this.delegate).close(handler);
  }
}
