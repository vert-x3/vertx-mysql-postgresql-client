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

}
