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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MyVerticle extends AbstractVerticle {

  public static final Set<Context> CONTEXTS = new CopyOnWriteArraySet<>();

  @Override
  public void start() throws Exception {
    CompletableFuture<String> promise = new CompletableFuture<String>();
    CONTEXTS.add(context);
    promise.whenCompleteAsync((v, e) -> {
      if (context != Vertx.currentContext()) {
        throw new RuntimeException("Bad context");
      }
      CONTEXTS.add(Vertx.currentContext());
    }, vertx.nettyEventLoopGroup());
    promise.complete("hello");
  }
}
