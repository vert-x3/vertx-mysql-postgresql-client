package io.vertx.ext.asyncsql.impl;

import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import scala.concurrent.ExecutionContext;

import java.util.Objects;

public class VertxExecutionContext implements ExecutionContext {

  public static ExecutionContext create() {
    return new VertxExecutionContext((ex) -> LOGGER.error("An exception occurred", ex));
  }

  public static ExecutionContext create(Handler<Throwable> handler) {
    return new VertxExecutionContext(handler);
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(VertxExecutionContext.class);

  private final Handler<Throwable> errorHandler;

  private VertxExecutionContext(Handler<Throwable> errorHandler) {
    Objects.requireNonNull(errorHandler);
    this.errorHandler = errorHandler;
  }

  @Override
  public void execute(Runnable runnable) {
    // TODO This should probably use executeBlocking if we are on an event loop thread.
    runnable.run();
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
