package io.vertx.ext.asyncsql.mysql;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.BaseSqlService;
import io.vertx.ext.asyncsql.DatabaseCommands;
import io.vertx.ext.asyncsql.mysql.impl.MysqlServiceImpl;
import io.vertx.proxygen.ProxyHelper;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>
 */
@VertxGen
@ProxyGen
public interface MysqlService extends BaseSqlService, DatabaseCommands {

  static MysqlService create(Vertx vertx, JsonObject config) {
    return new MysqlServiceImpl(vertx, config);
  }

  static MysqlService createEventBusProxy(Vertx vertx, String address) {
    return ProxyHelper.createProxy(MysqlService.class, vertx, address);
  }

  @Override
  void start(Handler<AsyncResult<Void>> whenDone);

  @Override
  void stop(Handler<AsyncResult<Void>> whenDone);

  /**
   * Begins a transaction.
   *
   * @return The transaction.
   */
  MysqlTransaction begin();

}
