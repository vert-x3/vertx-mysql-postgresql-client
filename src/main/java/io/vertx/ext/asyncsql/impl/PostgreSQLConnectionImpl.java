package io.vertx.ext.asyncsql.impl;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool;
import io.vertx.ext.sql.UpdateResult;

import java.util.concurrent.Executor;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>.
 */
public class PostgreSQLConnectionImpl extends AsyncSQLConnectionImpl {
  public PostgreSQLConnectionImpl(Connection conn, AsyncConnectionPool pool, Executor ec) {
    super(conn, pool, ec);
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
