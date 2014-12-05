package io.vertx.ext.asyncsql.impl

import com.github.mauricio.async.db.Connection
import io.vertx.core.logging.Logger
import io.vertx.core.logging.impl.LoggerFactory
import io.vertx.core.{AsyncResult, Handler, Vertx, Future => VFuture}
import io.vertx.ext.asyncsql.impl.pool.SimpleExecutionContext

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
trait BaseTransactionImpl extends CommandImplementations with TransactionCommandNames {
  val vertx: Vertx

  protected val connection: Connection
  protected val freeHandler: Connection => Future[_]

  private val logger: Logger = LoggerFactory.getLogger(super.getClass)
  private implicit val executionContext: ExecutionContext = SimpleExecutionContext(logger)

  override protected def withConnection[T](fn: Connection => Future[T]): Future[T] = fn(connection)

  def closeWithCommand(cmd: String, resultHandler: Handler[AsyncResult[Void]]): Unit = {
    connection.sendQuery(cmd) onComplete {
      case Success(_) =>
        freeHandler(connection)
        resultHandler.handle(VFuture.succeededFuture())
      case Failure(x) =>
        freeHandler(connection)
        resultHandler.handle(VFuture.failedFuture(x))
    }
  }

  def rollback(resultHandler: Handler[AsyncResult[Void]]): Unit = closeWithCommand(rollbackCommand, resultHandler)

  def commit(resultHandler: Handler[AsyncResult[Void]]): Unit = closeWithCommand(commitCommand, resultHandler)

}
