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

package io.vertx.groovy.ext.asyncsql;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@CompileStatic
public interface BaseSqlService {
  public Object getDelegate();
  void start(Handler<AsyncResult<Void>> whenDone);
  void stop(Handler<AsyncResult<Void>> whenDone);

  static final java.util.function.Function<io.vertx.ext.asyncsql.BaseSqlService, BaseSqlService> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.asyncsql.BaseSqlService arg -> new BaseSqlServiceImpl(arg);
  };
}

@CompileStatic
class BaseSqlServiceImpl implements BaseSqlService {
  final def io.vertx.ext.asyncsql.BaseSqlService delegate;
  public BaseSqlServiceImpl(io.vertx.ext.asyncsql.BaseSqlService delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void start(Handler<AsyncResult<Void>> whenDone) {
    ((io.vertx.ext.asyncsql.BaseSqlService) this.delegate).start(whenDone);
  }
  public void stop(Handler<AsyncResult<Void>> whenDone) {
    ((io.vertx.ext.asyncsql.BaseSqlService) this.delegate).stop(whenDone);
  }
}
