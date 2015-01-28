package io.vertx.ext.asyncsql;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="http://www.campudus.com/">Joern Bernhardt</a>
 */
public class AsyncSqlServiceVerticle extends AbstractVerticle {

  AsyncSqlService postgresqlService;
  AsyncSqlService mysqlService;

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    System.out.println("starting AsyncSqlServiceVerticle with config " + config().encode());
    final JsonObject conf = config();
    final CountDownLatch cdl = new CountDownLatch(1);
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
    final String address = conf.getJsonObject("postgresql", conf.getJsonObject("mysql")).getString("address");
    if (address == null) {
      throw new IllegalStateException("address field must be specified in config for service verticle");
    }
    final AsyncSqlService service = AsyncSqlService.create(vertx, conf);
    ProxyHelper.registerService(AsyncSqlService.class, vertx, service, address);
    service.start(simpleEndHandler);
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