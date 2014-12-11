package io.vertx.ext.asyncsql.mysql.impl

import com.github.mauricio.async.db.Connection
import io.vertx.core.Vertx
import io.vertx.ext.asyncsql.impl.BaseConnectionImpl
import io.vertx.ext.asyncsql.mysql.MysqlConnection

import scala.concurrent.Future

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class MysqlConnectionImpl(val vertx: Vertx,
                               override protected val connection: Connection,
                               override protected val freeHandler: Connection => Future[_])
  extends BaseConnectionImpl with MysqlConnection with MysqlOverrides {

}
