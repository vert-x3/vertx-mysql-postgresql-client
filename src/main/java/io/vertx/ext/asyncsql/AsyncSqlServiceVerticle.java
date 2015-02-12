package io.vertx.ext.asyncsql;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.concurrent.CountDownLatch;

/**
 * Base class for async SQL service verticles.
 *
 * @author <a href="http://www.campudus.com/">Joern Bernhardt</a>
 */
public abstract class AsyncSqlServiceVerticle extends AbstractVerticle {

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
    final String address = conf.getString("address");
    if (address == null) {
      throw new IllegalStateException("address field must be specified in config for service verticle");
    }
    service = createService(vertx, conf);
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

  protected abstract AsyncSqlService createService(Vertx vertx, JsonObject config);
}