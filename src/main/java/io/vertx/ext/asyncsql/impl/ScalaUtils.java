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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import scala.Function1;
import scala.collection.immutable.List;
import scala.concurrent.ExecutionContext;
import scala.runtime.AbstractFunction1;
import scala.util.Try;

/**
 * Some Scala <=> Java conversion utilities.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ScalaUtils {

  public static <T> Future<T> scalaToVertx(scala.concurrent.Future<T> future, ExecutionContext ec) {
    Future<T> fut = Future.future();
    future.onComplete(new AbstractFunction1<Try<T>, Void>() {
      @Override
      public Void apply(Try<T> v1) {
        if (v1.isSuccess()) {
          fut.complete(v1.get());
        } else {
          fut.fail(v1.failed().get());
        }
        return null;
      }
    }, ec);
    return fut;
  }

  public static <T> Future<Void> scalaToVertxVoid(scala.concurrent.Future<T> future, ExecutionContext ec) {
    Future<Void> fut = Future.future();
    future.onComplete(new AbstractFunction1<Try<T>, Void>() {
      @Override
      public Void apply(Try<T> v1) {
        if (v1.isSuccess()) {
          fut.complete();
        } else {
          fut.fail(v1.failed().get());
        }
        return null;
      }
    }, ec);
    return fut;
  }


  public static <T> java.util.List<T> toJavaList(List<T> list) {
    return scala.collection.JavaConversions.bufferAsJavaList(list.toBuffer());
  }

  public static <T> List<T> toScalaList(java.util.List<T> list) {
    return scala.collection.JavaConversions.asScalaBuffer(list).toList();
  }

  public static <V> Function1<Try<V>, Void> toFunction1(Handler<AsyncResult<V>> code) {
    return new AbstractFunction1<Try<V>, Void>() {
      @Override
      public Void apply(Try<V> v1) {
        if (v1.isSuccess()) {
          code.handle(Future.succeededFuture(v1.get()));
        } else {
          code.handle(Future.failedFuture(v1.failed().get()));
        }
        return null;
      }
    };
  }
}
