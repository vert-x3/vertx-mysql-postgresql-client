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

import io.vertx.ext.asyncsql.category.NeedsDocker;
import org.junit.*;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

@Category(NeedsDocker.class)
public class MySQLClientTest extends SQLTestBase {

  public static GenericContainer mysql = new MySQLContainer("mysql:5.6")
    .withDatabaseName(MYSQL_DATABASE)
    .withUsername(MYSQL_USERNAME)
    .withPassword(MYSQL_PASSWORD)
    .withExposedPorts(3306);

  static {
    mysql.start();
  }

  @Before
  public void init() {
    mysql.start();
    client = MySQLClient.createNonShared(vertx, new JsonObject()
      .put("host", mysql.getContainerIpAddress())
      .put("port", mysql.getMappedPort(3306))
      .put("database", MYSQL_DATABASE)
      .put("username", MYSQL_USERNAME)
      .put("password", MYSQL_PASSWORD));
    clientNoDatabase = MySQLClient.createNonShared(vertx,
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
    return "2015-02-22T07:15:01.000";
  }

  /**
   * @return the String form of the time returned for "2014-06-27T17:50:02.468".
   */
  @Override
  public String getExpectedTime2() {
    return "2014-06-27T17:50:02.000";
  }

  @Override
  protected void setSqlModeIfPossible(Handler<Void> handler) {
    conn.execute("set SQL_MODE = 'STRICT_ALL_TABLES'", ar1 -> {
      // INFO: we ignore the result of this call because it is a mysql specific feature and not all versions support it
      // what is means is that we want the sql parser to be strict even if the engine e.g.: myisam does not implement
      // all constraints such as is the date Feb 31 a valid date. By specifying this we will tell for example that the
      // previous date is invalid.
      handler.handle(null);
    });
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
        conn.updateWithParams("INSERT INTO test_table (name) VALUES (?)", new JsonArray().add(name1), ar3 -> {
          ensureSuccess(context, ar3);
          UpdateResult updateResult1 = ar3.result();
          long id1 = updateResult1.getKeys().getLong(0);
          conn.updateWithParams("INSERT INTO test_table (name) VALUES (?)", new JsonArray().add(name2), ar4 -> {
            ensureSuccess(context, ar4);
            UpdateResult updateResult2 = ar4.result();
            long id2 = updateResult2.getKeys().getLong(0);
            checkConsistency(context, async, conn, id1, name1, id2, name2);
          });
        });
      });
    });
  }

  @Override
  protected void compareInstantStrings(TestContext context, String result, String expected) {
    // mysql will perform some rounding since it does not have the precision to store the full TS

    // this will perform a small hack it will parse the dates and assert that they difference is less or equal to 1 second
    JsonObject test = new JsonObject()
      .put("expected", expected + "Z")
      .put("result", result + "Z");

    final int oneSecond = 1000000000;

    context.assertTrue(Math.abs(test.getInstant("expected").getNano() - test.getInstant("result").getNano()) <= oneSecond);
  }

  @Override
  protected void compareTimeStrings(TestContext context, String result, String expected) {
    // MySQL always only delivers seconds and truncates milliseconds to ".000"
    context.assertEquals(result, expected.replaceAll("\\.\\d{3}$", ".000"));
  }

  @Override
  protected String createByteArray1TableColumn() {
    return "BIT(1)";
  }

  @Override
  protected String createByteArray2TableColumn() {
    return "BIT(2)";
  }

  @Override
  protected String createByteArray3TableColumn() {
    return "BIT(9)";
  }

  @Override
  protected String[] insertByteArray1Values() {
    return new String[]{"B'1'", "B'0'", "B'1'"};
  }

  @Override
  protected String[] insertByteArray2Values() {
    return new String[]{"B'10'", "B'01'", "B'11'"};
  }

  @Override
  protected String[] insertByteArray3Values() {
    return new String[]{"B'100000000'", "B'000000001'", "B'100000001'"};
  }

  private void setupAutoIncrementTable(SQLConnection conn, Handler<AsyncResult<Void>> handler) {
    conn.execute("BEGIN",
        ar -> conn.execute("DROP TABLE IF EXISTS test_table",
            ar2 -> conn.execute("CREATE TABLE test_table (id BIGINT AUTO_INCREMENT, name VARCHAR(255), PRIMARY KEY(id))",
                ar3 -> conn.execute("COMMIT", handler::handle))));
  }
}
