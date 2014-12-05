package io.vertx.ext.asyncsql.postgresql;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.BaseSqlService;
import io.vertx.ext.asyncsql.DatabaseCommands;
import io.vertx.ext.asyncsql.postgresql.impl.PostgresqlServiceImpl;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>
 */
@VertxGen
@ProxyGen
public interface PostgresqlService extends BaseSqlService, DatabaseCommands {

  static PostgresqlService create(Vertx vertx, JsonObject config) {
    return new PostgresqlServiceImpl(vertx, config);
  }

  static PostgresqlService createEventBusProxy(Vertx vertx, String address) {
    return ProxyHelper.createProxy(PostgresqlService.class, vertx, address);
  }

  /**
   * Begins a transaction and frees it on commit or callback.
   *
   * @param transaction The transaction to be used.
   */
  void begin(Handler<AsyncResult<PostgresqlTransaction>> transaction);

  /**
   * Gets a connection and frees it on close.
   *
   * @param connection The connection that can be used.
   */
  void take(Handler<AsyncResult<PostgresqlConnection>> connection);

}
