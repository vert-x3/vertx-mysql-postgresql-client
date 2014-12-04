package io.vertx.ext.asyncsql;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.mysql.MysqlService;
import io.vertx.ext.asyncsql.postgresql.PostgresqlService;
import io.vertx.proxygen.ProxyHelper;

import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * @author <a href="http://www.campudus.com/">Joern Bernhardt</a>
 */
public class AsyncSqlServiceVerticle extends AbstractVerticle {

  PostgresqlService postgresqlService;
  MysqlService mysqlService;

  private <T extends BaseSqlService> T createServiceFor(JsonObject config, Class<T> cls, Function<Pair<Vertx, JsonObject>, T> createServiceFn, Handler<AsyncResult<Void>> done) {
    final String address = config.getString("address");
    if (address == null) {
      throw new IllegalStateException("address field must be specified in config for service verticle");
    }
    final T service = createServiceFn.apply(new Pair<>(vertx, config));
    ProxyHelper.registerService(cls, vertx, service, address);
    service.start(done);
    return service;
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    final JsonObject postgresqlConfig = config().getJsonObject("postgresql");
    final JsonObject mysqlConfig = config().getJsonObject("mysql");
    final CountDownLatch cdl =
      new CountDownLatch((postgresqlConfig != null ? 1 : 0) + (mysqlConfig != null ? 1 : 0));
    final Handler<AsyncResult<Void>> simpleEndHandler = res -> {
      cdl.countDown();
      if (res.failed()) {
        startFuture.fail(res.cause());
      } else {
        if (cdl.getCount() == 0 && !startFuture.isComplete()) {
          startFuture.complete();
        }
      }
    };

    // Create the service object
    if (postgresqlConfig != null) {
      postgresqlService =
        createServiceFor(
          postgresqlConfig,
          PostgresqlService.class,
          p -> PostgresqlService.create(p.t1, p.t2),
          simpleEndHandler);
    }

    if (mysqlConfig != null) {
      mysqlService =
        createServiceFor(
          mysqlConfig,
          MysqlService.class,
          p -> MysqlService.create(p.t1, p.t2),
          simpleEndHandler);
    }

  }

  @Override
  public void stop(Future<Void> stopFuture) throws Exception {
    if (postgresqlService != null) {
      postgresqlService.stop(res -> {
        if (res.failed()) {
          stopFuture.fail(res.cause());
        } else {
          stopFuture.complete();
        }
      });
    } else if (mysqlService != null) {
      mysqlService.stop(res -> {
        if (res.failed()) {
          stopFuture.fail(res.cause());
        } else {
          stopFuture.complete();
        }
      });
    }
  }
}