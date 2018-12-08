package io.vertx.ext.asyncsql.impl;

import java.util.concurrent.ExecutorService;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool;
import io.vertx.ext.sql.UpdateResult;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>.
 */
public class PostgreSQLConnectionImpl extends AsyncSQLConnectionImpl {
  public PostgreSQLConnectionImpl(Connection conn, AsyncConnectionPool pool, Vertx vertx) {
    super(conn, pool, vertx);
  }

  @Override
  protected String getStartTransactionStatement() {
    // TODO: consider the tx isolation level
    return "BEGIN";
  }

    @Override
  protected UpdateResult queryResultToUpdateResult(QueryResult qr) {
    int affected = (int) qr.getRowsAffected();
    return new UpdateResult(affected, new JsonArray());
  }
}
