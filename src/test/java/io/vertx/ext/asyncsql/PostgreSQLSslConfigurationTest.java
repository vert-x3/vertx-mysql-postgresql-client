package io.vertx.ext.asyncsql;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Tests the configuration options of the PostgreSQL client.
 */
public class PostgreSQLSslConfigurationTest extends ConfigurationTest {

  @Before
  public void areSslTestsIgnored() {
    Assume.assumeFalse(Boolean.getBoolean("skipPostgresSslConfigTest"));
  }

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

  @Test
  public void testCorrectSslConfiguration(TestContext context) {
    Async async = context.async();
    String path = getClass()
      .getResource("/ssl-docker/server.crt")
      .getPath();

    JsonObject sslConfig = new JsonObject()
      .put("port", Integer.parseInt(System.getProperty("dbssl.port", "54321")))
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
    client = createClient(vertx,
      new JsonObject()
        .put("host", System.getProperty("db.host", "localhost"))
        .put("port", Integer.parseInt(System.getProperty("dbssl.port", "54321")))
        .put("sslMode", "verify-ca")
        .put("sslRootCert", "something-wrong.crt")
    );

    client.getConnection(sqlConnectionAsyncResult -> {
      context.assertTrue(sqlConnectionAsyncResult.failed());
      async.complete();
    });
  }

  @Test
  public void testNoSslConfiguration(TestContext context) {
    Async async = context.async();
    client = createClient(vertx,
      new JsonObject()
        .put("host", System.getProperty("db.host", "localhost"))
        .put("port", Integer.parseInt(System.getProperty("dbssl.port", "54321")))
    );

    client.getConnection(sqlConnectionAsyncResult -> {
      context.assertTrue(sqlConnectionAsyncResult.failed());
      async.complete();
    });
  }


  @Test
  public void testPreferSslConfiguration(TestContext context) {
    Async async = context.async();
    SQLClient clientSsl = createClient(vertx,
      new JsonObject()
        .put("host", System.getProperty("db.host", "localhost"))
        .put("port", Integer.parseInt(System.getProperty("dbssl.port", "54321")))
        .put("sslMode", "prefer")
    );
    SQLClient clientNoSsl = createClient(vertx,
      new JsonObject()
        .put("host", System.getProperty("db.host", "localhost"))
        .put("port", Integer.parseInt(System.getProperty("dbssl.port", "54321")))
        .put("sslMode", "prefer")
    );

    clientSsl.getConnection(sqlConnectionAsyncResult -> {
      context.assertTrue(sqlConnectionAsyncResult.succeeded());
      conn = sqlConnectionAsyncResult.result();
      conn.query("SELECT 1", ar -> {
        if (ar.failed()) {
          context.fail("Should not fail on ssl connection");
        } else {
          clientNoSsl.getConnection(sqlConnectionAsyncResult2 -> {
            context.assertTrue(sqlConnectionAsyncResult2.succeeded());
            conn = sqlConnectionAsyncResult2.result();
            conn.query("SELECT 1", ar2 -> {
              if (ar2.failed()) {
                context.fail("Should not fail on non-ssl connection");
              } else {
                async.complete();
              }
            });
          });
        }
      });
    });
  }
}
