package io.vertx.ext.asyncsql.impl

import com.github.mauricio.async.db.Configuration
import io.netty.channel.EventLoop
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.impl.LoggerFactory
import io.vertx.core.{AsyncResult, Future => VFuture, Handler, Vertx}
import io.vertx.ext.asyncsql.impl.pool.{AsyncConnectionPool, SimpleExecutionContext}
import io.vertx.ext.sql.SQLConnection

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
trait BaseSQLClient {

  val vertx: Vertx
  val config: JsonObject

  protected final val logger: Logger = LoggerFactory.getLogger(super.getClass)
  protected implicit val executionContext: ExecutionContext = SimpleExecutionContext(logger)

  protected def defaultHost: String

  protected def defaultPort: Int

  protected def defaultDatabase: Option[String]

  protected def defaultUser: String

  protected def defaultPassword: Option[String]

  protected val poolFactory: (Vertx, Configuration, EventLoop, Int) => AsyncConnectionPool

  protected lazy val maxPoolSize: Integer = config.getInteger("maxPoolSize", 10)
  protected lazy val transactionTimeout: Integer = config.getInteger("transactionTimeout", 500)
  protected lazy val configuration: Configuration = getConfiguration(config)
  protected lazy val pool: AsyncConnectionPool = AsyncConnectionPool(vertx, maxPoolSize, configuration, poolFactory)
  protected lazy val registerAddress: String = config.getString("address")

  def getConnection(handler: Handler[AsyncResult[SQLConnection]]): Unit = {
    pool.take() onComplete {
      case Success(conn) =>
        val connection = new AsyncSQLConnectionImpl(conn, pool)(executionContext)
        handler.handle(VFuture.succeededFuture(connection))
      case Failure(ex) =>
        handler.handle(VFuture.failedFuture(ex))
    }
  }

  def close(stopped: Handler[AsyncResult[Void]]): Unit = {
    logger.info(s"Stopping AsyncSqlClient:${getClass.getName}")
    pool.close() onComplete {
      case Success(p) => stopped.handle(VFuture.succeededFuture())
      case Failure(ex) => stopped.handle(VFuture.failedFuture(ex))
    }
  }

  private def getConfiguration(config: JsonObject) = {
    val host = config.getString("host", defaultHost)
    val port = config.getInteger("port", defaultPort)
    val username = config.getString("username", defaultUser)
    val password = Option(config.getString("password")).orElse(defaultPassword)
    val database = Option(config.getString("database")).orElse(defaultDatabase)

    logger.info(s"host=$host, defaultHost=$defaultHost")
    Configuration(username, host, port, password, database)
  }

}
