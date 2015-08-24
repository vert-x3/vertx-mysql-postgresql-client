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

  // Configure the expected time used in the date test


  /**
   * @return the String form of the time returned for "2015-02-22T07:15:01.234Z".
   */
  @Override
  public String getExpectedTime1() {
    return "2015-02-22T07:15:01.234";
  }

  /**
   * @return the String form of the time returned for "2014-06-27T17:50:02.468+02:00".
   */
  @Override
  public String getExpectedTime2() {
    return "2014-06-27T17:50:02.468";
  }
}
