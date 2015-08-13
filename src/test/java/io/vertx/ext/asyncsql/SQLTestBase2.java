package io.vertx.ext.asyncsql;


import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class SQLTestBase2 {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

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

  @Test
  @Ignore
  public void testSimpleConnection(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      if (ar.failed()) {
        context.fail(ar.cause());
        return;
      }
      conn = ar.result();
      conn.query("SELECT 1 AS something", ar2 -> {
        if (ar2.failed()) {
          context.fail(ar2.cause());
        } else {
          ResultSet result = ar2.result();
          context.assertNotNull(result);
          JsonObject expected = new JsonObject()
              .put("columnNames", new JsonArray().add("something"))
              .put("results", new JsonArray().add(new JsonArray().add(1)));
          context.assertEquals(expected, result.toJson());
          async.complete();
        }
      });
    });
  }

  @Test
  public void testSimpleSelect(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      if (ar.failed()) {
        context.fail(ar.cause());
        return;
      }

      // Create table
      conn = ar.result();
      setupSimpleTable(ar.result(), ar2 -> {
        conn.queryWithParams("SELECT name FROM test_table WHERE id=?",
            new JsonArray().add(2), ar3 -> {
              if (ar3.failed()) {
                context.fail(ar3.cause());
              } else {
                final ResultSet res = ar3.result();
                context.assertNotNull(res);
                context.assertEquals(res.getColumnNames().size(), 1);
                context.assertEquals(res.getColumnNames().get(0), "name");
                context.assertEquals(Data.NAMES.get(2), res.getResults().get(0).getString(0));
                async.complete();
              }
            });
      });
    });
  }


  @Test
  public void testTwoTransactionsAfterEachOther(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      if (ar.failed()) {
        context.fail(ar.cause());
        return;
      }

      conn = ar.result();
      conn.setAutoCommit(false, ar2 -> {
        ensureSuccess(context, ar2);
        conn.query("SELECT 1", ar3 -> {
          ensureSuccess(context, ar3);
          conn.commit(ar4 -> {
            ensureSuccess(context, ar4);
            conn.query("SELECT 2", ar5 -> {
              ensureSuccess(context, ar5);
              conn.commit(ar6 -> {
                ensureSuccess(context, ar6);
                conn.setAutoCommit(true, ar7 -> {
                  ensureSuccess(context, ar7);
                  async.complete();
                });
              });
            });
          });
        });
      });
    });
  }


  protected void ensureSuccess(TestContext context, AsyncResult result) {
    if (result.failed()) {
      context.fail(result.cause());
    }
  }

  @Test
  public void testUpdatingRow(TestContext context) {
    int id = 0;
    String name = "Adele";

    Async async = context.async();
    client.getConnection(ar -> {
      if (ar.failed()) {
        context.fail(ar.cause());
        return;
      }

      conn = ar.result();
      setupSimpleTable(conn, ar2 -> {
        ensureSuccess(context, ar2);
        conn.updateWithParams("UPDATE test_table SET name=? WHERE id=?",
            new JsonArray().add(name).add(id),
            ar3 -> {
              ensureSuccess(context, ar3);
              UpdateResult updateRes = ar3.result();
              conn.query("SELECT name FROM test_table ORDER BY id", ar4 -> {
                ensureSuccess(context, ar4);
                ResultSet selectRes = ar4.result();
                context.assertNotNull(updateRes);
                context.assertNotNull(selectRes);
                context.assertEquals(1, updateRes.getUpdated());
                context.assertEquals("Adele", selectRes.getResults().get(0).getString(0));
                async.complete();
              });
            });
      });
    });

  }


  @Test
  public void testRollback(TestContext context) {
    int id = 0;
    String name = "Adele";

    Async async = context.async();
    client.getConnection(ar -> {
      if (ar.failed()) {
        context.fail(ar.cause());
        return;
      }

      conn = ar.result();
      setupSimpleTable(conn, ar2 -> {
        ensureSuccess(context, ar2);
        conn.setAutoCommit(false, ar3 -> {
          ensureSuccess(context, ar3);
          conn.updateWithParams("UPDATE test_table SET name=? WHERE id=?",
              new JsonArray().add(name).add(id),
              ar4 -> {
                ensureSuccess(context, ar4);
                UpdateResult updateRes = ar4.result();
                conn.rollback(ar5 -> {
                  ensureSuccess(context, ar5);
                  conn.query("SELECT name FROM test_table ORDER BY id", ar6 -> {
                    ensureSuccess(context, ar6);
                    ResultSet selectRes = ar6.result();
                    context.assertNotNull(updateRes);
                    context.assertNotNull(selectRes);
                    context.assertEquals(1, updateRes.getUpdated());
                    context.assertEquals("Albert",
                        selectRes.getResults().get(0).getString(0));
                    async.complete();
                  });
                });
              });
        });
      });
    });
  }

  /*

   */

  private void setupSimpleTable(SQLConnection conn, Handler<AsyncResult<Void>> handler) {
    conn.execute("BEGIN",
        ar -> conn.execute("DROP TABLE IF EXISTS test_table",
            ar2 -> conn.execute(CREATE_TABLE_STATEMENT,
                ar3 -> conn.update("INSERT INTO test_table (id, name) VALUES " + Data.get(),
                    ar4 -> conn.execute("COMMIT", handler::handle)))));
  }


  private static final String CREATE_TABLE_STATEMENT = "CREATE TABLE test_table " +
      "(id BIGINT, name VARCHAR(255))";

}
