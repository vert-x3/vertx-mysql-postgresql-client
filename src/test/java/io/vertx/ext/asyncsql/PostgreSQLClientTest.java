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
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.category.NeedsDocker;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

@Category(NeedsDocker.class)
public class PostgreSQLClientTest extends SQLTestBase {

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
    client = PostgreSQLClient.createNonShared(vertx,   new JsonObject()
      .put("host", postgresql.getContainerIpAddress())
      .put("port", postgresql.getMappedPort(5432))
      .put("database", POSTGRESQL_DATABASE)
      .put("username", POSTGRESQL_USERNAME)
      .put("password", POSTGRESQL_PASSWORD));

    clientNoDatabase = PostgreSQLClient.createNonShared(vertx,
      new JsonObject()
        .put("host", "localhost")
        .put("port", 65000)
        .put("maxPoolSize", 2)
    );
  }

  // Configure the expected time used in the date test


  /**
   * @return the String form of the time returned for "2015-02-22T07:15:01.234".
   */
  @Override
  public String getExpectedTime1() {
    return "2015-02-22T07:15:01.234";
  }

  /**
   * @return the String form of the time returned for "2014-06-27T17:50:02.468".
   */
  @Override
  public String getExpectedTime2() {
    return "2014-06-27T17:50:02.468";
  }

  @Test
  public void testInsertedIds(TestContext context) {
    String name1 = "Adele";
    String name2 = "Betty";
    Async async = context.async();

    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      setupAutoIncrementTable(conn, ar2 -> {
        ensureSuccess(context, ar2);
        conn.queryWithParams("INSERT INTO test_table (name) VALUES (?) RETURNING id", new JsonArray().add(name1), ar3 -> {
          ensureSuccess(context, ar3);
          ResultSet updateResult1 = ar3.result();
          long id1 = updateResult1.getResults().get(0).getLong(0);
          conn.queryWithParams("INSERT INTO test_table (name) VALUES (?) RETURNING id", new JsonArray().add(name2), ar4 -> {
            ensureSuccess(context, ar4);
            ResultSet updateResult2 = ar4.result();
            long id2 = updateResult2.getResults().get(0).getLong(0);
            checkConsistency(context, async, conn, id1, name1, id2, name2);
          });
        });
      });
    });
  }

  @Test
  public void testUsingUUIDsInTables(TestContext context) {
    Async async = context.async();
    final UUID uuid = UUID.randomUUID();
    final String name = "xyz";

    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      setupTableWithUUIDs(conn, ar2 -> {
        ensureSuccess(context, ar2);
        conn.queryWithParams("INSERT INTO test_table (some_uuid, name) VALUES (?, ?)", new JsonArray().add(uuid.toString()).add(name), ar3 -> {
          ensureSuccess(context, ar3);
          conn.queryWithParams("SELECT some_uuid FROM test_table WHERE name = ?", new JsonArray().add(name), ar4 -> {
            ensureSuccess(context, ar4);
            ResultSet resultSet = ar4.result();
            context.assertEquals(1, resultSet.getNumRows());
            context.assertEquals("some_uuid", resultSet.getColumnNames().get(0));
            context.assertEquals(new JsonObject().put("some_uuid", uuid.toString()), resultSet.getRows().get(0));

            async.complete();
          });
        });
      });
    });
  }

  @Override
  protected String createByteArray1TableColumn() {
    return "BYTEA";
  }

  @Override
  protected String createByteArray2TableColumn() {
    return "BYTEA";
  }

  @Override
  protected String createByteArray3TableColumn() {
    return "BYTEA";
  }

  @Override
  protected String[] insertByteArray1Values() {
    return new String[]{"E'\\x01'", "E'\\\\x00'", "E'\\\\x01'"};
  }

  @Override
  protected String[] insertByteArray2Values() {
    return new String[]{"E'\\\\x02'", "E'\\\\x01'", "E'\\\\x03'"};
  }

  @Override
  protected String[] insertByteArray3Values() {
    return new String[]{"E'\\\\x0100'", "E'\\\\x0001'", "E'\\\\x0101'"};
  }

  private void setupAutoIncrementTable(SQLConnection conn, Handler<AsyncResult<Void>> handler) {
    conn.execute("BEGIN",
        ar -> conn.execute("DROP TABLE IF EXISTS test_table",
            ar2 -> conn.execute("CREATE TABLE test_table (id BIGSERIAL, name VARCHAR(255), PRIMARY KEY(id))",
                ar3 -> conn.execute("COMMIT", handler::handle))));
  }

  private void setupTableWithUUIDs(SQLConnection conn, Handler<AsyncResult<Void>> handler) {
    conn.execute("BEGIN",
        ar -> conn.execute("DROP TABLE IF EXISTS test_table",
            ar2 -> conn.execute("CREATE TABLE test_table (some_uuid UUID, name VARCHAR(255))",
                ar3 -> conn.execute("COMMIT", handler::handle))));
  }
}
