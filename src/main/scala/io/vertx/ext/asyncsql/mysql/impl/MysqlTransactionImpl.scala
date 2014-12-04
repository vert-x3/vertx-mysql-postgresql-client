package io.vertx.ext.asyncsql.mysql.impl

import com.github.mauricio.async.db.Connection
import io.vertx.core.Vertx
import io.vertx.ext.asyncsql.impl.BaseTransactionImpl
import io.vertx.ext.asyncsql.mysql.MysqlTransaction

import scala.concurrent.{Future, Promise}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class MysqlTransactionImpl(val vertx: Vertx,
                           override protected val transactionClass: Class[MysqlTransaction],
                           override protected val takePromise: Promise[Connection],
                           override protected val freeHandler: Connection => Future[_])
  extends BaseTransactionImpl[MysqlTransaction] with MysqlTransaction {

}
