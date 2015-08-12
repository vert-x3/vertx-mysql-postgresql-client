package io.vertx.ext.asyncsql.impl;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool;
import io.vertx.ext.asyncsql.impl.pool.PostgresqlAsyncConnectionPool;

public class PostgreSQLClientImpl extends BaseSQLClient {

  private final PostgresqlAsyncConnectionPool pool;

  public PostgreSQLClientImpl(Vertx vertx, JsonObject config) {
    super(vertx, config);
    pool = new PostgresqlAsyncConnectionPool(vertx, maxPoolSize, getConfiguration(
        PostgreSQLClient.DEFAULT_HOST,
        PostgreSQLClient.DEFAULT_PORT,
        PostgreSQLClient.DEFAULT_DATABASE,
        PostgreSQLClient.DEFAULT_USER,
        PostgreSQLClient.DEFAULT_PASSWORD,
        config));
  }

  @Override
  protected AsyncConnectionPool pool() {
    return pool;
  }
}
