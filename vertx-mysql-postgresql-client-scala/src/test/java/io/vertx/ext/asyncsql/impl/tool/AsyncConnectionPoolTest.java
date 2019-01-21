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

package io.vertx.ext.asyncsql.impl.tool;

import com.github.mauricio.async.db.Connection;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import scala.concurrent.impl.Promise;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * AsyncConnectionPoolTest.java
 *
 * @author <a href="mailto:ernestojpg@gmail.com">Ernesto J. Perez</a>
 */
@RunWith(VertxUnitRunner.class)
public class AsyncConnectionPoolTest {

  private final int MAX_POOL_SIZE = 15;

  private Vertx vertx;

  @Before
  public void setUp() {
    this.vertx = Mockito.mock(Vertx.class);
    Mockito.when(vertx.setTimer(Mockito.anyLong(),Mockito.any()))
      .then(invocation -> {
        final Handler<Long> handler = invocation.getArgument(1);
        handler.handle(ThreadLocalRandom.current().nextLong());
        return null;
      });
  }

  // We will try to obtain 50 connections from a pool with a maxPoolSize of 15
  @Test
  public void testMaxPoolSize(TestContext context) throws InterruptedException {
    final int TEST_LENGTH = 50;
    final CountDownLatch countDownLatch = new CountDownLatch(TEST_LENGTH);

    final AsyncConnectionPoolMock pool = new AsyncConnectionPoolMock(
      new JsonObject().put("maxPoolSize", MAX_POOL_SIZE),
      this::getGoodConnection);

    final Queue<Connection> connectionSet = new LinkedList<>();
    // Ask for 50 connections
    for (int i = 0; i < TEST_LENGTH; i++) {
      pool.take(result -> {
        // We will decrease our CountDownLatch with each obtained connection
        countDownLatch.countDown();
        context.assertTrue(result.succeeded());
        connectionSet.add(result.result());
      });
    }

    // Wait up to 1 second to obtain the 50 connections (it should not happen)
    context.assertFalse(countDownLatch.await(1, TimeUnit.SECONDS));
    // We will check that we only had 15 connections (MAX_POOL_SIZE)
    context.assertEquals(MAX_POOL_SIZE, pool.connectionAttempts);
    context.assertEquals(MAX_POOL_SIZE, pool.createdConnections);
    context.assertEquals(MAX_POOL_SIZE, connectionSet.size());
    context.assertEquals(TEST_LENGTH - MAX_POOL_SIZE, (int)countDownLatch.getCount()); // Counter should be 35

    // We will give back the connections one by one. No new connections should be created
    for (int i = MAX_POOL_SIZE + 1; i <= TEST_LENGTH; i++) {
      pool.giveBack(connectionSet.poll());
      context.assertEquals(MAX_POOL_SIZE, pool.connectionAttempts);
      context.assertEquals(MAX_POOL_SIZE, pool.createdConnections);
      context.assertEquals(MAX_POOL_SIZE, connectionSet.size());
      context.assertEquals(TEST_LENGTH - i, (int)countDownLatch.getCount());
    }
  }

  // Test that by default we don't do any retry
  @Test
  public void testNoRetriesByDefault(TestContext context) {

    final Async async = context.async();

    final AsyncConnectionPoolMock pool = new AsyncConnectionPoolMock(new JsonObject(),
      this::getFailingConnection);

      pool.take(result -> {
        context.assertTrue(result.failed());
        context.assertTrue(result.cause() instanceof RuntimeException);
        context.assertEquals(1, pool.connectionAttempts);
        context.assertEquals(0, pool.createdConnections);
        Mockito.verifyNoMoreInteractions(vertx);
        async.complete();
      });
  }

  // Try to obtain a connection and fails in all retries
  @Test
  public void testRetriesAndFail(TestContext context) {
    final int MAX_RETRIES = 5;

    final Async async = context.async();

    final AsyncConnectionPoolMock pool = new AsyncConnectionPoolMock(
      new JsonObject()
        .put("maxConnectionRetries", MAX_RETRIES)
        .put("connectionRetryDelay", 100L),
      this::getFailingConnection);

    pool.take(result -> {
      context.assertTrue(result.failed());
      context.assertTrue(result.cause() instanceof RuntimeException);
      context.assertEquals(MAX_RETRIES + 1, pool.connectionAttempts);
      context.assertEquals(0, pool.createdConnections);

      // Verify the the Vert.x timer has been used 5 times (MAX_RETRIES)
      Mockito.verify(vertx, Mockito.times(MAX_RETRIES)).setTimer(Mockito.eq(100L), Mockito.any());
      Mockito.verifyNoMoreInteractions(vertx);

      async.complete();
    });
  }

  // Try to obtain a connection, fails, and success in the last retry
  @Test
  public void testRetriesAndSuccess(TestContext context) {
    final int MAX_RETRIES = 5;

    final Async async = context.async();

    final AsyncConnectionPoolMock pool = new AsyncConnectionPoolMock(
      new JsonObject()
        .put("maxConnectionRetries", MAX_RETRIES)
        .put("connectionRetryDelay", 100L),
      new Supplier<Connection>() {
        int count=0;
        @Override public Connection get() {
          count++;
          if (count < MAX_RETRIES + 1)
            return getFailingConnection();
          else
            return getGoodConnection();
        }
      });

    pool.take(result -> {
      context.assertTrue(result.succeeded());
      context.assertEquals(MAX_RETRIES + 1, pool.connectionAttempts);
      context.assertEquals(1, pool.createdConnections);

      // Verify the the Vert.x timer has been used 5 times (MAX_RETRIES)
      Mockito.verify(vertx, Mockito.times(MAX_RETRIES)).setTimer(Mockito.eq(100L), Mockito.any());
      async.complete();
    });
  }

  private Connection getGoodConnection() {
    final Connection connection = Mockito.mock(Connection.class);
    Mockito.when(connection.connect()).thenReturn(new Promise.DefaultPromise<Connection>().success(connection).future());
    Mockito.when(connection.isConnected()).thenReturn(true);
    return connection;
  }

  private Connection getFailingConnection() {
    throw new RuntimeException("Expected exception");
  }

  /**
   * This class just extends from the abstract class AsyncConnectionPool that we want to test.
   */
  private class AsyncConnectionPoolMock extends AsyncConnectionPool {

    int connectionAttempts = 0;
    int createdConnections = 0;
    private Supplier<Connection> connectionSupplier;

    AsyncConnectionPoolMock(JsonObject globalConfig, Supplier<Connection> connectionSupplier) {
      super(AsyncConnectionPoolTest.this.vertx, globalConfig, null);
      this.connectionSupplier = connectionSupplier;
    }

    @Override
    protected Connection create() {
      this.connectionAttempts++;
      final Connection connection = connectionSupplier.get();
      this.createdConnections++;
      return connection;
    }
  }
}
