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
  protected val takePromise: Promise[Connection]
  protected val freeHandler: Connection => Future[_]

  private val logger: Logger = LoggerFactory.getLogger(super.getClass)
  private implicit val executionContext: ExecutionContext = SimpleExecutionContext(logger)
  private val connection: Future[Connection] = takePromise.future

  override protected def withConnection[T](fn: Connection => Future[T]): Future[T] = {
    logger.info("withConnection in TransactionImpl")
    connection flatMap { c =>
      logger.info("transaction -> got a connection, applying fn to it")
      fn(c)
    }
  }

  protected def rollbackCommand: String = "ROLLBACK"

  protected def commitCommand: String = "COMMIT"

  def rollback(resultHandler: Handler[AsyncResult[Void]]): Unit = {
    for {
      conn <- connection
    } yield {
      conn.sendQuery(rollbackCommand) map {
        _ => resultHandler.handle(VFuture.succeededFuture())
      }
      freeHandler(conn)
    }
  }

  def commit(resultHandler: Handler[AsyncResult[String]]): Unit = {
    for {
      conn <- connection
    } yield {
      conn.sendQuery(commitCommand) map {
        _ => resultHandler.handle(VFuture.succeededFuture(""))
      }
      freeHandler(conn)
    }
  }

}
