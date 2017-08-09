package io.vertx.ext.asyncsql.storedproc;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.asyncsql.tx.IsolationLevelTest;
import org.junit.Before;

public class PostgreSQLStoredProcTest extends StoredProcTest {

  @Before
  public void init() {
    client = PostgreSQLClient.createNonShared(vertx,
      new JsonObject()
        .put("host", System.getProperty("db.host", "localhost"))
    );
  }
}
