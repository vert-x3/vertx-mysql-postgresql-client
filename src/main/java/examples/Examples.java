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

package examples;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSqlService;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SqlConnection;

/**
  *
  * @author <a href="http://tfox.org">Tim Fox</a>
  */
public class Examples {

  public void example1(Vertx vertx) {

    // Deploy service - can be anywhere on your network
    JsonObject config = new JsonObject().put("host", "mymysqldb.mycompany");
    DeploymentOptions options = new DeploymentOptions().setConfig(config);

    // Service name is "io.vertx:postgresql-service" if you want postgreSql
    vertx.deployVerticle("io.vertx:mysql-service", options, res -> {
      if (res.succeeded()) {
        // Deployed ok
      } else {
        // Failed to deploy
      }
    });
  }

  public void example2(Vertx vertx) {

    AsyncSqlService proxy = AsyncSqlService.createEventBusProxy(vertx, "vertx.mysql");

    // Now do stuff with it:

    proxy.getConnection(res -> {
      if (res.succeeded()) {

        SqlConnection connection = res.result();

        connection.query("SELECT * FROM some_table", res2 -> {
          if (res2.succeeded()) {

            ResultSet rs = res2.result();
            // Do something with results
          }
        });
      } else {
        // Failed to get connection - deal with it
      }
    });
  }

  public void example3(Vertx vertx) {

    JsonObject config = new JsonObject().put("host", "mymysqldb.mycompany");

    AsyncSqlService mySqlService = AsyncSqlService.createMySqlService(vertx, config);

    mySqlService.start(res -> {
      if (res.succeeded()) {

        // Started OK - now ready to use!
      } else {
        // Failed to start
      }
    });

  }

  public void example4(AsyncSqlService service) {

    // Now do stuff with it:

    service.getConnection(res -> {
      if (res.succeeded()) {

        SqlConnection connection = res.result();

        // Got a connection

      } else {
        // Failed to get connection - deal with it
      }
    });

  }


}
