package io.vertx.ext.asyncsql.postgresql.impl

import java.util.UUID

import com.github.mauricio.async.db.{Configuration, Connection}
import io.netty.channel.EventLoop
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.impl.BaseSqlService
import io.vertx.ext.asyncsql.impl.pool.{PostgresqlAsyncConnectionPool, AsyncConnectionPool}
import io.vertx.ext.asyncsql.postgresql.{PostgresqlService, PostgresqlTransaction}

import scala.concurrent.{Future, Promise}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class PostgresqlServiceImpl(val vertx: Vertx, val config: JsonObject)
  extends BaseSqlService[PostgresqlTransaction, PostgresqlAsyncConnectionPool] with PostgresqlService {

  override protected val poolFactory = PostgresqlAsyncConnectionPool.apply _

  override protected val defaultHost: String = "localhost"

  override protected val defaultPort: Int = 5432

  override protected val defaultDatabase: Option[String] = Some("testdb")

  override protected val defaultUser: String = "vertx"

  override protected val defaultPassword: Option[String] = Some("test")

  override protected def createConnectionProxy(connId: String, takePromise: Promise[Connection], freeHandler: Connection => Future[_]): PostgresqlTransaction = {
    val connId = UUID.randomUUID().toString
    val address = s"$registerAddress.$connId"
    val transaction = new PostgresqlTransactionImpl(vertx, classOf[PostgresqlTransaction], takePromise, freeHandler)
    logger.info("creating transaction proxy?")
    transaction
  }

}
