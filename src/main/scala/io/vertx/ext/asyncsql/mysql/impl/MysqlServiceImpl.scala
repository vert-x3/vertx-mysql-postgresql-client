package io.vertx.ext.asyncsql.mysql.impl

import java.util.UUID

import com.github.mauricio.async.db.{Configuration, Connection}
import io.netty.channel.EventLoop
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.impl.BaseSqlService
import io.vertx.ext.asyncsql.impl.pool.MysqlAsyncConnectionPool
import io.vertx.ext.asyncsql.mysql.{MysqlService, MysqlTransaction}

import scala.concurrent.{Future, Promise}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class MysqlServiceImpl(val vertx: Vertx, val config: JsonObject)
  extends BaseSqlService[MysqlTransaction, MysqlAsyncConnectionPool] with MysqlService {

  override protected val poolFactory = MysqlAsyncConnectionPool.apply _

  override protected val defaultHost: String = "localhost"

  override protected val defaultPort: Int = 3306

  override protected val defaultDatabase: Option[String] = Some("testdb")

  override protected val defaultUser: String = "root"

  override protected val defaultPassword: Option[String] = None

  override protected def createConnectionProxy(connId: String, takePromise: Promise[Connection], freeHandler: Connection => Future[_]): MysqlTransaction = {
    val connId = UUID.randomUUID().toString
    val address = s"$registerAddress.$connId"
    val transaction = new MysqlTransactionImpl(vertx, classOf[MysqlTransaction], takePromise, freeHandler)
    transaction
  }

}
