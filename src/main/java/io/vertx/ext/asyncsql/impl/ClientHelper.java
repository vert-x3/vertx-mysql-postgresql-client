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

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.sql.SQLClient;


/**
 * This class handles sharing the client instances by using a local shared map.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ClientHelper {

  private static final String DS_LOCAL_MAP_NAME_BASE = "__vertx.MySQLPostgreSQL.pools.";

  public static AsyncSQLClient getOrCreate(Vertx vertx, JsonObject config, String poolName, boolean
      mySQL) {
    synchronized (vertx) {
      LocalMap<String, ClientHolder> map = vertx.sharedData().getLocalMap(
          DS_LOCAL_MAP_NAME_BASE + (mySQL ? "MySQL" : "PostgreSQL"));

      ClientHolder theHolder = map.get(poolName);
      if (theHolder == null) {
        theHolder = new ClientHolder(vertx, config, mySQL, () -> removeFromMap(vertx, map, poolName));
        map.put(poolName, theHolder);
      } else {
        theHolder.incRefCount();
      }
      return new ClientWrapper(theHolder);
    }
  }

  private static void removeFromMap(Vertx vertx, LocalMap<String, ClientHolder> map, String poolName) {
    synchronized (vertx) {
      map.remove(poolName);
      if (map.isEmpty()) {
        map.close();
      }
    }
  }

}
