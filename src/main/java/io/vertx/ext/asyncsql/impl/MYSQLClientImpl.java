package io.vertx.ext.asyncsql.impl;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool;
import io.vertx.ext.asyncsql.impl.pool.MysqlAsyncConnectionPool;

public class MYSQLClientImpl extends BaseSQLClient {

  private final MysqlAsyncConnectionPool pool;

  public MYSQLClientImpl(Vertx vertx, JsonObject config) {
    super(vertx, config);
    pool = new MysqlAsyncConnectionPool(vertx, maxPoolSize, getConfiguration(
        MySQLClient.DEFAULT_HOST,
        MySQLClient.DEFAULT_PORT,
        MySQLClient.DEFAULT_DATABASE,
        MySQLClient.DEFAULT_USER,
        MySQLClient.DEFAULT_PASSWORD,
        config));
  }

  @Override
  protected AsyncConnectionPool pool() {
    return pool;
  }
}
