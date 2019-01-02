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

import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.test.core.VertxTestBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@RunWith(VertxUnitRunner.class)
public class RefCountTest extends VertxTestBase {

  private LocalMap<String, Object> getLocalMap() {
    return vertx.sharedData().getLocalMap("__vertx.MySQLPostgreSQL.pools.MySQL");
  }

  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  @Test
  public void testNonShared() {
    LocalMap<String, Object> map = getLocalMap();
    JsonObject config = new JsonObject();
    SQLClient client1 = MySQLClient.createNonShared(vertx, config);
    assertEquals(1, map.size());
    SQLClient client2 = MySQLClient.createNonShared(vertx, config);
    assertEquals(2, map.size());
    SQLClient client3 = MySQLClient.createNonShared(vertx, config);
    assertEquals(3, map.size());
    client1.close();
    assertEquals(2, map.size());
    client2.close();
    assertEquals(1, map.size());
    client3.close();
    assertEquals(0, map.size());
    assertWaitUntil(() -> getLocalMap().size() == 0);
    assertWaitUntil(() -> map != getLocalMap()); // Map has been closed
  }

  @Test
  public void testSharedDefault() throws Exception {
    LocalMap<String, Object> map = getLocalMap();
    JsonObject config = new JsonObject();
    SQLClient client1 = MySQLClient.createShared(vertx, config);
    assertEquals(1, map.size());
    SQLClient client2 = MySQLClient.createShared(vertx, config);
    assertEquals(1, map.size());
    SQLClient client3 = MySQLClient.createShared(vertx, config);
    assertEquals(1, map.size());
    client1.close();
    assertEquals(1, map.size());
    client2.close();
    assertEquals(1, map.size());
    client3.close();
    assertWaitUntil(() -> map.size() == 0);
    assertWaitUntil(() -> map != getLocalMap()); // Map has been closed
  }

  @Test
  public void testSharedNamed() throws Exception {
    LocalMap<String, Object> map = getLocalMap();
    JsonObject config = new JsonObject();
    SQLClient client1 = MySQLClient.createShared(vertx, config, "ds1");
    assertEquals(1, map.size());
    SQLClient client2 = MySQLClient.createShared(vertx, config, "ds1");
    assertEquals(1, map.size());
    SQLClient client3 = MySQLClient.createShared(vertx, config, "ds1");
    assertEquals(1, map.size());

    SQLClient client4 = MySQLClient.createShared(vertx, config, "ds2");
    assertEquals(2, map.size());
    SQLClient client5 = MySQLClient.createShared(vertx, config, "ds2");
    assertEquals(2, map.size());
    SQLClient client6 = MySQLClient.createShared(vertx, config, "ds2");
    assertEquals(2, map.size());

    client1.close();
    assertEquals(2, map.size());
    client2.close();
    assertEquals(2, map.size());
    client3.close();
    assertWaitUntil(() -> map.size() == 1);

    client4.close();
    assertEquals(1, map.size());
    client5.close();
    assertEquals(1, map.size());
    client6.close();
    assertWaitUntil(() -> map.size() == 0);
    assertWaitUntil(() -> map != getLocalMap()); // Map has been closed
  }
}
