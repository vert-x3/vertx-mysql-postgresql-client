package io.vertx.ext.asyncsql.tx;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import org.junit.Before;

public class PostgreSQLIsolationLevelTest extends IsolationLevelTest {

  @Before
  public void init() {
    client = PostgreSQLClient.createNonShared(vertx,
      new JsonObject()
        .put("host", System.getProperty("db.host", "localhost"))
    );
  }
}
