package io.vertx.ext.asyncsql;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@VertxGen(concrete = false)
public interface ConnectionCommands {

  /**
   * Starts a transaction on this connection.
   *
   * @param handler Tells the caller whether the transaction command was successful.
   */
  void startTransaction(Handler<AsyncResult<Void>> handler);

  /**
   * Commits a transaction.
   *
   * @param resultHandler Callback if commit succeeded.
   */
  void commit(Handler<AsyncResult<Void>> resultHandler);

  /**
   * Rolls back a transaction.
   *
   * @param resultHandler Callback if rollback succeeded.
   */
  void rollback(Handler<AsyncResult<Void>> resultHandler);

  /**
   * Frees the connection and puts it back into the pool.
   *
   * @param handler Callback to show whether it succeeded.
   */
  @ProxyClose
  void close(Handler<AsyncResult<Void>> handler);

}
