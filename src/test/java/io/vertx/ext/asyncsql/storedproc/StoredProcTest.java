package io.vertx.ext.asyncsql.storedproc;

import io.vertx.ext.asyncsql.AbstractTestBase;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public abstract class StoredProcTest extends AbstractTestBase {

  @Test
  public void testSimpleCall(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      if (ar.failed()) {
        context.fail(ar.cause());
        return;
      }

      // Create table
      conn = ar.result();

      // prepare data
      String func =
        "CREATE OR REPLACE FUNCTION add(integer, integer) RETURNS integer\n" +
          "    AS 'select $1 + $2;'\n" +
          "    LANGUAGE SQL\n" +
          "    IMMUTABLE\n" +
          "    RETURNS NULL ON NULL INPUT;";

      conn.execute(func, ar1 -> {
        ensureSuccess(context, ar1);
        conn.call("{ call add(1,1) }", ar2 -> {
          ensureSuccess(context, ar2);
          context.assertEquals(2, ar2.result().getResults().get(0).getInteger(0));
          async.complete();
        });
      });
    });
  }
}
