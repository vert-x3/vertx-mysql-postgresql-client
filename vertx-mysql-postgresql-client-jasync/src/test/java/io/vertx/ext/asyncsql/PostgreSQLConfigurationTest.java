package io.vertx.ext.asyncsql;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.category.NeedsDocker;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

import static io.vertx.ext.asyncsql.SQLTestBase.*;

/**
 * Tests the configuration options of the PostgreSQL client.
 */
@Category(NeedsDocker.class)
public class PostgreSQLConfigurationTest extends ConfigurationTest {

  public static GenericContainer postgresql = new PostgreSQLContainer()
    .withDatabaseName(POSTGRESQL_DATABASE)
    .withUsername(POSTGRESQL_USERNAME)
    .withPassword(POSTGRESQL_PASSWORD)
    .withExposedPorts(5432);

  static {
    postgresql.start();
  }

  @Override
  protected SQLClient createClient(Vertx vertx, JsonObject config) {
    return PostgreSQLClient.createNonShared(vertx, config.mergeIn(new JsonObject()
      .put("host", postgresql.getContainerIpAddress())
      .put("port", postgresql.getMappedPort(5432))
      .put("database", POSTGRESQL_DATABASE)
      .put("username", POSTGRESQL_USERNAME)
      .put("password", POSTGRESQL_PASSWORD)));
  }

  @Test
  public void testPreferSslConfigurationOnNoneSSLInstance(TestContext context) {
    Async async = context.async();

    SQLClient clientSsl = createClient(vertx, new JsonObject().put("sslMode", "prefer"));

    clientSsl.getConnection(sqlConnectionAsyncResult -> {
      context.assertTrue(sqlConnectionAsyncResult.succeeded());
      conn = sqlConnectionAsyncResult.result();
      conn.query("SELECT 1", ar -> {
        if (ar.failed()) {
          context.fail("Should not fail on ssl connection");
        } else {
          async.complete();
        }
      });
    });
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
