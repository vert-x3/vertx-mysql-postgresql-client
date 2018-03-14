package io.vertx.ext.asyncsql;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.List;

import static io.vertx.ext.asyncsql.PostgreSQL.start;
import static io.vertx.ext.asyncsql.SQLTestBase.START_POSTGRES;

/**
 * Tests the configuration options of the PostgreSQL client.
 */
public class PostgreSQLConfigurationTest extends ConfigurationTest {

  private static PostgreSQL pg;

  @BeforeClass
  public static void before() throws Exception {
    if (START_POSTGRES) {
      pg = start(SQLTestBase.POSTGRESQL_PORT);
    }
  }

  @AfterClass
  public static void after() throws Exception {
    if (pg != null) {
      pg.stop();
    }
  }

  @Override
  protected SQLClient createClient(Vertx vertx, JsonObject config) {
    return PostgreSQLClient.createNonShared(vertx, config.mergeIn(SQLTestBase.POSTGRESQL_CONFIG));
  }

  @Override
  public String sleepCommand(int seconds) {
    return "pg_sleep(" + seconds + ")";
  }

  @Override
  protected String getEncodingStatement() {
    return "SHOW client_encoding";
  }

  @Override
  protected String getEncodingValueFromResults(List<JsonArray> results) {
    return results.get(0).getString(0);
  }

}
