package io.vertx.ext.asyncsql.postgresql.impl

import com.github.mauricio.async.db.Connection
import io.vertx.core.Vertx
import io.vertx.ext.asyncsql.impl.{BaseConnectionImpl, BaseTransactionImpl}
import io.vertx.ext.asyncsql.postgresql.{PostgresqlConnection, PostgresqlTransaction}

import scala.concurrent.Future

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class PostgresqlConnectionImpl(val vertx: Vertx,
                               override protected val connection: Connection,
                               override protected val freeHandler: Connection => Future[_])
  extends BaseConnectionImpl with PostgresqlConnection {

}
