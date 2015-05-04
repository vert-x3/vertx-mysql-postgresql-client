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

package io.vertx.ext.asyncsql.impl;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.asyncsql.AsyncSQLClient;


/**
 *
 * This class handles sharing the client instances by using a local shared map.
 *
 * Note - the main body of the client is currently written in Scala - this should be refactored to Java then we
 * can more easily just share the pool part of the client instead of creating a wrapper for the whole client.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ClientHelper {

  private static final String DS_LOCAL_MAP_NAME_BASE =  "__vertx.MySQLPostgreSQL.datasources.";

  public static AsyncSQLClient getOrCreate(Vertx vertx, JsonObject config, String dataSourceName, boolean mySQL) {
    return lookupClient(vertx, config, dataSourceName, mySQL);
  }

  private static AsyncSQLClient lookupClient(Vertx vertx, JsonObject config, String datasourceName, boolean mySQL) {
    synchronized (vertx) {
      LocalMap<String, ClientHolder> map = vertx.sharedData().getLocalMap(DS_LOCAL_MAP_NAME_BASE + (mySQL ? "MySQL" : "PostgreSQL"));
      ClientHolder theHolder = map.get(datasourceName);
      if (theHolder == null) {
        theHolder = new ClientHolder(vertx, config, mySQL, () -> removeFromMap(vertx, map, datasourceName));
        map.put(datasourceName, theHolder);
      } else {
        theHolder.incRefCount();
      }
      return new ClientWrapper(theHolder);
    }
  }

  private static void removeFromMap(Vertx vertx, LocalMap<String, ClientHolder> map, String dataSourceName) {
    synchronized (vertx) {
      map.remove(dataSourceName);
      if (map.isEmpty()) {
        map.close();
      }
    }
  }

}
