package io.vertx.ext.asyncsql;

import io.vertx.core.json.JsonObject;
import org.junit.Before;

public class PostgreSQLClientTest extends SQLTestBase {


  @Before
  public void init() {
    client = PostgreSQLClient.createNonShared(vertx,
        new JsonObject()
            .put("host", System.getProperty("db.host", "localhost"))
    );
  }
}
