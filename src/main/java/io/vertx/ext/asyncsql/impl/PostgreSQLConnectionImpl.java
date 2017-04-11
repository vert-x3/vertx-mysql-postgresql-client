package io.vertx.ext.asyncsql.impl;

import com.github.mauricio.async.db.Connection;
import com.github.mauricio.async.db.QueryResult;
import com.github.mauricio.async.db.mysql.MySQLQueryResult;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool;
import io.vertx.ext.sql.UpdateResult;
import scala.concurrent.ExecutionContext;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>.
 */
public class PostgreSQLConnectionImpl extends AsyncSQLConnectionImpl {
  public PostgreSQLConnectionImpl(Connection conn, AsyncConnectionPool pool, ExecutionContext ec) {
    super(conn, pool, ec);
  }

  @Override
  protected UpdateResult queryResultToUpdateResult(QueryResult qr) {
    int affected = (int) qr.rowsAffected();
    return new UpdateResult(affected, new JsonArray());
  }
}
