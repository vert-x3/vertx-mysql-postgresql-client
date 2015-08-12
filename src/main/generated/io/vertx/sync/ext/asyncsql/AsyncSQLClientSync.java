/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.sync.ext.asyncsql;

import io.vertx.ext.sql.SQLConnection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.sync.AsyncAdaptor;
import co.paralleluniverse.fibers.Suspendable;
/**
 *
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.asyncsql.AsyncSQLClient original} non interface using Vert.x codegen.
 */

public class AsyncSQLClientSync {

  private final AsyncSQLClient delegate;

  public AsyncSQLClientSync(AsyncSQLClient delegate) {
    this.delegate = delegate;
  }

  public AsyncSQLClient asyncDel() {
    return delegate;
  }

  // The sync methods

  @Suspendable
  public Void close() {
    try {
      return new AsyncAdaptor<Void>() {
        @Override
        protected void requestAsync() {
          delegate.close(this);
        }
      }.run();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  @Suspendable
  public io.vertx.sync.ext.sql.SQLConnectionSync getConnection() {
    try {
      return new io.vertx.sync.ext.sql.SQLConnectionSync(new AsyncAdaptor<SQLConnection>() {
        @Override
        protected void requestAsync() {
          delegate.getConnection(this);
        }
      }.run());
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

}
