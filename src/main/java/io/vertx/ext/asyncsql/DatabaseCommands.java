package io.vertx.ext.asyncsql;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@VertxGen(concrete = false)
public interface DatabaseCommands {

  /**
   * Sends a raw command to the database.
   *
   * @param command       The command to send.
   * @param resultHandler Callback to handle the result.
   */
  void raw(String command, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Inserts new values into a table. Use this action to insert new rows into a table. You need to specify a table, the
   * fields to insert and an array of rows to insert. The rows itself are an array of values.
   *
   * @param table         The name of the table.
   * @param fields        The fields to put values into.
   * @param values        JsonArrays full of values to put into the fields.
   * @param resultHandler Callback to handle the result.
   */
  void insert(String table, List<String> fields, List<JsonArray> values, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Reads data from a table. The select action creates a SELECT statement to get a projection from a table. You can
   * filter the columns by providing a fields array. If you omit the fields array, it selects every column available in
   * the table.
   *
   * @param table         The table to select data from.
   * @param options       Options for Limit/Offset etc.
   * @param resultHandler Callback to handle the result.
   */
  void select(String table, SelectOptions options, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Creates a prepared statement and lets you fill the ? with values.
   *
   * @param statement     The statement to prepare.
   * @param values        The values to set for the "?" placeholders.
   * @param resultHandler Callback to handle the result.
   */
  void prepared(String statement, JsonArray values, Handler<AsyncResult<JsonObject>> resultHandler);

}
