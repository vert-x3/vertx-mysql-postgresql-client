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
import io.vertx.proxygen.ProxyHelper;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>
 */
@VertxGen
@ProxyGen
public interface PostgresqlService extends BaseSqlService, DatabaseCommands {

  static PostgresqlService create(Vertx vertx, JsonObject config) {
    System.out.println("yay?!");
    return new PostgresqlServiceImpl(vertx, config);
  }

  static PostgresqlService createEventBusProxy(Vertx vertx, String address) {
    return ProxyHelper.createProxy(PostgresqlService.class, vertx, address);
  }

  void start(Handler<AsyncResult<Void>> whenDone);

  void stop(Handler<AsyncResult<Void>> whenDone);

  /**
   * Begins a transaction.
   *
   * @return The transaction.
   */
  PostgresqlTransaction begin();

}
