package io.vertx.ext.asyncsql.impl;

import com.github.mauricio.async.db.QueryResult;
import com.github.mauricio.async.db.mysql.MySQLConnection;
import com.github.mauricio.async.db.mysql.MySQLQueryResult;
import com.github.mauricio.async.db.pool.ConnectionPool;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.UpdateResult;
import scala.concurrent.ExecutionContext;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
public class MySQLConnectionImpl extends AsyncSQLConnectionImpl<MySQLConnection> {

  public MySQLConnectionImpl(Vertx vertx, MySQLConnection conn, ConnectionPool<MySQLConnection> pool, ExecutionContext ec) {
    super(vertx, conn, pool, ec);
  }

  @Override
  protected String getStartTransactionStatement() {
    return "BEGIN";
  }

  @Override
  protected String getSetIsolationLevelStatement() {
    switch (transactionIsolation) {
      case READ_UNCOMMITTED:
        return "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
      case REPEATABLE_READ:
        return "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ";
      case READ_COMMITTED:
        return "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED";
      case SERIALIZABLE:
        return "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE";
    }

    return null;
  }

  @Override
  protected String getGetIsolationLevelStatement() {
    return "SELECT @@tx_isolation";
  }

  @Override
  protected UpdateResult queryResultToUpdateResult(QueryResult qr) {
    int affected = (int)qr.rowsAffected();
    MySQLQueryResult mySQLQueryResult = (MySQLQueryResult) qr;
    return new UpdateResult(affected, new JsonArray().add(mySQLQueryResult.lastInsertId()));
  }
}
