package io.vertx.ext.asyncsql;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class PostgreSQLClientTest extends SQLTestBase {


  @Before
  public void init() {
    client = PostgreSQLClient.createNonShared(vertx,
        new JsonObject()
            .put("host", System.getProperty("db.host", "localhost"))
    );
  }

  // Date test is implementation-specific as MySQL is shrinking the end of the time (using 000).


  public static final String insertedTime1 = "2015-02-22T07:15:01.234Z";
  public static final String expectedTime1 = "2015-02-22T07:15:01.234";
  public static final String insertedTime2 = "2014-06-27T17:50:02.468+02:00";
  public static final String expectedTime2 = "2014-06-27T17:50:02.468";

  @Test
  public void testDateValueSelection(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      ensureSuccess(context, ar);
      conn = ar.result();
      conn.execute("DROP TABLE IF EXISTS test_date_table", ar2 -> {
        ensureSuccess(context, ar2);
        conn.execute("CREATE TABLE test_date_table (id BIGINT, some_date DATE,some_timestamp TIMESTAMP)", ar3 -> {
          ensureSuccess(context, ar3);
          conn.updateWithParams("INSERT INTO test_date_table (id, some_date, some_timestamp) VALUES (?, ?, ?)", new JsonArray().add(1).add("2015-02-22").add(insertedTime1), ar4 -> {
            ensureSuccess(context, ar4);
            conn.updateWithParams("INSERT INTO test_date_table (id, some_date, some_timestamp) VALUES (?, ?, ?)", new JsonArray().add(2).add("2007-07-20").add(insertedTime2), ar5 -> {
              ensureSuccess(context, ar5);
              conn.query("SELECT id, some_date, some_timestamp FROM test_date_table ORDER BY id", ar6 -> {
                ensureSuccess(context, ar6);
                ResultSet results = ar6.result();
                List<String> columns = results.getColumnNames();
                context.assertEquals(3, columns.size());
                context.assertEquals("id", columns.get(0));
                context.assertEquals("some_date", columns.get(1));
                context.assertEquals("some_timestamp", columns.get(2));

                context.assertEquals(2, results.getResults().size());
                JsonArray row1 = results.getResults().get(0);
                context.assertEquals(row1.getString(1), "2015-02-22");
                context.assertEquals(row1.getString(2), expectedTime1);
                JsonArray row2 = results.getResults().get(1);
                context.assertEquals(row2.getString(1), "2007-07-20");
                context.assertEquals(row2.getString(2), expectedTime2);

                async.complete();
              });
            });
          });
        });
      });
    });
  }
}
