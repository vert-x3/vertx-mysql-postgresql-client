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

package io.vertx.groovy.ext.asyncsql.mysql;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonArray
import java.util.List
import io.vertx.groovy.ext.asyncsql.DatabaseCommands
import io.vertx.ext.asyncsql.SelectOptions
import io.vertx.core.json.JsonObject
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.groovy.ext.asyncsql.ConnectionCommands
/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@CompileStatic
public class MysqlConnection implements ConnectionCommands,  DatabaseCommands {
  final def io.vertx.ext.asyncsql.mysql.MysqlConnection delegate;
  public MysqlConnection(io.vertx.ext.asyncsql.mysql.MysqlConnection delegate) {
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
  /**
   * Sends a raw command to the database.
   *
   * @param command       The command to send.
   * @param resultHandler Callback to handle the result.
   */
  public void raw(String command, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    ((io.vertx.ext.asyncsql.DatabaseCommands) this.delegate).raw(command, new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result(event.result()?.getMap())
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  /**
   * Inserts new values into a table. Use this action to insert new rows into a table. You need to specify a table, the
   * fields to insert and an array of rows to insert. The rows itself are an array of values.
   *
   * @param table         The name of the table.
   * @param fields        The fields to put values into.
   * @param values        JsonArrays full of values to put into the fields.
   * @param resultHandler Callback to handle the result.
   */
  public void insert(String table, List<String> fields, List<List<Object>> values, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    ((io.vertx.ext.asyncsql.DatabaseCommands) this.delegate).insert(table, fields, values.collect({underpants -> new JsonArray(underpants)}), new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result(event.result()?.getMap())
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  /**
   * Reads data from a table. The select action creates a SELECT statement to get a projection from a table. You can
   * filter the columns by providing a fields array. If you omit the fields array, it selects every column available in
   * the table.
   *
   * @param table         The table to select data from.
   * @param options       Options for Limit/Offset etc.
   * @param resultHandler Callback to handle the result.
   */
  public void select(String table, Map<String, Object> options, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    ((io.vertx.ext.asyncsql.DatabaseCommands) this.delegate).select(table, options != null ? new io.vertx.ext.asyncsql.SelectOptions(new io.vertx.core.json.JsonObject(options)) : null, new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result(event.result()?.getMap())
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  /**
   * Creates a prepared statement and lets you fill the ? with values.
   *
   * @param statement     The statement to prepare.
   * @param values        The values to set for the "?" placeholders.
   * @param resultHandler Callback to handle the result.
   */
  public void prepared(String statement, List<Object> values, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    ((io.vertx.ext.asyncsql.DatabaseCommands) this.delegate).prepared(statement, values != null ? new io.vertx.core.json.JsonArray(values) : null, new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result(event.result()?.getMap())
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }

  static final java.util.function.Function<io.vertx.ext.asyncsql.mysql.MysqlConnection, MysqlConnection> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.asyncsql.mysql.MysqlConnection arg -> new MysqlConnection(arg);
  };
}
