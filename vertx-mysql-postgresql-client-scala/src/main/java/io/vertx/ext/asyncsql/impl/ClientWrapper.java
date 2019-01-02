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
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

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
}
