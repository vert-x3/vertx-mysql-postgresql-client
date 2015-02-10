/*
 * Copyright 2014 Red Hat, Inc.
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

package io.vertx.ext.asyncsql;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * A verticle that starts an instance of a PostgreSQL service
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class PostgreSqlServiceVerticle extends AsyncSqlServiceVerticle {

  @Override
  protected AsyncSqlService createService(Vertx vertx, JsonObject config) {
    return AsyncSqlService.createPostgreSqlService(vertx, config);
  }
}
