package io.vertx.ext.asyncsql;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.impl.AsyncSqlServiceImpl;
import io.vertx.ext.sql.SqlConnection;
import io.vertx.serviceproxy.ProxyHelper;

/**
 *
 * Represents an asynchronous MySQL or PostgreSQL service.
 *
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@VertxGen
@ProxyGen
public interface AsyncSqlService {

  /**
   * Create a MySQL service
   *
   * @param vertx  the Vert.x instance
   * @param config  the config
   * @return the service
   */
  static AsyncSqlService createMySqlService(Vertx vertx, JsonObject config) {
    return new AsyncSqlServiceImpl(vertx, config, true);
  }

  /**
   * Create a PostgreSQL service
   *
   * @param vertx  the Vert.x instance
   * @param config  the config
   * @return the service
   */
  static AsyncSqlService createPostgreSqlService(Vertx vertx, JsonObject config) {
    return new AsyncSqlServiceImpl(vertx, config, false);
  }

  /**
   * Create an event bus proxy to a service which lives somewhere on the network and is listening on the specified
   * event bus address
   *
   * @param vertx  the Vert.x instance
   * @param address  the address on the event bus where the service is listening
   * @return
   */
  static AsyncSqlService createEventBusProxy(Vertx vertx, String address) {
    return ProxyHelper.createProxy(AsyncSqlService.class, vertx, address);
  }

  /**
   * Called to start the service
   */
  @ProxyIgnore
  void start(Handler<AsyncResult<Void>> whenDone);

  /**
   * Called to stop the service
   */
  @ProxyIgnore
  void stop(Handler<AsyncResult<Void>> whenDone);

  /**
   * Returns a connection that can be used to perform SQL operations on. It's important to remember to close the
   * connection when you are done, so it is returned to the pool.
   *
   * @param handler the handler which is called when the <code>JdbcConnection</code> object is ready for use.
   */
  void getConnection(Handler<AsyncResult<SqlConnection>> handler);

}
