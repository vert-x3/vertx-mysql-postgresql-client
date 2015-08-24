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
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
public class PostgreSQLTest extends VertxTestBase {

  AsyncSQLClient asyncSqlClient;

  final String address = "campudus.postgresql";

  final JsonObject config = new JsonObject()
      .put("host", System.getProperty("db.host", "localhost"))
      .put("postgresql", new JsonObject().put("address", address));

  @Override
  public void setUp() throws Exception {
    super.setUp();
    asyncSqlClient = PostgreSQLClient.createNonShared(vertx, config);
  }

  @Override
  public void tearDown() throws Exception {
    CountDownLatch latch;
    if (this.asyncSqlClient != null) {
      latch = new CountDownLatch(1);
      this.asyncSqlClient.close((ar) -> {
        latch.countDown();
      });
      this.awaitLatch(latch);
    }

    super.tearDown();
  }

  @Test
  public void someTest() throws Exception {
    asyncSqlClient.getConnection(onSuccess(conn -> {
      conn.query("SELECT 1 AS something", onSuccess(resultSet -> {
        System.out.println(resultSet.getResults());
        assertNotNull(resultSet);
        assertNotNull(resultSet.getColumnNames());
        assertNotNull(resultSet.getResults());
        assertEquals(new JsonArray().add(1), resultSet.getResults().get(0));
        conn.close((ar) -> {
          if (ar.succeeded()) {
            testComplete();
          } else {
            fail("should be able to close the asyncSqlClient");
          }
        });
      }));
    }));

    await();
  }

}
