package io.vertx.ext.asyncsql;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;

import java.util.List;

/**
 * Tests the configuration options of the PostgreSQL client.
 */
public class PostgreSQLConfigurationTest extends ConfigurationTest {

  @Override
  protected SQLClient createClient(Vertx vertx, JsonObject config) {
    return PostgreSQLClient.createNonShared(vertx, config);
  }

  @Override
  public String sleepCommand(int seconds) {
    return "pg_sleep(" + seconds + ")";
  }

  @Override
  protected String getEncodingStatement() {
    return "SHOW client_encoding";
  }

  @Override
  protected String getEncodingValueFromResults(List<JsonArray> results) {
    return results.get(0).getString(0);
  }

}
