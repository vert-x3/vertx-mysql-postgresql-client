package io.vertx.ext.asyncsql.impl;

import io.vertx.core.Context;
import io.vertx.core.Vertx;

import java.util.concurrent.Executor;

/**
 * @author <a href="mailto:andy.yx.chen@outlook.com">Andy Chen</a>.
 */
public class VxContextAwareExecutor implements Executor {

  private final Context context;

  public VxContextAwareExecutor(Vertx vertx) {
    this.context = vertx.getOrCreateContext();
  }

  @Override
  public void execute(Runnable command) {
    this.context.runOnContext(ignored -> {
      command.run();
    });
  }
}
