package io.vertx.ext.asyncsql;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@VertxGen(concrete = false)
public interface BaseSqlService {
  void start(Handler<AsyncResult<Void>> whenDone);

  void stop(Handler<AsyncResult<Void>> whenDone);
}
