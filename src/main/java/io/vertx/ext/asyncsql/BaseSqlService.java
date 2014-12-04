package io.vertx.ext.asyncsql;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
public interface BaseSqlService {
  void start(Handler<AsyncResult<Void>> whenDone);

  void stop(Handler<AsyncResult<Void>> whenDone);
}
