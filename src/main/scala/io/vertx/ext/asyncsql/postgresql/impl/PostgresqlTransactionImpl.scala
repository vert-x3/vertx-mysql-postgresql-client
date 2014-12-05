package io.vertx.ext.asyncsql.postgresql.impl

import com.github.mauricio.async.db.Connection
import io.vertx.core.Vertx
import io.vertx.ext.asyncsql.impl.BaseTransactionImpl
import io.vertx.ext.asyncsql.postgresql.PostgresqlTransaction

import scala.concurrent.{Future, Promise}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class PostgresqlTransactionImpl(val vertx: Vertx,
                                override protected val connection: Connection,
                                override protected val freeHandler: Connection => Future[_])
  extends BaseTransactionImpl with PostgresqlTransaction {

}
