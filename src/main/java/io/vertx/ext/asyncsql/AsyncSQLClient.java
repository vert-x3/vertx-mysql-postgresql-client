package io.vertx.ext.asyncsql;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.sql.SQLConnection;

/**
 *
 * Represents an asynchronous SQL client
 *
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@VertxGen
public interface AsyncSQLClient {

  /**
   * Close the client and release all resources
   */
  void close(Handler<AsyncResult<Void>> whenDone);

  /**
   * Returns a connection that can be used to perform SQL operations on. It's important to remember to close the
   * connection when you are done, so it is returned to the pool.
   *
   * @param handler the handler which is called when the <code>JdbcConnection</code> object is ready for use.
   */
  void getConnection(Handler<AsyncResult<SQLConnection>> handler);

}
