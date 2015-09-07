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

package io.vertx.ext.asyncsql.impl;

import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import scala.concurrent.impl.Promise;
import scala.util.Success;

import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class VertxEventLoopExecutionContextTest {

  protected static Vertx vertx;

  @BeforeClass
  public static void setUp() {
    vertx = Vertx.vertx();
  }

  @AfterClass
  public static void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }


  @Test
  public void testThatTaskSubmittedOnVertxEventLoopAreExecutedOnThisEventLoop(TestContext tc) {
    Async async = tc.async();
    vertx.runOnContext(any -> {
      final Context context = Vertx.currentContext();
      final Promise.DefaultPromise<String> promise = new Promise.DefaultPromise<>();
      promise.onComplete(ScalaUtils.toFunction1(v -> {
        tc.assertEquals(context, Vertx.currentContext());
        async.complete();
      }), VertxEventLoopExecutionContext.create(vertx));
      promise.complete(new Success<>("hello"));
    });
  }

  @Test
  public void testThatTaskNotSubmittedOnVertxEventLoopAreExecutedOnTheEventLoop(TestContext tc) {
    Async async = tc.async();
    final Context context = Vertx.currentContext();
    tc.assertNull(context);
    final Promise.DefaultPromise<String> promise = new Promise.DefaultPromise<>();
    promise.onComplete(ScalaUtils.toFunction1(v -> {
      tc.assertNotNull(Vertx.currentContext());
      async.complete();
    }), VertxEventLoopExecutionContext.create(vertx));
    promise.complete(new Success<>("hello"));
  }

  @Test
  public void testWithVerticles(TestContext tc) {
    vertx.deployVerticle(MyVerticle.class.getName(), new DeploymentOptions().setInstances(2));
    await().atMost(10, TimeUnit.SECONDS).until(() -> MyVerticle.CONTEXTS.size() == 2);
  }


}