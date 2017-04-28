package io.vertx.ext.asyncsql.impl;

import com.github.mauricio.async.db.QueryResult;
import com.github.mauricio.async.db.pool.ConnectionPool;
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.UpdateResult;
import scala.concurrent.ExecutionContext;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>.
 */
public class PostgreSQLConnectionImpl extends AsyncSQLConnectionImpl<PostgreSQLConnection> {

  public PostgreSQLConnectionImpl(Vertx vertx, ConnectionPool<PostgreSQLConnection> pool, ExecutionContext ec) {
    super(vertx, pool, ec);
  }

  @Override
  protected String getStartTransactionStatement() {
    if (transactionIsolation != null) {
      switch (transactionIsolation) {
        case READ_UNCOMMITTED:
          return "START TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
        case REPEATABLE_READ:
          return "START TRANSACTION ISOLATION LEVEL REPEATABLE READ";
        case READ_COMMITTED:
          return "START TRANSACTION ISOLATION LEVEL READ COMMITTED";
        case SERIALIZABLE:
          return "START TRANSACTION ISOLATION LEVEL SERIALIZABLE";
      }
    }
    // the default way
    return "BEGIN";
  }

  @Override
  protected String getSetIsolationLevelStatement() {
    // NOOP
    return null;
  }

  @Override
  protected String getGetIsolationLevelStatement() {
    return "SELECT current_setting('transaction_isolation')";
  }

  @Override
  protected UpdateResult queryResultToUpdateResult(QueryResult qr) {
    int affected = (int) qr.rowsAffected();
    return new UpdateResult(affected, new JsonArray());
  }
}
