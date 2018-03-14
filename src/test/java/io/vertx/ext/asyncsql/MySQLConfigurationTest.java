package io.vertx.ext.asyncsql;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.List;

import static io.vertx.ext.asyncsql.MySQL.start;
import static io.vertx.ext.asyncsql.SQLTestBase.START_MYSQL;

/**
 * Tests the configuration options of the MySQL client.
 */
public class MySQLConfigurationTest extends ConfigurationTest {

  private static MySQL my;

  @BeforeClass
  public static void before() throws Exception {
    if (START_MYSQL) {
      my = start(SQLTestBase.MYSQL_PORT);
    }
  }

  @AfterClass
  public static void after() throws Exception {
    if (my != null) {
      my.stop();
    }
  }

  @Override
  protected SQLClient createClient(Vertx vertx, JsonObject config) {
    return MySQLClient.createNonShared(vertx, config.mergeIn(SQLTestBase.MYSQL_CONFIG));
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
