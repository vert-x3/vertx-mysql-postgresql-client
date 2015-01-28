package io.vertx.ext.asyncsql

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
abstract class SqlTestBase extends VertxTestBase with TestData {

  protected val log: Logger = LoggerFactory.getLogger(super.getClass)
  implicit val executionContext: ExecutionContext = SimpleExecutionContext.apply(log)

  def config: JsonObject

  def asyncSqlService: AsyncSqlService

  @Test
  def simpleConnection(): Unit = completeTest {
    def checkResult(res: ResultSet): Unit = {
      log.info(s"checking result ${res.toJson.encode()}")

      assertNotNull(res)
      val expected = new JsonObject()
        .put("columnNames", new JsonArray().add("something"))
        .put("results", new JsonArray().add(new JsonArray().add(1)))
      assertEquals(expected, res.toJson)
      log.info(s"checkResult end")
    }

    for {
      conn <- arhToFuture(asyncSqlService.getConnection _)
      res <- {
        log.info(s"got a connection $conn")
        arhToFuture((conn.query _).curried("SELECT 1 AS something")(null))
      }
      _ <- Future.successful(checkResult(res))
      _ <- {
        log.info("closing connection")
        arhToFuture(conn.close _)
      }
    } yield {
      log.info("done with simpleConnection test")
      ()
    }
  }

  //  @Test
  //  def insert(): Unit = completeTest {
  //    import scala.collection.JavaConverters._
  //    val id = 27L
  //    val name = "Adele"
  //
  //    for {
  //      _ <- setupSimpleTestTable
  //      i <- arhToFuture((asyncsqlService.insert _).curried("test_table")(List("id", "name").asJava)(List(new JsonArray().add(id).add(name)).asJava))
  //      s <- arhToFuture((asyncsqlService.select _).curried("test_table")(new SelectOptions().setFields(new JsonArray().add("id").add("name"))))
  //    } yield {
  //      log.info(s"result = ${s.encodePrettily()}")
  //      val results = s.getJsonArray("results")
  //      val fields = s.getJsonArray("fields").getList.asScala
  //      assertTrue("Should have an id field", fields.contains("id"))
  //      assertTrue("Should have a name field", fields.contains("name"))
  //      assertEquals(names.zipWithIndex.map(_.swap) ++ List((id, name)), results.getList.asScala.map { x =>
  //        val arr = x.asInstanceOf[JsonArray]
  //        (arr.getLong(0), arr.getString(1))
  //      }.toList)
  //    }
  //  }
  //
  //  @Test
  //  def prepared(): Unit = completeTest {
  //    import scala.collection.JavaConverters._
  //    val id = 27
  //    val name = "Adele"
  //
  //    for {
  //      _ <- setupSimpleTestTable
  //      p <- arhToFuture((asyncsqlService.prepared _).curried("INSERT INTO test_table (id, name) VALUES (?, ?)")(new JsonArray().add(id).add(name)))
  //      s <- arhToFuture((asyncsqlService.select _).curried("test_table")(new SelectOptions().setFields(new JsonArray().add("id").add("name"))))
  //    } yield {
  //      log.info(s"result = ${s.encodePrettily()}")
  //      val results = s.getJsonArray("results")
  //      val fields = s.getJsonArray("fields").getList.asScala
  //      assertTrue("Should have an id field", fields.contains("id"))
  //      assertTrue("Should have a name field", fields.contains("name"))
  //      assertEquals(names.zipWithIndex.map(_.swap) ++ List((id, name)), results.getList.asScala.map { x =>
  //        val arr = x.asInstanceOf[JsonArray]
  //        (arr.getLong(0), arr.getString(1))
  //      }.toList)
  //    }
  //  }

  private def arhToFuture[T](fn: Handler[AsyncResult[T]] => _): Future[T] = {
    val p = Promise[T]()
    fn(new Handler[AsyncResult[T]] {
      override def handle(event: AsyncResult[T]): Unit =
        if (event.succeeded()) p.success(event.result()) else p.failure(event.cause())
    })
    p.future
  }

  private def completeTest(f: Future[_]): Unit = {
    f map { _ =>
      log.info("done with test")
      testComplete()
    } recover {
      case ex: Throwable =>
        log.error("should not get this exception", ex)
        fail("got exception")
    }

    await()
  }

  private def setupSimpleTestTable: Future[Unit] = {
    for {
      conn <- arhToFuture(asyncSqlService.getConnection _)
      _ <- arhToFuture((conn.query _).curried("BEGIN;")(null))
      _ <- arhToFuture((conn.query _).curried("DROP TABLE IF EXISTS test_table;")(null))
      _ <- arhToFuture((conn.query _).curried(
        """CREATE TABLE test_table (
          |  id BIGINT,
          |  name VARCHAR(255)
          |);""".stripMargin)(null))
      _ <- arhToFuture((conn.query _).curried(s"INSERT INTO test_table (id, name) VALUES ${simpleTestTable.mkString(",")};")(null))
      _ <- arhToFuture(conn.commit _)
      _ <- arhToFuture(conn.close _)
    } yield ()
  }

}
