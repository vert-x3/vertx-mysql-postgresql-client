/*
 *  Copyright 2015 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.asyncsql;


import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.SQLRowStream;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public abstract class SQLTestBase extends AbstractTestBase {

  static final String POSTGRESQL_DATABASE = "testdb";
  static final String POSTGRESQL_USERNAME = "vertx";
  static final String POSTGRESQL_PASSWORD = "password";

  static final String MYSQL_DATABASE = "testdb";
  static final String MYSQL_USERNAME = "vertx";
  static final String MYSQL_PASSWORD = "password";



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
            .put("numColumns", 1)
            .put("numRows", 1)
            .put("rows", new JsonArray().add(new JsonObject().put("something", 1)))
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
      setupSimpleTable(conn, ar2 -> conn.queryWithParams("SELECT name FROM test_table WHERE id=?",
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
        }));
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

  // note that mysql and the DDL of the table bellow do not take the TZ in consideration
  public static final String insertedTime1 = "2015-02-22T07:15:01.234";
  public static final String insertedTime2 = "2014-06-27T17:50:02.468";

  /**
   * @return the String form of the time returned for "2015-02-22T07:15:01.234".
   */
  public abstract String getExpectedTime1();

  /**
   * @return the String form of the time returned for "2014-06-27T17:50:02.468".
   */
  public abstract String getExpectedTime2();

  @Test
  public void testDateValueSelection(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      setSqlModeIfPossible(nothing -> {
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
                  context.assertEquals(row1.getString(2), getExpectedTime1());
                  JsonArray row2 = results.getResults().get(1);
                  context.assertEquals(row2.getString(1), "2007-07-20");
                  context.assertEquals(row2.getString(2), getExpectedTime2());

                  async.complete();
                });
              });
            });
          });
        });
      });
    });
  }

  @Test
  public void testDecimalFields(TestContext context) {
    Async async = context.async();
    client.getConnection(arConn -> {
      ensureSuccess(context, arConn);
      conn = arConn.result();
      conn.execute("DROP TABLE IF EXISTS test_table", arDrop -> {
        ensureSuccess(context, arDrop);
        conn.execute("CREATE TABLE test_table (id INT, some_decimal DECIMAL(65,6))", arCreate -> {
          ensureSuccess(context, arCreate);
          conn.execute("INSERT INTO test_table (id, some_decimal) VALUES " +
            "(1, 43210987654321098765432109876543210987654321098765432109871.123451)," +
            "(2, 43210987654321098765432109876543210987654321098765432109872.123452)," +
            "(3, 43210987654321098765432109876543210987654321098765432109873.123453)", arInsert -> {
            ensureSuccess(context, arInsert);
            conn.query("SELECT some_decimal FROM test_table ORDER BY id", arQuery -> {
              ensureSuccess(context, arQuery);
              ResultSet res = arQuery.result();
              context.assertEquals(new BigDecimal("43210987654321098765432109876543210987654321098765432109871.123451"), new BigDecimal(res.getRows().get(0).getString("some_decimal")));
              context.assertEquals(new BigDecimal("43210987654321098765432109876543210987654321098765432109872.123452"), new BigDecimal(res.getResults().get(1).getString(0)));
              context.assertEquals(new BigDecimal("43210987654321098765432109876543210987654321098765432109873.123453"), new BigDecimal(res.getRows().get(2).getString("some_decimal")));
              // This will convert both (big) numbers into a double which will loose some information
              context.assertEquals(43210987654321098765432109876543210987654321098765432109873.123453, Double.parseDouble(res.getRows().get(2).getString("some_decimal")));
              async.complete();
            });
          });
        });
      });
    });
  }

  protected abstract String createByteArray1TableColumn();

  protected abstract String createByteArray2TableColumn();

  protected abstract String createByteArray3TableColumn();

  protected abstract String[] insertByteArray1Values();

  protected abstract String[] insertByteArray2Values();

  protected abstract String[] insertByteArray3Values();

  @Test
  public void testByteA1Fields(TestContext context) {
    Async async = context.async();
    client.getConnection(arConn -> {
      ensureSuccess(context, arConn);
      conn = arConn.result();
      conn.execute("DROP TABLE IF EXISTS test_table", arDrop -> {
        ensureSuccess(context, arDrop);
        conn.execute("CREATE TABLE test_table (id INT, some_bit " + createByteArray1TableColumn() + ")", arCreate -> {
          ensureSuccess(context, arCreate);
          String[] s = insertByteArray1Values();
          conn.execute("INSERT INTO test_table (id, some_bit) VALUES (1, " + s[0] + "),(2, " + s[1] + "),(3, " + s[2] + ")", arInsert -> {
            ensureSuccess(context, arInsert);
            conn.query("SELECT some_bit FROM test_table ORDER BY id", arQuery -> {
              ensureSuccess(context, arQuery);
              ResultSet res = arQuery.result();
              context.assertTrue(Arrays.equals(new byte[]{0b1}, res.getRows().get(0).getBinary("some_bit")));
              context.assertTrue(Arrays.equals(new byte[]{0b0}, res.getResults().get(1).getBinary(0)));
              context.assertTrue(Arrays.equals(new byte[]{0b1}, res.getRows().get(2).getBinary("some_bit")));
              async.complete();
            });
          });
        });
      });
    });
  }

  @Test
  public void testByteA2Fields(TestContext context) {
    Async async = context.async();
    client.getConnection(arConn -> {
      ensureSuccess(context, arConn);
      conn = arConn.result();
      conn.execute("DROP TABLE IF EXISTS test_table", arDrop -> {
        ensureSuccess(context, arDrop);
        conn.execute("CREATE TABLE test_table (id INT, some_bit " + createByteArray2TableColumn() + ")", arCreate -> {
          ensureSuccess(context, arCreate);
          String[] s = insertByteArray2Values();
          conn.execute("INSERT INTO test_table (id, some_bit) VALUES (1, " + s[0] + "),(2, " + s[1] + "),(3, " + s[2] + ")", arInsert -> {
            ensureSuccess(context, arInsert);
            conn.query("SELECT some_bit FROM test_table ORDER BY id", arQuery -> {
              ensureSuccess(context, arQuery);
              ResultSet res = arQuery.result();
              context.assertTrue(Arrays.equals(new byte[]{0b10}, res.getRows().get(0).getBinary("some_bit")));
              context.assertTrue(Arrays.equals(new byte[]{0b01}, res.getResults().get(1).getBinary(0)));
              context.assertTrue(Arrays.equals(new byte[]{0b11}, res.getRows().get(2).getBinary("some_bit")));
              async.complete();
            });
          });
        });
      });
    });
  }

  @Test
  public void testByteA3Fields(TestContext context) {
    Async async = context.async();
    client.getConnection(arConn -> {
      ensureSuccess(context, arConn);
      conn = arConn.result();
      conn.execute("DROP TABLE IF EXISTS test_table", arDrop -> {
        ensureSuccess(context, arDrop);
        conn.execute("CREATE TABLE test_table (id INT, some_bit " + createByteArray3TableColumn() + ")", arCreate -> {
          ensureSuccess(context, arCreate);
          String[] s = insertByteArray3Values();
          conn.execute("INSERT INTO test_table (id, some_bit) VALUES (1, " + s[0] + "),(2, " + s[1] + "),(3, " + s[2] + ")", arInsert -> {
            ensureSuccess(context, arInsert);
            conn.query("SELECT some_bit FROM test_table ORDER BY id", arQuery -> {
              ensureSuccess(context, arQuery);
              ResultSet res = arQuery.result();
              context.assertTrue(Arrays.equals(new byte[]{0b1, 0b0}, res.getRows().get(0).getBinary("some_bit")));
              context.assertTrue(Arrays.equals(new byte[]{0b0, 0b1}, res.getResults().get(1).getBinary(0)));
              context.assertTrue(Arrays.equals(new byte[]{0b1, 0b1}, res.getRows().get(2).getBinary("some_bit")));
              async.complete();
            });
          });
        });
      });
    });
  }

  @Test
  public void testInvalidInsertStatement(TestContext context) {
    Async async = context.async();

    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      conn.updateWithParams("INVALID INSERT", new JsonArray(), ar2 -> {
        if (ar2.failed() && ar2.cause() instanceof com.github.mauricio.async.db.exceptions.DatabaseException) {
          async.complete();
        } else {
          context.fail("Should receive an exception of type DatabaseException.");
        }
      });
    });
  }

  // Does not pass with MySQL 5.6
  @Test
  public void testInstant(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      conn.execute("DROP TABLE IF EXISTS test_table", ar1 -> {
        ensureSuccess(context, ar1);
        conn.execute("CREATE TABLE test_table (instant TIMESTAMP)", ar2 -> {
          ensureSuccess(context, ar2);
          JsonArray args = new JsonArray().add(Instant.now());
          String now = args.getString(0);
          conn.queryWithParams("INSERT INTO test_table (instant) VALUES (?)", args, ar3 -> {
            ensureSuccess(context, ar3);
            conn.query("SELECT instant FROM test_table", ar4 -> {
              ensureSuccess(context, ar4);
              // timestamps with out time zone are returned as strings, so we must compare to the original instant
              // ignoring the timezone offset (meaning ignore everything after char 23)
              compareInstantStrings(
                context,
                ar4.result().getResults().get(0).getString(0),
                now.substring(0, 23)
              );
              async.complete();
            });
          });
        });
      });
    });
  }

  protected void compareInstantStrings(TestContext context, String result, String expected) {
    context.assertEquals(result, expected);
  }

  protected void setSqlModeIfPossible(Handler<Void> handler) {
    handler.handle(null);
  }

  protected void checkConsistency(TestContext context, Async async, SQLConnection conn, long id1, String name1, long id2, String name2) {
    conn.queryWithParams("SELECT name FROM test_table WHERE id = ?", new JsonArray().add(id1), ar5 -> {
      ensureSuccess(context, ar5);
      ResultSet resultSet1 = ar5.result();
      context.assertEquals(name1, resultSet1.getResults().get(0).getString(0));
      conn.queryWithParams("SELECT name FROM test_table WHERE id = ?", new JsonArray().add(id2), ar6 -> {
        ensureSuccess(context, ar6);
        ResultSet resultSet2 = ar6.result();
        context.assertNotEquals(id1, id2);
        context.assertEquals(name2, resultSet2.getResults().get(0).getString(0));
        async.complete();
      });
    });
  }

  private void setupSimpleTable(SQLConnection conn, Handler<AsyncResult<Void>> handler) {
    conn.execute("BEGIN",
      ar -> conn.execute("DROP TABLE IF EXISTS test_table",
        ar2 -> conn.execute(CREATE_TABLE_STATEMENT,
          ar3 -> conn.update("INSERT INTO test_table (id, name) VALUES " + Data.get(),
            ar4 -> conn.execute("COMMIT", handler)))));
  }

  private static final String CREATE_TABLE_STATEMENT = "CREATE TABLE test_table " +
    "(id BIGINT, name VARCHAR(255))";

  @Test
  public void testSimpleStream(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      if (ar.failed()) {
        context.fail(ar.cause());
        return;
      }

      // Create table
      conn = ar.result();
      setupSimpleTable(conn, ar2 -> {

        conn.queryStream("SELECT name, id FROM test_table ORDER BY name ASC", ar3 -> {
          if (ar3.failed()) {
            context.fail(ar3.cause());
          } else {
            final SQLRowStream res = ar3.result();
            context.assertNotNull(res);

            final AtomicInteger count = new AtomicInteger();

            res
              .handler(row -> {
                context.assertEquals(Data.NAMES.get(count.getAndIncrement()), row.getString(0));
              })
              .endHandler(v -> {
                context.assertEquals(Data.NAMES.size(), count.get());
                async.complete();
              });
          }
        });
      });
    });
  }

  private void setupTestTable(SQLConnection conn, Supplier<String> idNameValuesSupplier, Handler<AsyncResult<Void>> handler) {
    conn.execute("BEGIN",
      ar -> conn.execute("DROP TABLE IF EXISTS test_table",
        ar2 -> conn.execute(CREATE_TABLE_STATEMENT,
          ar3 -> conn.update("INSERT INTO test_table (id, name) VALUES " + idNameValuesSupplier.get(),
            ar4 -> conn.execute("COMMIT", handler)))));
  }

  @Test
  public void testColumnsFromStream(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      if (ar.failed()) {
        context.fail(ar.cause());
        return;
      }

      // Create table
      conn = ar.result();
      setupTestTable(conn, () -> "(1,'NOTHING')", ar2 -> {
        conn.queryStream("SELECT name, id FROM test_table ORDER BY name ASC", ar3 -> {
          if (ar3.failed()) {
            context.fail(ar3.cause());
          } else {
            final SQLRowStream res = ar3.result();
            context.assertNotNull(res);

            // assert that we have columns and they are valid
            assertNotNull(res.columns());
            assertEquals(Arrays.asList("name", "id"), res.columns());

            // assert the collection is immutable
            try {
              res.columns().add("durp!");
              fail();
            } catch (RuntimeException e) {
              // expected!
            }
          }
          async.complete();
        });
      });
    });
  }

  @Test
  public void testLongStream(TestContext context) {
    final int NUM_ROWS = 10000;
    Async async = context.async();
    client.getConnection(ar -> {
      if (ar.failed()) {
        context.fail(ar.cause());
        return;
      }

      // Create and populate table
      conn = ar.result();

      Map<Integer, String> data = new HashMap<>();
      for (int i=0; i<NUM_ROWS; i++) {
        data.put(i, Integer.toString(i));
      }

      setupTestTable(conn, () -> {
          StringBuilder builder = new StringBuilder();
          for (int i=0; i<NUM_ROWS; i++) {
            if (i > 0) {
              builder.append(",");
            }
            builder.append("(")
              .append(i)
              .append(",'")
              .append(data.get(i))
              .append("')");
          }
          return builder.toString();
        }, ar2 -> {

        conn.queryStream("SELECT id, name FROM test_table ORDER BY id ASC", ar3 -> {
          if (ar3.failed()) {
            context.fail(ar3.cause());
          } else {
            final SQLRowStream res = ar3.result();
            context.assertNotNull(res);

            final AtomicInteger count = new AtomicInteger();

            res
              .handler(row -> {
                int rowNum = count.getAndIncrement();
                context.assertEquals(rowNum, row.getInteger(0));
                context.assertEquals(data.get(rowNum), row.getString(1));

              })
              .endHandler(v -> {
                context.assertEquals(NUM_ROWS, count.get());
                async.complete();
              });
          }
        });
      });
    });
  }

  private static class UserDefinedException extends RuntimeException {
  }

  @Test
  public void testUncaughtException(TestContext testContext) {
    Async async = testContext.async();
    Context context = vertx.getOrCreateContext();
    context.exceptionHandler(throwable -> {
      assertThat(throwable, instanceOf(UserDefinedException.class));
      async.complete();
    });
    context.runOnContext(v -> {
      client.getConnection(connection -> {
        try {
          throw new UserDefinedException();
        } finally {
          if (connection.succeeded()) {
            connection.result().close();
          }
        }
      });
    });
  }

  @Test
  public void testUnavailableDatabase(TestContext testContext) {
    Async async = testContext.async(3);

    Handler<AsyncResult<SQLConnection>> handler = new Handler<AsyncResult<SQLConnection>>() {
      @Override
      public void handle(AsyncResult<SQLConnection> sqlConnectionAsyncResult) {
        testContext.assertFalse(sqlConnectionAsyncResult.succeeded());
        async.countDown();
      }
    };
    clientNoDatabase.getConnection(handler);
    clientNoDatabase.getConnection(handler);
    clientNoDatabase.getConnection(handler);
  }

  @Test
  public void testTimeColumn(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      conn.execute("DROP TABLE IF EXISTS test_table", ar1 -> {
        ensureSuccess(context, ar1);
        conn.execute("CREATE TABLE test_table (timecolumn TIME)", ar2 -> {
          ensureSuccess(context, ar2);
          String someTime1 = "11:12:13.456";
          String someTime2 = "01:02:00.120";
          String someTime3 = "00:00:01.001";
          JsonArray args = new JsonArray().add(someTime1).add(someTime2).add(someTime3);
          conn.queryWithParams("INSERT INTO test_table (timecolumn) VALUES (?), (?), (?)", args, ar3 -> {
            ensureSuccess(context, ar3);
            conn.query("SELECT timecolumn FROM test_table", ar4 -> {
              ensureSuccess(context, ar4);
              String result1 = ar4.result().getResults().get(0).getString(0);
              String result2 = ar4.result().getResults().get(1).getString(0);
              String result3 = ar4.result().getResults().get(2).getString(0);
              compareTimeStrings(context, result1, someTime1);
              compareTimeStrings(context, result2, someTime2);
              compareTimeStrings(context, result3, someTime3);
              async.complete();
            });
          });
        });
      });
    });
  }

  protected void compareTimeStrings(TestContext context, String result, String expected) {
    context.assertEquals(result, expected);
  }

  @Test
  public void testUnhandledExceptionInHandlerResultSet(TestContext testContext) {
    this.<ResultSet>testUnhandledExceptionInHandler(testContext, (sqlConnection, handler) -> {
      sqlConnection.query("SELECT name FROM test_table", handler);
    });
  }

  @Test
  public void testUnhandledExceptionInHandlerRowStream(TestContext testContext) {
    this.<SQLRowStream>testUnhandledExceptionInHandler(testContext, (sqlConnection, handler) -> {
      sqlConnection.queryStream("SELECT name FROM test_table", handler);
    });
  }

  @Test
  public void testUnhandledExceptionInHandlerUpdateResult(TestContext testContext) {
    this.<UpdateResult>testUnhandledExceptionInHandler(testContext, (sqlConnection, handler) -> {
      sqlConnection.update("INSERT INTO test_table (name) VALUES ('pimpo')", handler);
    });
  }

  private <T> void testUnhandledExceptionInHandler(TestContext testContext, BiConsumer<SQLConnection, Handler<AsyncResult<T>>> testMethod) {
    AtomicInteger count = new AtomicInteger();
    Async async = testContext.async();
    Context context = vertx.getOrCreateContext();
    context.exceptionHandler(t -> {
      async.complete();
    }).runOnContext(v -> {
      client.getConnection(testContext.asyncAssertSuccess(connection -> {
        setupSimpleTable(connection, testContext.asyncAssertSuccess(st -> {
          testMethod.accept(connection, ar -> {
            count.incrementAndGet();
            throw new RuntimeException();
          });
        }));
      }));
    });
    async.await(MILLISECONDS.convert(5, SECONDS));
    assertEquals(1, count.get());
  }
}
