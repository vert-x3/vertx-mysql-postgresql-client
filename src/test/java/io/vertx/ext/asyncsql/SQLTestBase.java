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

import java.util.List;

@RunWith(VertxUnitRunner.class)
public class SQLTestBase {

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

  @Test
  public void testMultipleConnections(TestContext context) {
    int id = 0;
    String name = "Adele";

    SQLConnection[] connections = new SQLConnection[2];

    Async async1 = context.async();
    Async async2 = context.async();
    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      setupSimpleTable(conn, ar2 -> {
        client.getConnection(ar3 -> {
          ensureSuccess(context, ar3);
          connections[0] = ar3.result();
          connections[0].setAutoCommit(false, ar31 -> {
            ensureSuccess(context, ar31);
            connections[0].updateWithParams("UPDATE test_table SET name=? WHERE id=?",
                new JsonArray().add(name).add(id), ar32 -> {
                  ensureSuccess(context, ar32);
                  context.assertEquals(ar32.result().getUpdated(), 1);
                  connections[0].rollback(ar33 -> {
                    ensureSuccess(context, ar33);
                    connections[0].close(v -> {
                      ensureSuccess(context, v);
                      async1.complete();
                    });
                  });
                });
          });
        });
        client.getConnection(ar4 -> {
          ensureSuccess(context, ar4);
          connections[1] = ar4.result();
          connections[1].query("SELECT name FROM test_table ORDER BY id", ar41 -> {
            ensureSuccess(context, ar41);
            ResultSet resultSet = ar41.result();
            context.assertEquals(resultSet.getResults().get(0).getString(0), "Albert");
            connections[1].close(v -> {
              ensureSuccess(context, v);
              async2.complete();
            });
          });
        });
      });
    });
  }

  @Test
  public void testSetAutocommitWhileInTransaction(TestContext context) {
    //TODO not sur it is the correct translation, need to be checked
    int id = 0;
    String name = "Adele";
    Async async = context.async();
    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      setupSimpleTable(conn, ar2 -> {
        ensureSuccess(context, ar2);
        client.getConnection(ar3 -> {
          ensureSuccess(context, ar3);
          SQLConnection conn1 = ar3.result();
          conn1.setAutoCommit(false, ar4 -> {
            ensureSuccess(context, ar4);
            conn1.updateWithParams("UPDATE test_table SET name=? WHERE id=?",
                new JsonArray().add(name).add(id),
                ar5 -> {
                  ensureSuccess(context, ar5);
                  conn1.setAutoCommit(true, ar6 -> {
                    ensureSuccess(context, ar6);
                    context.assertEquals(1, ar5.result().getUpdated());
                    client.getConnection(ar7 -> {
                      ensureSuccess(context, ar7);
                      SQLConnection conn2 = ar7.result();
                      conn2.query("SELECT name FROM test_table ORDER BY id", ar8 -> {
                        ensureSuccess(context, ar8);
                        context.assertEquals(ar8.result().getResults()
                            .get(0).getString(0), name);
                        conn2.close(v -> conn1.close(v2 -> async.complete()));
                      });
                    });
                  });
                });
          });
        });
      });
    });


  }

  @Test
  public void testRollingBackWhenNotInTransaction(TestContext context) {
    int id = 0;
    String name = "adele";
    Async async = context.async();
    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      setupSimpleTable(conn, ar2 -> {
        ensureSuccess(context, ar2);
        conn.setAutoCommit(false, ar3 -> {
          ensureSuccess(context, ar3);
          conn.updateWithParams("UPDATE test_table SET name=? WHERE id=?",
              new JsonArray().add(name).add(id), ar4 -> {
                ensureSuccess(context, ar4);
                conn.setAutoCommit(true, ar5 -> {
                  ensureSuccess(context, ar5);
                  conn.rollback(ar6 -> {
                    context.assertTrue(ar6.failed());
                    async.complete();
                  });
                });
              });
        });
      });
    });
  }

  @Test
  public void testCommitWhenNotInTransaction(TestContext context) {
    int id = 0;
    String name = "adele";
    Async async = context.async();
    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      setupSimpleTable(conn, ar2 -> {
        ensureSuccess(context, ar2);
        conn.setAutoCommit(false, ar3 -> {
          ensureSuccess(context, ar3);
          conn.updateWithParams("UPDATE test_table SET name=? WHERE id=?",
              new JsonArray().add(name).add(id), ar4 -> {
                ensureSuccess(context, ar4);
                conn.setAutoCommit(true, ar5 -> {
                  ensureSuccess(context, ar5);
                  conn.commit(ar6 -> {
                    context.assertTrue(ar6.failed());
                    async.complete();
                  });
                });
              });
        });
      });
    });
  }

  @Test
  public void testInsertion(TestContext context) {
    int id = 27;
    String name = "Adele";
    Async async = context.async();

    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      setupSimpleTable(conn, ar2 -> {
        ensureSuccess(context, ar2);
        conn.updateWithParams("INSERT INTO test_table (id, name) VALUES (?, ?)",
            new JsonArray().add(id).add(name), ar3 -> {
              ensureSuccess(context, ar3);
              conn.query("SELECT id, name FROM test_table ORDER BY id", ar4 -> {
                ensureSuccess(context, ar4);
                ResultSet resultSet = ar4.result();
                context.assertEquals("Adele",
                    resultSet.getResults().get(id - 1).getString(1));
                async.complete();
              });
            });
      });
    });
  }

  @Test
  public void testSelectionOfNullValues(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      conn.execute("DROP TABLE IF EXISTS test_nulls_table", ar2 -> {
        ensureSuccess(context, ar2);
        conn.execute("CREATE TABLE test_nulls_table (id BIGINT, name VARCHAR(255) NULL)",
            ar3 -> {
              ensureSuccess(context, ar3);
              conn.execute("INSERT INTO test_nulls_table (id, name) VALUES (1, NULL)", ar4 -> {
                ensureSuccess(context, ar4);
                conn.query("SELECT id, name FROM test_nulls_table ORDER BY id", ar5 -> {
                  ensureSuccess(context, ar5);
                  ResultSet rs = ar5.result();

                  List<String> columns = rs.getColumnNames();
                  context.assertEquals(2, columns.size());
                  context.assertEquals(columns.get(0), "id");
                  context.assertEquals(columns.get(1), "name");

                  context.assertEquals(rs.getResults().size(), 1);
                  context.assertEquals(rs.getResults().get(0).getInteger(0), 1);
                  context.assertEquals(rs.getResults().get(0).getString(1), null);
                  async.complete();
                });
              });
            });
      });
    });
  }

  public static final String insertedTime1 = "2015-02-22T07:15:01.234Z";

  public static final String expectedTime1 = "2015-02-22T07:15:01.";

  public static final String insertedTime2 = "2014-06-27T17:50:02.468+02:00";

  public static final String expectedTime2 = "2014-06-27T17:50:02.";


  @Test
  public void testDateValueSelection(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      conn.execute("DROP TABLE IF EXISTS test_date_table", ar2 -> {
        ensureSuccess(context, ar2);
        conn.execute("CREATE TABLE test_date_table (id BIGINT, some_date DATE,some_timestamp TIMESTAMP)", ar3 -> {
          ensureSuccess(context, ar3);
          conn.updateWithParams("INSERT INTO test_date_table (id, some_date, some_timestamp) VALUES (?, ?, ?)", new JsonArray().add(1).add("2015-02-22").add(insertedTime1), ar4 -> {
            ensureSuccess(context, ar4);
            conn.updateWithParams("INSERT INTO test_date_table (id, some_date, some_timestamp) VALUES (?, ?, ?)", new JsonArray().add(2).add("2007-07-20").add(insertedTime2), ar5 -> {
              ensureSuccess(context, ar5);
              conn.query("SELECT id, some_date, some_timestamp FROM test_date_table ORDER BY id", ar6 -> {
                ensureSuccess(context, ar6);
                ResultSet results = ar6.result();
                List<String> columns = results.getColumnNames();
                context.assertEquals(3, columns.size());
                context.assertEquals("id", columns.get(0));
                context.assertEquals("some_date", columns.get(1));
                context.assertEquals("some_timestamp", columns.get(2));

                context.assertEquals(2, results.getResults().size());
                JsonArray row1 = results.getResults().get(0);
                context.assertEquals(row1.getString(1), "2015-02-22");
                context.assertTrue(row1.getString(2).startsWith(expectedTime1));
                JsonArray row2 = results.getResults().get(1);
                context.assertEquals(row2.getString(1), "2007-07-20");
                context.assertTrue(row2.getString(2).startsWith(expectedTime2));

                async.complete();
              });
            });
          });
        });
      });
    });
  }

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
