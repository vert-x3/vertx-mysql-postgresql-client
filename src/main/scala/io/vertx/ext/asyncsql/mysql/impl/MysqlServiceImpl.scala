package io.vertx.ext.asyncsql.mysql.impl

import java.util.UUID

import com.github.mauricio.async.db.{Configuration, Connection}
import io.netty.channel.EventLoop
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.impl.BaseSqlService
import io.vertx.ext.asyncsql.impl.pool.MysqlAsyncConnectionPool
import io.vertx.ext.asyncsql.mysql.{MysqlConnection, MysqlService, MysqlTransaction}

import scala.concurrent.{Future, Promise}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class MysqlServiceImpl(val vertx: Vertx, val config: JsonObject)
  extends BaseSqlService[MysqlConnection, MysqlTransaction, MysqlAsyncConnectionPool] with MysqlService with MysqlOverrides {

  override protected val poolFactory = MysqlAsyncConnectionPool.apply _

  override protected val defaultHost: String = "localhost"

  override protected val defaultPort: Int = 3306

  override protected val defaultDatabase: Option[String] = Some("testdb")

  override protected val defaultUser: String = "root"

  override protected val defaultPassword: Option[String] = None

  override protected def createTransactionProxy(connection: Connection, freeHandler: Connection => Future[_]): MysqlTransaction = {
    new MysqlTransactionImpl(vertx, connection, freeHandler)
  }

  override protected def createConnectionProxy(connection: Connection, freeHandler: Connection => Future[_]): MysqlConnection = {
    new MysqlConnectionImpl(vertx, connection, freeHandler)
  }

}
