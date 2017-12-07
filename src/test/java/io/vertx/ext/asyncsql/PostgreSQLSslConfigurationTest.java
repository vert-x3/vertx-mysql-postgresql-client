package io.vertx.ext.asyncsql;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.List;

/**
 * Tests the configuration options of the PostgreSQL client.
 */
public class PostgreSQLSslConfigurationTest extends ConfigurationTest {

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

    System.out.println("Path = " + path);

    JsonObject sslConfig = new JsonObject()
      .put("port", Integer.parseInt(System.getProperty("dbssl.port", "54321")))
      .put("sslMode", "require")
      .put("sslRootCert", path);

    client = createClient(vertx, sslConfig);

    System.out.println("testCorrectSslConfiguration");
    client.getConnection(sqlConnectionAsyncResult -> {
      context.assertTrue(sqlConnectionAsyncResult.succeeded());
      conn = sqlConnectionAsyncResult.result();
      System.out.println("testCorrectSslConfiguration step2");
      conn.query("SELECT 1", ar -> {
        System.out.println("testCorrectSslConfiguration callback2");
        if (ar.failed()) {
          context.fail("Should not fail on ssl connection");
        } else {
          System.out.println("testCorrectSslConfiguration all good!");
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

    System.out.println("testWrongSslConfiguration");
    client.getConnection(sqlConnectionAsyncResult -> {
      System.out.println("testWrongSslConfiguration callback");
      context.assertTrue(sqlConnectionAsyncResult.failed());
      System.out.println("testWrongSslConfiguration success!");
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

    System.out.println("testNoSslConfiguration");
    client.getConnection(sqlConnectionAsyncResult -> {
      System.out.println("testNoSslConfiguration callback");
      context.assertTrue(sqlConnectionAsyncResult.failed());
      System.out.println("testNoSslConfiguration success!");
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

    System.out.println("testPreferSslConfiguration");

    clientSsl.getConnection(sqlConnectionAsyncResult -> {
      context.assertTrue(sqlConnectionAsyncResult.succeeded());
      conn = sqlConnectionAsyncResult.result();
      System.out.println("testPreferSslConfiguration step2");
      conn.query("SELECT 1", ar -> {
        System.out.println("testPreferSslConfiguration callback2");
        if (ar.failed()) {
          context.fail("Should not fail on ssl connection");
        } else {
          System.out.println("testPreferSslConfiguration SSL OK");

          clientNoSsl.getConnection(sqlConnectionAsyncResult2 -> {
            context.assertTrue(sqlConnectionAsyncResult2.succeeded());
            conn = sqlConnectionAsyncResult2.result();
            System.out.println("testPreferSslConfiguration step3");
            conn.query("SELECT 1", ar2 -> {
              System.out.println("testPreferSslConfiguration callback4");
              if (ar2.failed()) {
                context.fail("Should not fail on non-ssl connection");
              } else {
                System.out.println("testPreferSslConfiguration non-SSL OK");
                System.out.println("testPreferSslConfiguration all good!");
                async.complete();
              }
            });
          });
        }
      });
    });
  }
}
