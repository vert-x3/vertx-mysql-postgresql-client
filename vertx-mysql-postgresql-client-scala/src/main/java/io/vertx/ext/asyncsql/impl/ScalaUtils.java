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

import com.github.mauricio.async.db.RowData;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import scala.Function1;
import scala.collection.immutable.List;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.FiniteDuration;
import scala.runtime.AbstractFunction1;
import scala.util.Try;

import java.time.Instant;
import java.util.UUID;

/**
 * Some Scala <=> Java conversion utilities.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public final class ScalaUtils {

  private ScalaUtils () {}

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

  public static JsonArray rowToJsonArray(RowData data) {
    JsonArray array = new JsonArray();
    data.foreach(new AbstractFunction1<Object, Void>() {
      @Override
      public Void apply(Object value) {
        convertValue(array, value);
        return null;
      }
    });
    return array;
  }
  private static void convertValue(JsonArray array, Object value) {
    if (value == null) {
      array.addNull();
    } else if (value instanceof scala.math.BigDecimal) {
      array.add(value.toString());
    } else if (value instanceof LocalDateTime) {
      array.add(value.toString());
    } else if (value instanceof LocalDate) {
      array.add(value.toString());
    } else if (value instanceof LocalTime) {
      array.add(value.toString());
    } else if (value instanceof FiniteDuration) {
      String time = durationToString(((FiniteDuration) value).toMillis());
      array.add(time);
    } else if (value instanceof DateTime) {
      array.add(Instant.ofEpochMilli(((DateTime) value).getMillis()));
    } else if (value instanceof UUID) {
      array.add(value.toString());
    } else if (value instanceof scala.collection.mutable.ArrayBuffer) {
      scala.collection.mutable.ArrayBuffer<Object> arrayBuffer = (scala.collection.mutable.ArrayBuffer<Object>) value;
      JsonArray subArray = new JsonArray();
      arrayBuffer.foreach(new AbstractFunction1<Object, Void>() {

        @Override
        public Void apply(Object subValue) {
          convertValue(subArray, subValue);
          return null;
        }

      });
      array.add(subArray);
    } else {
      array.add(value);
    }
  }

  private static String durationToString(long allInMillis) {
    long hours = allInMillis / 1000 / 60 / 60;
    long minutes = allInMillis / 1000 / 60 % 60;
    long seconds = allInMillis / 1000 % 60;
    long millis = allInMillis % 1000;
    return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
  }
}
