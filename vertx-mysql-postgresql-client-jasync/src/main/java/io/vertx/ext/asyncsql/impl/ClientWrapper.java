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
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.sql.*;

/**
 * Wraps a client with the {@link ClientHolder} in order to keep track of the references.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ClientWrapper implements AsyncSQLClient {

  private final ClientHolder holder;
  private final AsyncSQLClient client;

  public ClientWrapper(ClientHolder holder) {
    this.holder = holder;
    this.client = holder.client();
  }

  @Override
  public void close(Handler<AsyncResult<Void>> whenDone) {
    holder.close(whenDone);
  }

  @Override
  public void close() {
    holder.close(null);
  }

  @Override
  public SQLClient getConnection(Handler<AsyncResult<SQLConnection>> handler) {
    return client.getConnection(handler);
  }

  @Override
  public SQLClient query(String sql, Handler<AsyncResult<ResultSet>> handler) {
    client.query(sql, handler);
    return this;
  }

  @Override
  public SQLClient queryStream(String sql, Handler<AsyncResult<SQLRowStream>> handler) {
    client.queryStream(sql, handler);
    return this;
  }

  @Override
  public SQLClient queryStreamWithParams(String sql, JsonArray params, Handler<AsyncResult<SQLRowStream>> handler) {
    client.queryStreamWithParams(sql, params, handler);
    return this;
  }

  @Override
  public SQLClient queryWithParams(String sql, JsonArray params, Handler<AsyncResult<ResultSet>> handler) {
    client.queryWithParams(sql, params, handler);
    return this;
  }

  @Override
  public SQLClient update(String sql, Handler<AsyncResult<UpdateResult>> handler) {
    client.update(sql, handler);
    return this;
  }

  @Override
  public SQLClient updateWithParams(String sql, JsonArray params, Handler<AsyncResult<UpdateResult>> handler) {
    client.updateWithParams(sql, params, handler);
    return this;
  }

  @Override
  public SQLClient call(String sql, Handler<AsyncResult<ResultSet>> handler) {
    client.call(sql, handler);
    return this;
  }

  @Override
  public SQLClient callWithParams(String sql, JsonArray params, JsonArray outputs, Handler<AsyncResult<ResultSet>> handler) {
    client.callWithParams(sql, params, outputs, handler);
    return this;
  }
}
