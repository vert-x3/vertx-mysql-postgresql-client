package io.vertx.ext.asyncsql.impl.pool

import com.github.mauricio.async.db.{Configuration, Connection}
import io.netty.channel.EventLoop
import io.vertx.core.Vertx
import io.vertx.core.impl.EventLoopContext
import io.vertx.core.logging.Logger
import io.vertx.core.logging.impl.LoggerFactory

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

trait AsyncConnectionPool {

  val maxPoolSize: Int
  val vertx: Vertx

  private val logger: Logger = LoggerFactory.getLogger(classOf[AsyncConnectionPool])

  private implicit val executionContext: ExecutionContext = SimpleExecutionContext(logger)

  private var poolSize: Int = 0
  private val availableConnections: mutable.Queue[Connection] = mutable.Queue.empty
  private val waiters: mutable.Queue[Promise[Connection]] = mutable.Queue.empty

  def create(): Future[Connection]

  private def createConnection(): Future[Connection] = {
    poolSize += 1
    create() recoverWith {
      case ex: Throwable =>
        logger.info(s"creating a connection went wrong: $ex")
        poolSize -= 1
        Future.failed(ex)
    }
  }

  private def waitForAvailableConnection(): Future[Connection] = {
    val p = Promise[Connection]()
    waiters.enqueue(p)
    p.future
  }

  private def createOrWaitForAvailableConnection(): Future[Connection] = {
    if (poolSize < maxPoolSize) {
      createConnection()
    } else {
      waitForAvailableConnection()
    }
  }

  def take(): Future[Connection] = availableConnections.dequeueFirst(_ => true) match {
    case Some(connection) =>
      if (connection.isConnected) {
        Future.successful(connection)
      } else {
        poolSize -= 1
        take()
      }
    case None => createOrWaitForAvailableConnection()
  }

  private def notifyWaitersAboutAvailableConnection(): Future[_] = {
    waiters.dequeueFirst(_ => true) match {
      case Some(waiter) =>
        waiter.completeWith(take())
        waiter.future
      case None =>
        Future.successful()
    }
  }

  def giveBack(conn: Connection)(implicit ec: ExecutionContext) = {
    if (conn.isConnected) {
      availableConnections.enqueue(conn)
    } else {
      poolSize -= 1
    }
    notifyWaitersAboutAvailableConnection()
  }

  def close(): Future[AsyncConnectionPool] = {
    Future.sequence(availableConnections.map(_.disconnect)) map (_ => this)
  }

  def withConnection[ResultType](fn: Connection => Future[ResultType]): Future[ResultType] = {
    val p = Promise[ResultType]()
    take map { c: Connection =>
      try {
        fn(c).onComplete {
          case Success(x) =>
            giveBack(c)
            p.success(x)
          case Failure(x) =>
            giveBack(c)
            p.failure(x)
        }
      } catch {
        case ex: Throwable =>
          giveBack(c)
          p.failure(ex)
      }
    } recover {
      case ex => p.failure(ex)
    }
    p.future
  }

}

object AsyncConnectionPool {

  def apply[T <: AsyncConnectionPool](vertx: Vertx, maxPoolSize: Int, config: Configuration, factoryFn: (Vertx, Configuration, EventLoop, Int) => T) = {
    factoryFn(vertx,
      config,
      vertx.getOrCreateContext.asInstanceOf[EventLoopContext].getEventLoop,
      maxPoolSize)
  }

}