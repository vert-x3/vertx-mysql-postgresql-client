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
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.sql.*;

import java.util.function.Function;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
public class AsyncSQLClientImpl implements AsyncSQLClient {

  private final BaseSQLClient baseClient;

  public AsyncSQLClientImpl(Vertx vertx, JsonObject config, boolean mysql) {
    if (mysql) {
      baseClient = new MYSQLClientImpl(vertx, config);
    } else {
      baseClient = new PostgreSQLClientImpl(vertx, config);
    }
  }

  @Override
  public void close() {
    baseClient.close(null);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    baseClient.close(completionHandler);
  }

  @Override
  public SQLClient getConnection(Handler<AsyncResult<SQLConnection>> handler) {
    baseClient.getConnection(handler);
    return this;
  }

  @Override
  public SQLClient query(String sql, Handler<AsyncResult<ResultSet>> handler) {
    executeDirect(conn -> Future.future(f -> conn.query(sql, f)), handler);
    return this;
  }

  @Override
  public SQLClient queryStream(String sql, Handler<AsyncResult<SQLRowStream>> handler) {
    executeDirect(conn -> Future.future(f -> conn.queryStream(sql, f)), handler);
    return this;
  }

  @Override
  public SQLClient queryStreamWithParams(String sql, JsonArray params, Handler<AsyncResult<SQLRowStream>> handler) {
    executeDirect(conn -> Future.future(f -> conn.queryStreamWithParams(sql, params, f)), handler);
    return this;
  }

  @Override
  public SQLClient queryWithParams(String sql, JsonArray params, Handler<AsyncResult<ResultSet>> handler) {
    executeDirect(conn -> Future.future(f -> conn.queryWithParams(sql, params, f)), handler);
    return this;
  }

  @Override
  public SQLClient update(String sql, Handler<AsyncResult<UpdateResult>> handler) {
    executeDirect(conn -> Future.future(f -> conn.update(sql, f)), handler);
    return this;
  }

  @Override
  public SQLClient updateWithParams(String sql, JsonArray params, Handler<AsyncResult<UpdateResult>> handler) {
    executeDirect(conn -> Future.future(f -> conn.updateWithParams(sql, params, f)), handler);
    return this;
  }

  @Override
  public SQLClient call(String sql, Handler<AsyncResult<ResultSet>> handler) {
    executeDirect(conn -> Future.future(f -> conn.call(sql, f)), handler);
    return this;
  }

  @Override
  public SQLClient callWithParams(String sql, JsonArray params, JsonArray outputs, Handler<AsyncResult<ResultSet>> handler) {
    executeDirect(conn -> Future.future(f -> conn.callWithParams(sql, params, outputs, f)), handler);
    return this;
  }

  private <T> void executeDirect(Function<SQLConnection, Future<T>> action, Handler<AsyncResult<T>> handler) {
    getConnection(getConnection -> {
      if (getConnection.failed()) {
        handler.handle(Future.failedFuture(getConnection.cause()));
      } else {
        final SQLConnection conn = getConnection.result();
        Future<T> future;
        try {
          future = action.apply(conn);
        } catch (Throwable e) {
          future = Future.failedFuture(e);
        }
        future.setHandler(handler);
      }
    });
  }
}
