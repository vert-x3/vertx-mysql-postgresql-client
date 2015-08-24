package io.vertx.ext.asyncsql.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import scala.concurrent.ExecutionContext;

import java.util.Objects;

/**
 * Execution environment for Scala Future. The submitted {@link Runnable} are executed in the Vert.x Event Loop.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class VertxEventLoopExecutionContext implements ExecutionContext {

  public static ExecutionContext create(Vertx vertx) {
    return new VertxEventLoopExecutionContext(vertx, (ex) -> LOGGER.error("An exception occurred", ex));
  }

  public static ExecutionContext create(Vertx vertx,
                                        Handler<Throwable> handler) {
    return new VertxEventLoopExecutionContext(vertx, handler);
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(VertxEventLoopExecutionContext.class);

  private final Vertx vertx;
  private final Handler<Throwable> errorHandler;

  private VertxEventLoopExecutionContext(Vertx vertx, Handler<Throwable> errorHandler) {
    Objects.requireNonNull(errorHandler);
    Objects.requireNonNull(vertx);
    this.vertx = vertx;
    this.errorHandler = errorHandler;
  }

  @Override
  public void execute(Runnable runnable) {
    vertx.runOnContext(v -> runnable.run());
  }

  @Override
  public void reportFailure(Throwable cause) {
    errorHandler.handle(cause);
  }

  @Override
  public ExecutionContext prepare() {
    // No preparation required.
    return this;
  }
}
