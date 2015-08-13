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
}
