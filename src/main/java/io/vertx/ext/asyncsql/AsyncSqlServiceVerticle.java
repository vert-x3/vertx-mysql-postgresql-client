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

  AsyncSqlService service;

  @Override
  public void start(Future<Void> startFuture) throws Exception {
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
    service = AsyncSqlService.create(vertx, conf);
    ProxyHelper.registerService(AsyncSqlService.class, vertx, service, address);
    service.start(simpleEndHandler);
  }

  @Override
  public void stop(Future<Void> stopFuture) throws Exception {
    if (service != null) {
      service.stop(res -> {
        if (res.failed()) {
          stopFuture.fail(res.cause());
        } else {
          stopFuture.complete();
        }
      });
    }
  }
}