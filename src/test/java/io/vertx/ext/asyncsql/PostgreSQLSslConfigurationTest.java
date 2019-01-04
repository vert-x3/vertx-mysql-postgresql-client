package io.vertx.ext.asyncsql;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.category.NeedsDocker;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

import static io.vertx.ext.asyncsql.SQLTestBase.*;
import static org.testcontainers.containers.BindMode.READ_ONLY;

/**
 * Tests the configuration options of the PostgreSQL client.
 */
@Category(NeedsDocker.class)
public class PostgreSQLSslConfigurationTest extends ConfigurationTest {

  public static GenericContainer postgresql = new PostgreSQLContainer()
    .withDatabaseName(POSTGRESQL_DATABASE)
    .withUsername(POSTGRESQL_USERNAME)
    .withPassword(POSTGRESQL_PASSWORD)
    .withExposedPorts(5432)
    .withClasspathResourceMapping("/ssl-docker/server.crt","/docker-entrypoint-initdb.d/server.crt", READ_ONLY)
    .withClasspathResourceMapping("/ssl-docker/server.key","/docker-entrypoint-initdb.d/server.key", READ_ONLY)
    .withClasspathResourceMapping("/ssl-docker/init.sh","/docker-entrypoint-initdb.d/init.sh", READ_ONLY);

  static {
    postgresql.start();
  }

  @Override
  protected SQLClient createClient(Vertx vertx, JsonObject config) {
    JsonObject json = new JsonObject()
      .put("host", postgresql.getContainerIpAddress())
      .put("port", postgresql.getMappedPort(5432))
      .put("database", POSTGRESQL_DATABASE)
      .put("username", POSTGRESQL_USERNAME)
      .put("password", POSTGRESQL_PASSWORD)
      .put("sslMode", "prefer").mergeIn(config.copy());
    System.out.println("Creating client " + json.toString());
    return PostgreSQLClient.createNonShared(vertx, json);
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

  @Test
  public void testCorrectSslConfiguration(TestContext context) {
    Async async = context.async();
    String path = getClass()
      .getResource("/ssl-docker/server.crt")
      .getPath();

    JsonObject sslConfig = new JsonObject()
      .put("sslMode", "require")
      .put("sslRootCert", path);

    client = createClient(vertx, sslConfig);

    client.getConnection(sqlConnectionAsyncResult -> {
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

  @Test
  public void testWrongSslConfiguration(TestContext context) {
    Async async = context.async();
    client = createClient(vertx,new JsonObject()
        .put("sslMode", "verify-ca")
        .put("sslRootCert", "something-wrong.crt")
    );

    client.getConnection(sqlConnectionAsyncResult -> {
      context.assertTrue(sqlConnectionAsyncResult.failed());
      async.complete();
    });
  }

  @Test
  public void testPreferSslConfiguration(TestContext context) {
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
}
