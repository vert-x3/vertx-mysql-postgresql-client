package io.vertx.ext.asyncsql;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@VertxGen
@ProxyGen
public interface AsyncSqlConnection {

  /**
   * Sets the auto commit flag for this connection. True by default. Set to false if you want to start a transaction.
   * <p>
   * If you change autoCommit from false to true, it will commit the running transaction. If you change it from false to
   * true, it will start a new transaction. If the autoCommit flag doesn't change, it will just call the resultHandler
   * with a success.
   *
   * @param autoCommit    the autoCommit flag, true by default.
   * @param resultHandler The handler which is called once this operation completes.
   * @see java.sql.Connection#setAutoCommit(boolean)
   */
  @Fluent
  AsyncSqlConnection setAutoCommit(boolean autoCommit, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Executes the given SQL statement.
   *
   * @param sql           The SQL to execute. For example <code>CREATE TABLE IF EXISTS table ...</code>
   * @param resultHandler The handler which is called once this operation completes.
   */
  @Fluent
  AsyncSqlConnection execute(String sql, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Executes the given SQL <code>SELECT</code> statement which returns the results of the query.
   *
   * @param sql           The SQL to execute. For example <code>SELECT * FROM mytable</code>.
   * @param resultHandler The handler which is called once the operation completes. It will return a list of
   *                      <code>JsonObject</code>'s which represent the ResultSet. So column names are keys, and values
   *                      are of course values.
   */
  @Fluent
  AsyncSqlConnection query(String sql, Handler<AsyncResult<ResultSet>> resultHandler);

  /**
   * Executes the given SQL <code>SELECT</code> statement which returns the results of the query. It will use a prepared
   * statement to pass the parameters.
   *
   * @param sql           The SQL to execute. For example <code>SELECT * FROM mytable WHERE id=?</code>.
   * @param params        These are the parameters to fill the statement.
   * @param resultHandler The handler which is called once the operation completes. It will return a list of
   *                      <code>JsonObject</code>'s which represent the ResultSet. So column names are keys, and values
   *                      are of course values.
   */
  @Fluent
  AsyncSqlConnection queryWithParams(String sql, JsonArray params, Handler<AsyncResult<ResultSet>> resultHandler);

  /**
   * Executes the given SQL statement which may be an <code>INSERT</code>, <code>UPDATE</code>, or <code>DELETE</code>
   * statement.
   *
   * @param sql           The SQL to execute. For example <code>INSERT INTO table ...</code>
   * @param resultHandler The handler which is called once the operation completes.
   */
  @Fluent
  AsyncSqlConnection update(String sql, Handler<AsyncResult<UpdateResult>> resultHandler);

  /**
   * Executes the given SQL statement which may be an <code>INSERT</code>, <code>UPDATE</code>, or <code>DELETE</code>
   * statement.
   *
   * @param sql           The SQL to execute. For example <code>INSERT INTO mytable ('name', 'age') VALUES (?,
   *                      ?)</code>
   * @param params        These are the parameters to fill the statement.
   * @param resultHandler The handler which is called once the operation completes.
   */
  @Fluent
  AsyncSqlConnection updateWithParams(String sql, JsonArray params, Handler<AsyncResult<UpdateResult>> resultHandler);

  /**
   * Closes the connection. Important to always close the connection when you are done so it's returned to the pool.
   *
   * @param handler The handler which is called once the operation completes.
   */
  @ProxyClose
  void close(Handler<AsyncResult<Void>> handler);

  /**
   * Commits all changes made since the previous commit/rollback.
   *
   * @param handler The handler which is called once the operation completes.
   */
  @Fluent
  AsyncSqlConnection commit(Handler<AsyncResult<Void>> handler);

  /**
   * Rolls back all changes made since the previous commit/rollback.
   *
   * @param handler The handler which is called once the operation completes.
   */
  @Fluent
  AsyncSqlConnection rollback(Handler<AsyncResult<Void>> handler);
}
