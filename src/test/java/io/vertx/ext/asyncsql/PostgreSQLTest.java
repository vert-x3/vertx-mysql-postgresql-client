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
import io.vertx.ext.asyncsql.category.NeedsDocker;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import static io.vertx.ext.asyncsql.SQLTestBase.*;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@Category(NeedsDocker.class)
public class PostgreSQLTest extends AbstractTestBase {

  public static GenericContainer postgresql = new PostgreSQLContainer()
    .withDatabaseName(POSTGRESQL_DATABASE)
    .withUsername(POSTGRESQL_USERNAME)
    .withPassword(POSTGRESQL_PASSWORD)
    .withExposedPorts(5432);

  static {
    postgresql.start();
  }

  @Before
  public void init() {
    client = PostgreSQLClient.createNonShared(vertx, new JsonObject()
      .put("host", postgresql.getContainerIpAddress())
      .put("port", postgresql.getMappedPort(5432))
      .put("database", POSTGRESQL_DATABASE)
      .put("username", POSTGRESQL_USERNAME)
      .put("password", POSTGRESQL_PASSWORD));
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
  public void queryArrayTypeTest(TestContext context) throws Exception {
    Async async = context.async();
    client.getConnection(connAr -> {
      ensureSuccess(context, connAr);
      conn = connAr.result();
      conn.execute("DROP TABLE IF EXISTS test_table", onSuccess(context, dropped -> {
        conn.execute("CREATE TABLE IF NOT EXISTS test_table (arr_int integer[], arr_str text[][])", onSuccess(context, created -> {
          conn.execute("INSERT INTO test_table (arr_int,arr_str) VALUES ('{10000, 10000, 10000, 10000}', '{{\"meeting\", \"lunch\"}, {\"training\", \"presentation\"}}')", onSuccess(context, inserted -> {
            conn.query("SELECT * FROM test_table;", onSuccess(context, arraySelect -> {
              context.assertNotNull(arraySelect);
              context.assertNotNull(arraySelect.getResults());
              List<JsonArray> results = arraySelect.getResults();
              JsonArray resultRow = results.get(0);
              context.assertEquals(resultRow.getJsonArray(0), new JsonArray("[10000,10000,10000,10000]"));
              context.assertEquals(resultRow.getJsonArray(1), new JsonArray("[[\"meeting\",\"lunch\"],[\"training\",\"presentation\"]]"));
              async.complete();
            }));
          }));
        }));
      }));
    });
  }

}
