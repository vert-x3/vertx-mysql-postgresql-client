/*
 *  Copyright 2015 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.asyncsql.impl;

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import scala.concurrent.ExecutionContext;

import java.util.Objects;

/**
 * Execution environment for Scala Future. The submitted {@link Runnable} are executed in the Vert.x Event Loop. The
 * event loop is the one attached to the passed {@link Context}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class VertxEventLoopExecutionContext implements ExecutionContext {

  public static ExecutionContext create(Context context) {
    return new VertxEventLoopExecutionContext(context, (ex) -> LOGGER.error("An exception occurred", ex));
  }

  public static ExecutionContext create(Context context, Handler<Throwable> handler) {
    return new VertxEventLoopExecutionContext(context, handler);
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(VertxEventLoopExecutionContext.class);

  private final Context context;
  private final Handler<Throwable> errorHandler;

  private VertxEventLoopExecutionContext(Context context, Handler<Throwable> errorHandler) {
    Objects.requireNonNull(errorHandler);
    Objects.requireNonNull(context);
    this.context = context;
    this.errorHandler = errorHandler;
  }

  @Override
  public void execute(Runnable runnable) {
    context.runOnContext(v -> {
      try {
        runnable.run();
      } catch (Throwable e) {
        reportFailure(e);
      }
    });
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
