package io.vertx.ext.asyncsql.impl;

import java.util.concurrent.ExecutorService;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.mysql.MySQLQueryResult;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool;
import io.vertx.ext.sql.UpdateResult;
import sun.security.provider.certpath.Vertex;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
public class MySQLConnectionImpl extends AsyncSQLConnectionImpl {
  public MySQLConnectionImpl(Connection conn, AsyncConnectionPool pool, Vertx vertx) {
    super(conn, pool, vertx);
  }

  @Override
  protected String getStartTransactionStatement() {
    return "BEGIN";
  }

  @Override
  protected UpdateResult queryResultToUpdateResult(QueryResult qr) {
    int affected = (int)qr.getRowsAffected();
    MySQLQueryResult mySQLQueryResult = (MySQLQueryResult) qr;
    return new UpdateResult(affected, new JsonArray().add(mySQLQueryResult.getLastInsertId()));
  }
}
