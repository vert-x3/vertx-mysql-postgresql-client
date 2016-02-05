package io.vertx.ext.asyncsql;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public abstract class AbstractTestBase {

  protected AsyncSQLClient client;
  protected static Vertx vertx;
  protected SQLConnection conn;

  @BeforeClass
  public static void setUp() {
    vertx = Vertx.vertx();
  }

  @AfterClass
  public static void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @After
  public void cleanup(TestContext context) {
    if (conn != null) {
      conn.close(context.asyncAssertSuccess());
    }
    if (client != null) {
      client.close(context.asyncAssertSuccess());
    }
  }

  protected void ensureSuccess(TestContext context, AsyncResult result) {
    if (result.failed()) {
      context.fail(result.cause());
    }
  }

}
