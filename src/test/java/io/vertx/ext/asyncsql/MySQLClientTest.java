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

package io.vertx.ext.asyncsql;

import io.vertx.core.json.JsonObject;
import org.junit.Before;

public class MySQLClientTest extends SQLTestBase {


  @Before
  public void init() {
    client = MySQLClient.createNonShared(vertx,
        new JsonObject()
            .put("host", System.getProperty("db.host", "localhost"))
    );
  }

  // Configure the expected time used in the date test

  /**
   * @return the String form of the time returned for "2015-02-22T07:15:01.234".
   */
  @Override
  public String getExpectedTime1() {
    return "2015-02-22T07:15:01.000";
  }

  /**
   * @return the String form of the time returned for "2014-06-27T17:50:02.468".
   */
  @Override
  public String getExpectedTime2() {
    return "2014-06-27T17:50:02.000";
  }
}
