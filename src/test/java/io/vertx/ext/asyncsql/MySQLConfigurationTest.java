package io.vertx.ext.asyncsql;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;

import java.util.List;

/**
 * Tests the configuration options of the MySQL client.
 */
public class MySQLConfigurationTest extends ConfigurationTest {

  @Override
  protected SQLClient createClient(Vertx vertx, JsonObject config) {
    return MySQLClient.createNonShared(vertx, config);
  }

  @Override
  public String sleepCommand(int seconds) {
    return "sleep(" + seconds + ")";
  }

  @Override
  protected String getEncodingStatement() {
    return "SHOW VARIABLES LIKE 'character_set_connection'";
  }

  @Override
  protected String getEncodingValueFromResults(List<JsonArray> results) {
    return results.get(0).getString(1);
  }

}
