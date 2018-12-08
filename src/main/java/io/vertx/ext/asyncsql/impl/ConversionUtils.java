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

import com.github.jasync.sql.db.RowData;

import com.github.jasync.sql.db.util.ExecutorServiceUtils;
import io.netty.channel.EventLoopGroup;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.UUID;

/**
 * Some Java conversion utilities.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public final class ConversionUtils {

  private ConversionUtils () {}

  public static <T> Future<T> completableFutureToVertx(CompletableFuture<T> future, ExecutorService executor) {
    Future<T> fut = Future.future();
    future.whenCompleteAsync((result, error) -> {
      if (error != null) {
        fut.fail(error);
      } else {
        fut.complete(result);
      }
    }, executor);
    return fut;
  }

  public static <T> Future<Void> completableFutureToVertxVoid(CompletableFuture<T> future, ExecutorService executor) {
    Future<Void> fut = Future.future();
    future.whenCompleteAsync((ignored, error) -> {
      if (error != null) {
        fut.fail(error);
      } else {
        fut.complete();
      }
    }, executor);
    return fut;
  }

  public static <T> void connectCompletableFutureWithHandler(CompletableFuture<T> future, ExecutorService executor, Handler<AsyncResult<T>> handler)
  {
    future.whenCompleteAsync((result, error) -> {
      if (error != null) {
        handler.handle(Future.failedFuture(error));
      } else {
        handler.handle(Future.succeededFuture(result));
      }
    }, executor);
  }

  public static <T> void connectCompletableFutureWithVoidHandler(CompletableFuture<T> future, ExecutorService executor, Handler<AsyncResult<Void>> handler)
  {
    future.whenCompleteAsync((ignored, error) -> {
      if (error != null) {
        handler.handle(Future.failedFuture(error));
      } else {
        handler.handle(Future.succeededFuture());
      }
    }, executor);
  }

  public static ExecutorService vertxToExecutorService(Vertx vertx)
  {
    EventLoopGroup eventLoopGroup = vertx.nettyEventLoopGroup();
    return eventLoopGroup == null ? ExecutorServiceUtils.INSTANCE.getCommonPool() : eventLoopGroup;
  }

  @SuppressWarnings("unchecked")
  public static List<Object> WrapList(JsonArray array)
  {
    return (List<Object>)array.getList();
  }

  public static JsonArray rowToJsonArray(RowData data) {
    JsonArray array = new JsonArray();
    data.forEach(value -> convertValue(array, value));
    return array;
  }
  private static void convertValue(JsonArray array, Object value) {
    if (value == null) {
      array.addNull();
    } else if (value instanceof BigDecimal) {
      array.add(value.toString());
    } else if (value instanceof LocalDateTime) {
      array.add(value.toString());
    } else if (value instanceof LocalDate) {
      array.add(value.toString());
    } else if (value instanceof LocalTime) {
      array.add(value.toString());
    } else if (value instanceof DateTime) {
      array.add(Instant.ofEpochMilli(((DateTime) value).getMillis()));
    } else if (value instanceof UUID) {
      array.add(value.toString());
    } else if (value instanceof List) {
      JsonArray subArray = new JsonArray();
      @SuppressWarnings("unchecked")
      List<Object> list = (List<Object>)value;
      for (int i = 0; i < list.size(); ++i)
      {
        convertValue(subArray, list.get(i));
      }
      array.add(subArray);
    } else {
      array.add(value);
    }
  }
}
