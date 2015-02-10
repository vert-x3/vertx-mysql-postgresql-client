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
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@VertxGen
@ProxyGen
public interface AsyncSqlService {

  static AsyncSqlService createMySqlService(Vertx vertx, JsonObject config) {
    return new AsyncSqlServiceImpl(vertx, config, true);
  }

  static AsyncSqlService createPostgreSqlService(Vertx vertx, JsonObject config) {
    return new AsyncSqlServiceImpl(vertx, config, false);
  }

  static AsyncSqlService createEventBusProxy(Vertx vertx, String address) {
    return ProxyHelper.createProxy(AsyncSqlService.class, vertx, address);
  }

  /**
   * Normally invoked by the <code>AsyncSqlServiceVerticle</code> to start the service when deployed. This is usually
   * not called by the user.
   */
  @ProxyIgnore
  void start(Handler<AsyncResult<Void>> whenDone);

  /**
   * Normally invoked by the <code>AsyncSqlServiceVerticle</code> to stop the service when the verticle is
   * stopped/undeployed. This is usually not called by the user.
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
