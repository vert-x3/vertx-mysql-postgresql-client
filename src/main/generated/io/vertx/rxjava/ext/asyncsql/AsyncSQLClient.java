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

package io.vertx.rxjava.ext.asyncsql;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.rxjava.ext.sql.SQLConnection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * Represents an asynchronous SQL client
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.asyncsql.AsyncSQLClient original} non RX-ified interface using Vert.x codegen.
 */

public class AsyncSQLClient {

  final io.vertx.ext.asyncsql.AsyncSQLClient delegate;

  public AsyncSQLClient(io.vertx.ext.asyncsql.AsyncSQLClient delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * Close the client and release all resources.
   * Note that closing is asynchronous.
   */
  public void close() { 
    this.delegate.close();
  }

  /**
   * Close the client and release all resources.
   * Call the handler when close is complete.
   * @param whenDone handler that will be called when close is complete
   */
  public void close(Handler<AsyncResult<Void>> whenDone) { 
    this.delegate.close(whenDone);
  }

  /**
   * Close the client and release all resources.
   * Call the handler when close is complete.
   * @return 
   */
  public Observable<Void> closeObservable() { 
    io.vertx.rx.java.ObservableFuture<Void> whenDone = io.vertx.rx.java.RxHelper.observableFuture();
    close(whenDone.toHandler());
    return whenDone;
  }

  /**
   * Returns a connection that can be used to perform SQL operations on. It's important to remember to close the
   * connection when you are done, so it is returned to the pool.
   * @param handler the handler which is called when the <code>JdbcConnection</code> object is ready for use.
   */
  public void getConnection(Handler<AsyncResult<SQLConnection>> handler) { 
    this.delegate.getConnection(new Handler<AsyncResult<io.vertx.ext.sql.SQLConnection>>() {
      public void handle(AsyncResult<io.vertx.ext.sql.SQLConnection> event) {
        AsyncResult<SQLConnection> f;
        if (event.succeeded()) {
          f = InternalHelper.<SQLConnection>result(new SQLConnection(event.result()));
        } else {
          f = InternalHelper.<SQLConnection>failure(event.cause());
        }
        handler.handle(f);
      }
    });
  }

  /**
   * Returns a connection that can be used to perform SQL operations on. It's important to remember to close the
   * connection when you are done, so it is returned to the pool.
   * @return 
   */
  public Observable<SQLConnection> getConnectionObservable() { 
    io.vertx.rx.java.ObservableFuture<SQLConnection> handler = io.vertx.rx.java.RxHelper.observableFuture();
    getConnection(handler.toHandler());
    return handler;
  }


  public static AsyncSQLClient newInstance(io.vertx.ext.asyncsql.AsyncSQLClient arg) {
    return arg != null ? new AsyncSQLClient(arg) : null;
  }
}
