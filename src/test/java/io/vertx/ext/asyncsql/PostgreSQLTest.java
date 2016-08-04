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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import java.time.Instant;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
public class PostgreSQLTest extends AbstractTestBase {

  final JsonObject config = new JsonObject()
      .put("host", System.getProperty("db.host", "localhost"));

  @Before
  public void init() {
    client = PostgreSQLClient.createNonShared(vertx, config);
  }

  @Test
  public void someTest(TestContext context) throws Exception {
    Async async = context.async();
    client.getConnection(connAr -> {
      ensureSuccess(context, connAr);
      conn = connAr.result();
      conn.query("SELECT 1 AS something", resultSetAr -> {
        ensureSuccess(context, resultSetAr);
        ResultSet resultSet = resultSetAr.result();
        context.assertNotNull(resultSet);
        context.assertNotNull(resultSet.getColumnNames());
        context.assertNotNull(resultSet.getResults());
        context.assertEquals(new JsonArray().add(1), resultSet.getResults().get(0));
        async.complete();
      });
    });
  }

  @Test
  public void queryTypeTimestampWithTimezoneTest(TestContext context) throws Exception {
    Async async = context.async();
    client.getConnection(connAr -> {
      ensureSuccess(context, connAr);
      conn = connAr.result();
      conn.execute("DROP TABLE IF EXISTS test_table", onSuccess(context, dropped -> {
        conn.execute("CREATE TABLE IF NOT EXISTS test_table (ts timestamp with time zone)", onSuccess(context, created -> {
          conn.execute("INSERT INTO test_table (ts) VALUES (now())", onSuccess(context, inserted -> {
            conn.query("SELECT * FROM test_table;", onSuccess(context, timestampSelect -> {
              context.assertNotNull(timestampSelect);
              context.assertNotNull(timestampSelect.getResults());
              context.assertTrue(timestampSelect.getResults().get(0).getString(0).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}(Z|[+-]\\d{2}:\\d{2})"));
              async.complete();
            }));
          }));
        }));
      }));
    });
  }

  @Test
  public void queryTypeTimestampWithoutTimezoneTest(TestContext context) throws Exception {
    Async async = context.async();
    client.getConnection(connAr -> {
      ensureSuccess(context, connAr);
      conn = connAr.result();
      conn.execute("DROP TABLE IF EXISTS test_table", onSuccess(context, dropped -> {
        conn.execute("CREATE TABLE IF NOT EXISTS test_table (ts timestamp without time zone)", onSuccess(context, created -> {
          conn.execute("INSERT INTO test_table (ts) VALUES (now())", onSuccess(context, inserted -> {
            conn.query("SELECT * FROM test_table;", onSuccess(context, timestampSelect -> {
              context.assertNotNull(timestampSelect);
              context.assertNotNull(timestampSelect.getResults());
              context.assertTrue(timestampSelect.getResults().get(0).getString(0).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}"));
              async.complete();
            }));
          }));
        }));
      }));
    });
  }

  @Test
  public void testUpdatingNumericField(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      conn.execute("DROP TABLE IF EXISTS test_table", ar1 -> {
        ensureSuccess(context, ar1);
        conn.execute("CREATE TABLE test_table (id BIGSERIAL, numcol NUMERIC)", ar2 -> {
          ensureSuccess(context, ar2);
          conn.query("INSERT INTO test_table DEFAULT VALUES RETURNING id", ar3 -> {
            ensureSuccess(context, ar3);
            System.out.println("result: " + ar3.result().toJson().encode());
            final long id = ar3.result().getResults().get(0).getLong(0);
            conn.updateWithParams("UPDATE test_table SET numcol = ? WHERE id = ?", new JsonArray().add(1234).add(id), ar4 -> {
              ensureSuccess(context, ar4);
              conn.updateWithParams("UPDATE test_table SET numcol = ? WHERE id = ?", new JsonArray().addNull().add(id), ar5 -> {
                ensureSuccess(context, ar5);
                conn.updateWithParams("UPDATE test_table SET numcol = ? WHERE id = ?", new JsonArray().add(123.123).add(id), ar6 -> {
                  ensureSuccess(context, ar6);

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
  public void testInstant(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      conn.execute("DROP TABLE IF EXISTS test_table", ar1 -> {
        ensureSuccess(context, ar1);
        conn.execute("CREATE TABLE test_table (instant TIMESTAMP)", ar2 -> {
          ensureSuccess(context, ar2);
          Instant now = Instant.now();
          conn.queryWithParams("INSERT INTO test_table (instant) VALUES (?)", new JsonArray().add(now), ar3 -> {
            ensureSuccess(context, ar3);
            conn.query("SELECT instant FROM test_table", ar4 -> {
              ensureSuccess(context, ar4);
              // timestamps with out time zone are returned as strings, so we must compare to the original instant
              // ignoring the timezone offset (meaning ignore everything after char 23)
              context.assertEquals(ar4.result().getResults().get(0).getString(0), now.toString().substring(0, 23));
              async.complete();
            });
          });
        });
      });
    });
  }

  @Test
  public void testInstantWithTimeZone(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      conn.execute("DROP TABLE IF EXISTS test_table", ar1 -> {
        ensureSuccess(context, ar1);
        conn.execute("CREATE TABLE test_table (instant TIMESTAMP WITH TIME ZONE)", ar2 -> {
          ensureSuccess(context, ar2);
          Instant now = Instant.now();
          conn.queryWithParams("INSERT INTO test_table (instant) VALUES (?)", new JsonArray().add(now), ar3 -> {
            ensureSuccess(context, ar3);
            conn.query("SELECT instant FROM test_table", ar4 -> {
              ensureSuccess(context, ar4);
              context.assertEquals(ar4.result().getResults().get(0).getInstant(0), now);
              async.complete();
            });
          });
        });
      });
    });
  }
}
