package io.vertx.ext.asyncsql

import java.lang

import io.vertx.core.json.{JsonArray, JsonObject}
import io.vertx.core.logging.Logger
import io.vertx.core.logging.impl.LoggerFactory
import io.vertx.core.{AsyncResult, Handler}
import io.vertx.ext.asyncsql.impl.pool.SimpleExecutionContext
import io.vertx.test.core.VertxTestBase
import org.junit.Test

import scala.concurrent.{ExecutionContext, Future, Promise}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
abstract class SqlTestBase extends VertxTestBase {

  protected val log: Logger = LoggerFactory.getLogger(super.getClass)
  implicit val executionContext: ExecutionContext = SimpleExecutionContext.apply(log)

  type Transaction = DatabaseCommands with TransactionCommands

  type SqlService = BaseSqlService with DatabaseCommands {
    def begin(): Transaction
  }

  def asyncsqlService: SqlService

  @Test
  def simpleConnection(): Unit = {
    log.info("starting simple select test")
    (for {
      res <- arhToFuture((asyncsqlService.raw _).curried("SELECT 1 AS one"))
    } yield {
      assertNotNull(res)
      val expected = new JsonObject()
        .put("rows", 1)
        .put("results", new JsonArray().add(new JsonArray().add(1)))
        .put("fields", new JsonArray().add("one"))
      res.remove("message")
      assertEquals(expected, res)
      testComplete()
    }) recover {
      case ex: Throwable =>
        log.error("should not get this exception:", ex)
        fail(s"should not get exception.")
    }
    await()
  }

  @Test
  def simpleTransaction(): Unit = {
    (for {
      transaction <- Future.successful(asyncsqlService.begin())
      _ <- waitTick()
      res <- arhToFuture((transaction.raw _).curried("SELECT 1 AS one"))
      commit <- {
        log.info(s"got a result from first select: ${res.encode()}")
        arhToFuture(transaction.commit)
      }
    } yield {
      log.info(s"res=${res.encode()}")
      assertNotNull(res)
      val expected = new JsonObject()
        .put("rows", 1)
        .put("results", new JsonArray().add(new JsonArray().add(1)))
        .put("fields", new JsonArray().add("one"))
      res.remove("message")
      assertEquals(expected, res)
      testComplete()
    }) recover {
      case ex: Throwable =>
        log.error("should not get this exception:", ex)
        fail(s"should not get an exception: ${ex.getClass.getName}")
    }
    await()
  }

  private def arhToFuture[T](fn: Handler[AsyncResult[T]] => Unit): Future[T] = {
    val p = Promise[T]()
    fn(new Handler[AsyncResult[T]] {
      override def handle(event: AsyncResult[T]): Unit =
        if (event.succeeded()) p.success(event.result()) else p.failure(event.cause())
    })
    p.future
  }

  private def waitTick(): Future[_] = {
    val p = Promise[Unit]()
    vertx.setTimer(10, new Handler[lang.Long] {
      override def handle(event: lang.Long): Unit = p.success()
    })
    p.future
  }

}
