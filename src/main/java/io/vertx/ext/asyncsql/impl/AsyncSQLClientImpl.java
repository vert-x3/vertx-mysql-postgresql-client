package io.vertx.ext.asyncsql.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.sql.SQLConnection;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
public class AsyncSQLClientImpl implements AsyncSQLClient {

  private final BaseSQLClient baseClient;

  public AsyncSQLClientImpl(Vertx vertx, JsonObject config, boolean mysql) {
    if (mysql) {
      baseClient = new MYSQLClientImpl(vertx, config);
    } else {
      baseClient = new PostgreSQLClientImpl(vertx, config);
    }
  }

  @Override
  public void close() {
    baseClient.close(null);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> whenDone) {
    baseClient.close(whenDone);
  }

  @Override
  public void getConnection(Handler<AsyncResult<SQLConnection>> handler) {
    baseClient.getConnection(handler);
  }

}
