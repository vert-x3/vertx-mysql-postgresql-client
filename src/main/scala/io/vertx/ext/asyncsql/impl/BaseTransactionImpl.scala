package io.vertx.ext.asyncsql.impl

import com.github.mauricio.async.db.Connection
import io.vertx.core.logging.Logger
import io.vertx.core.logging.impl.LoggerFactory
import io.vertx.core.{AsyncResult, Handler, Vertx, Future => VFuture}
import io.vertx.ext.asyncsql.impl.pool.SimpleExecutionContext

import scala.concurrent.{ExecutionContext, Future, Promise}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
trait BaseTransactionImpl[TransactionType] extends CommandImplementations {
  val vertx: Vertx

  protected val transactionClass: Class[TransactionType]
  protected val connection: Connection
  protected val freeHandler: Connection => Future[_]

  private val logger: Logger = LoggerFactory.getLogger(super.getClass)
  private implicit val executionContext: ExecutionContext = SimpleExecutionContext(logger)

  override protected def withConnection[T](fn: Connection => Future[T]): Future[T] = {
    logger.info("withConnection in TransactionImpl")
    fn(connection)
  }

  protected def rollbackCommand: String = "ROLLBACK"

  protected def commitCommand: String = "COMMIT"

  def closeWithCommand(cmd: String, resultHandler: Handler[AsyncResult[Void]]): Unit = {
    connection.sendQuery(cmd) map {
      _ => resultHandler.handle(VFuture.succeededFuture())
    }
    freeHandler(connection)
  }
  def rollback(resultHandler: Handler[AsyncResult[Void]]): Unit = closeWithCommand(rollbackCommand, resultHandler)

  def commit(resultHandler: Handler[AsyncResult[Void]]): Unit = closeWithCommand(commitCommand, resultHandler)

}
