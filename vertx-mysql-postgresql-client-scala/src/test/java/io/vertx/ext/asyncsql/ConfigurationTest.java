package io.vertx.ext.asyncsql;

import com.github.mauricio.async.db.exceptions.ConnectionTimeoutedException;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeoutException;

public abstract class ConfigurationTest extends AbstractTestBase {

  protected abstract SQLClient createClient(Vertx vertx, JsonObject config);

  protected abstract String sleepCommand(int seconds);

  protected abstract String getEncodingStatement();

  protected abstract String getEncodingValueFromResults(List<JsonArray> results);

  @Test
  public void testCharset(TestContext context) {
    Async async = context.async();
    showEncoding(context, "iso-8859-1", encoding1 -> {
      showEncoding(context, "utf-8", encoding2 -> {
        context.assertNotEquals(encoding1, encoding2);
        async.complete();
      });
    });
  }

  private void showEncoding(TestContext context, String charSetString, Handler<String> encodingHandler) {

    client = createClient(vertx,
        new JsonObject()
            .put("charset", charSetString)
    );

    client.getConnection(sqlConnectionAsyncResult -> {
      ensureSuccess(context, sqlConnectionAsyncResult);
      conn = sqlConnectionAsyncResult.result();
      conn.query(getEncodingStatement(), showEncodingAr -> {
        ensureSuccess(context, showEncodingAr);
        String encoding = getEncodingValueFromResults(showEncodingAr.result().getResults());
        conn.close(connCloseAr -> {
          ensureSuccess(context, connCloseAr);
          conn = null;

          client.close(clientCloseAr -> {
            ensureSuccess(context, clientCloseAr);
            client = null;

            encodingHandler.handle(encoding);

          });
        });
      });
    });
  }

  @Ignore("Not implemented in driver yet, see https://github.com/mauricio/postgresql-async/issues/6")
  @Test
  public void testConnectionTimeout(TestContext context) {
    Async async = context.async();
    client = createClient(vertx,
        new JsonObject()
            .put("connectTimeout", Long.parseLong(System.getProperty("db.connectTimeout", "1")))
    );

    client.getConnection(sqlConnectionAsyncResult -> {
      if (sqlConnectionAsyncResult.failed()) {
        context.assertTrue(sqlConnectionAsyncResult.cause() instanceof ConnectionTimeoutedException);
        async.complete();
      } else {
        context.fail("Should fail due to a connection timeout exception");
      }
    });
  }

  @Test
  public void testQueryTimeout(TestContext context) {
    Async async = context.async();
    client = createClient(vertx,
        new JsonObject()
            .put("queryTimeout", Long.parseLong(System.getProperty("db.queryTimeout", "1")))
    );

    client.getConnection(sqlConnectionAsyncResult -> {
      conn = sqlConnectionAsyncResult.result();
      conn.query("SELECT " + sleepCommand(2), ar -> {
        if (ar.failed()) {
          context.assertTrue(ar.cause() instanceof TimeoutException);
          async.complete();
        } else {
          context.fail("Should fail due to a connection timeout exception");
        }
      });
    });
  }

}
