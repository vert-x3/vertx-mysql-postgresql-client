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
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;
import io.vertx.ext.asyncsql.AsyncSQLClient;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
class ClientHolder implements Shareable {

  private final Vertx vertx;
  private final JsonObject config;
  private final boolean mySQL;
  private final Runnable closeRunner;

  private AsyncSQLClient client;
  private int refCount = 1;

  ClientHolder(Vertx vertx, JsonObject config, boolean mySQL, Runnable closeRunner) {
    this.vertx = vertx;
    this.config = config;
    this.mySQL = mySQL;
    this.closeRunner = closeRunner;
  }

  synchronized AsyncSQLClient client() {
    if (client == null) {
      client = new AsyncSQLClientImpl(vertx, config, mySQL);
    }
    return client;
  }

  synchronized void incRefCount() {
    refCount++;
  }

  synchronized void close(Handler<AsyncResult<Void>> whenDone) {
    if (--refCount == 0) {
      if (client != null) {
        client.close(whenDone);
      }
      if (closeRunner != null) {
        closeRunner.run();
      }
    }
  }
}

