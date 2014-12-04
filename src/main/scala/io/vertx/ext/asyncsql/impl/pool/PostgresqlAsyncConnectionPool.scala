package io.vertx.ext.asyncsql.impl.pool

import com.github.mauricio.async.db.Configuration
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import io.netty.channel.EventLoop
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.impl.LoggerFactory

import scala.concurrent.ExecutionContext

class PostgresqlAsyncConnectionPool(val vertx: Vertx, config: Configuration, eventLoop: EventLoop, val maxPoolSize: Int) extends AsyncConnectionPool {

  private val logger: Logger = LoggerFactory.getLogger(classOf[PostgresqlAsyncConnectionPool])

  private implicit val executionContext: ExecutionContext = SimpleExecutionContext(logger)

  override def create() = new PostgreSQLConnection(
    configuration = config,
    group = eventLoop,
    executionContext = executionContext
  ).connect

}

object PostgresqlAsyncConnectionPool {
  def apply(vertx: Vertx, config: Configuration, eventLoop: EventLoop, maxPoolSize: Int): PostgresqlAsyncConnectionPool =
    new PostgresqlAsyncConnectionPool(vertx, config, eventLoop, maxPoolSize)
}
