package io.vertx.ext.asyncsql.impl;

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import scala.concurrent.ExecutionContext;

import java.util.Objects;

public class VertxExecutionContext implements ExecutionContext {

  private final Vertx vertx;

  public static ExecutionContext create(Vertx vertx) {
    return new VertxExecutionContext(vertx, (ex) -> LOGGER.error("An exception occurred", ex));
  }

  public static ExecutionContext create(Vertx vertx,
                                        Handler<Throwable> handler) {
    return new VertxExecutionContext(vertx, handler);
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(VertxExecutionContext.class);

  private final Handler<Throwable> errorHandler;

  private VertxExecutionContext(Vertx vertx, Handler<Throwable> errorHandler) {
    Objects.requireNonNull(errorHandler);
    Objects.requireNonNull(vertx);
    this.vertx = vertx;
    this.errorHandler = errorHandler;
  }

  @Override
  public void execute(Runnable runnable) {
    if (Context.isOnWorkerThread()) {
      // Keep using the same thread.
      runnable.run();
    } else {
      vertx.executeBlocking((v) -> runnable.run(), null);
    }
  }

  @Override
  public void reportFailure(Throwable cause) {
    errorHandler.handle(cause);
  }

  @Override
  public ExecutionContext prepare() {
    return this;
  }
}
