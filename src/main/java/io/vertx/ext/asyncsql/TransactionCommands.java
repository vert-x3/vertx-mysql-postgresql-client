package io.vertx.ext.asyncsql;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@VertxGen(concrete = false)
public interface TransactionCommands {

  /**
   * Commits a transaction.
   *
   * @param resultHandler Callback if commit succeeded.
   */
  void commit(Handler<AsyncResult<String>> resultHandler);

  /**
   * Rolls back a transaction.
   *
   * @param resultHandler Callback if rollback succeeded.
   */
  void rollback(Handler<AsyncResult<Void>> resultHandler);

}
