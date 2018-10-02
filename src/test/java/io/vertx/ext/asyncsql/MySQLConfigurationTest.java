package io.vertx.ext.asyncsql;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.category.NeedsDocker;
import io.vertx.ext.sql.SQLClient;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

import java.util.List;

import static io.vertx.ext.asyncsql.SQLTestBase.*;

/**
 * Tests the configuration options of the MySQL client.
 */
@Category(NeedsDocker.class)
public class MySQLConfigurationTest extends ConfigurationTest {

  public static GenericContainer mysql = new MySQLContainer("mysql:5.6")
    .withDatabaseName(MYSQL_DATABASE)
    .withUsername(MYSQL_USERNAME)
    .withPassword(MYSQL_PASSWORD)
    .withExposedPorts(3306);

  static {
    mysql.start();
  }

  @Override
  protected SQLClient createClient(Vertx vertx, JsonObject config) {
    return MySQLClient.createNonShared(vertx, config.mergeIn(new JsonObject()
      .put("host", mysql.getContainerIpAddress())
      .put("port", mysql.getMappedPort(3306))
      .put("database", MYSQL_DATABASE)
      .put("username", MYSQL_USERNAME)
      .put("password", MYSQL_PASSWORD)));
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
