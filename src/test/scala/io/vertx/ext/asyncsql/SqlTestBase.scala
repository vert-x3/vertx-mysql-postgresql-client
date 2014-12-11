package io.vertx.ext.asyncsql

import java.lang

import io.vertx.core.json.{JsonArray, JsonObject}
import io.vertx.core.logging.Logger
import io.vertx.core.logging.impl.LoggerFactory
import io.vertx.core.{AsyncResult, Handler}
import io.vertx.ext.asyncsql.impl.pool.SimpleExecutionContext
import io.vertx.test.core.VertxTestBase
import org.junit.{Ignore, Test}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Try, Failure, Success}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
abstract class SqlTestBase[Transaction <: DatabaseCommands with TransactionCommands, Connection <: DatabaseCommands with ConnectionCommands, SqlService <: BaseSqlService with DatabaseCommands {
  def begin(transaction : Handler[AsyncResult[Transaction]])
  def take(connection : Handler[AsyncResult[Connection]])
}] extends VertxTestBase with TestData {

  protected val log: Logger = LoggerFactory.getLogger(super.getClass)
  implicit val executionContext: ExecutionContext = SimpleExecutionContext.apply(log)

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
      transaction <- beginTransaction()
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

  @Test
  def simpleUseConnection(): Unit = {
    (for {
      connection <- takeConnection()
      res <- arhToFuture((connection.raw _).curried("SELECT 1 AS one"))
      commit <- {
        log.info(s"got a result from first select: ${res.encode()}")
        arhToFuture(connection.close)
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

  @Test
  def selectWithEmptyOptions(): Unit = completeTest(
    for {
      _ <- setupSimpleTestTable
      s <- arhToFuture((asyncsqlService.select _).curried("test_table")(new SelectOptions()))
    } yield {
      val json = s.getJsonArray("results")
      assertEquals(26, json.size())
    }
  )

  @Test
  def selectWithFields(): Unit = completeTest(
    for {
      _ <- setupSimpleTestTable
      s <- arhToFuture((asyncsqlService.select _).curried("test_table")(new SelectOptions().setFields(new JsonArray().add("name"))))
    } yield {
      assertEquals(26, s.getInteger("rows"))
      val results = s.getJsonArray("results")
      assertEquals(26, results.size())
      import collection.JavaConverters._
      assertEquals(names, results.getList.asScala.map(_.asInstanceOf[JsonArray].getString(0)))
    }
  )

  @Test
  def selectWithLimit(): Unit = completeTest {
    import collection.JavaConverters._
    val expectedResults = 10

    for {
      _ <- setupSimpleTestTable
      s <- arhToFuture((asyncsqlService.select _).curried("test_table")(new SelectOptions().setLimit(expectedResults)))
    } yield {
      log.info(s"result = ${s.encodePrettily()}")
      assertEquals(expectedResults, s.getInteger("rows"))
      val results = s.getJsonArray("results")
      assertEquals(expectedResults, results.size())
      val fields = s.getJsonArray("fields").getList.asScala
      assertEquals(2, fields.length)
      val idIdx = fields.indexOf("id")
      val nameIdx = fields.indexOf("name")
      val expected = names.zipWithIndex.take(expectedResults)
      assertEquals(expected, results.getList.asScala.map { x =>
        val arr = x.asInstanceOf[JsonArray]
        (arr.getString(nameIdx), arr.getLong(idIdx))
      }.toList)
    }
  }

  @Test
  def selectWithOffset(): Unit = completeTest {
    import collection.JavaConverters._
    val expectedOffset = 10

    for {
      _ <- setupSimpleTestTable
      s <- arhToFuture((asyncsqlService.select _).curried("test_table")(new SelectOptions().setOffset(expectedOffset)))
    } yield {
      log.info(s"result = ${s.encodePrettily()}")
      assertEquals(names.length - expectedOffset, s.getInteger("rows"))
      val results = s.getJsonArray("results")
      assertEquals(names.length - expectedOffset, results.size())
      val fields = s.getJsonArray("fields").getList.asScala
      assertEquals(2, fields.length)
      val idIdx = fields.indexOf("id")
      val nameIdx = fields.indexOf("name")
      val expected = names.zipWithIndex.drop(expectedOffset)
      assertEquals(expected, results.getList.asScala.map { x =>
        val arr = x.asInstanceOf[JsonArray]
        (arr.getString(nameIdx), arr.getLong(idIdx))
      }.toList)
    }
  }

  @Test
  def selectWithLimitAndOffset(): Unit = completeTest {
    import collection.JavaConverters._
    val expectedLimit = 10
    val expectedOffset = 10

    for {
      _ <- setupSimpleTestTable
      s <- arhToFuture((asyncsqlService.select _).curried("test_table")(new SelectOptions().setLimit(expectedLimit).setOffset(expectedOffset)))
    } yield {
      log.info(s"result = ${s.encodePrettily()}")
      assertEquals(expectedLimit, s.getInteger("rows"))
      val results = s.getJsonArray("results")
      assertEquals(expectedLimit, results.size())
      val fields = s.getJsonArray("fields").getList.asScala
      assertEquals(2, fields.length)
      val idIdx = fields.indexOf("id")
      val nameIdx = fields.indexOf("name")
      val expected = names.zipWithIndex.drop(expectedOffset).take(expectedLimit)
      assertEquals(expected, results.getList.asScala.map { x =>
        val arr = x.asInstanceOf[JsonArray]
        (arr.getString(nameIdx), arr.getLong(idIdx))
      }.toList)
    }
  }

  @Test
  def selectWithFieldsAndLimitAndOffset(): Unit = completeTest {
    import collection.JavaConverters._
    val expectedLimit = 10
    val expectedOffset = 10

    for {
      _ <- setupSimpleTestTable
      s <- arhToFuture((asyncsqlService.select _).curried("test_table")(new SelectOptions().setFields(new JsonArray().add("name")).setLimit(expectedLimit).setOffset(expectedOffset)))
    } yield {
      log.info(s"result = ${s.encodePrettily()}")
      assertEquals(expectedLimit, s.getInteger("rows"))
      val results = s.getJsonArray("results")
      assertEquals(expectedLimit, results.size())
      val fields = s.getJsonArray("fields").getList.asScala
      assertEquals(1, fields.length)
      val expected = names.drop(expectedOffset).take(expectedLimit)
      assertEquals(expected, results.getList.asScala.map { x =>
        val arr = x.asInstanceOf[JsonArray]
        arr.getString(0)
      }.toList)
    }
  }

  @Test
  def insert(): Unit = completeTest {
    import collection.JavaConverters._
    val id = 27L
    val name = "Adele"

    for {
      _ <- setupSimpleTestTable
      i <- arhToFuture((asyncsqlService.insert _).curried("test_table")(List("id", "name").asJava)(List(new JsonArray().add(id).add(name)).asJava))
      s <- arhToFuture((asyncsqlService.select _).curried("test_table")(new SelectOptions().setFields(new JsonArray().add("id").add("name"))))
    } yield {
      log.info(s"result = ${s.encodePrettily()}")
      val results = s.getJsonArray("results")
      val fields = s.getJsonArray("fields").getList.asScala
      assertTrue("Should have an id field", fields.contains("id"))
      assertTrue("Should have a name field", fields.contains("name"))
      assertEquals(names.zipWithIndex.map(_.swap) ++ List((id, name)), results.getList.asScala.map { x =>
        val arr = x.asInstanceOf[JsonArray]
        (arr.getLong(0), arr.getString(1))
      }.toList)
    }
  }

  @Test
  def prepared(): Unit = completeTest {
    import collection.JavaConverters._
    val id = 27
    val name = "Adele"

    for {
      _ <- setupSimpleTestTable
      p <- arhToFuture((asyncsqlService.prepared _).curried("INSERT INTO test_table (id, name) VALUES (?, ?)")(new JsonArray().add(id).add(name)))
      s <- arhToFuture((asyncsqlService.select _).curried("test_table")(new SelectOptions().setFields(new JsonArray().add("id").add("name"))))
    } yield {
      log.info(s"result = ${s.encodePrettily()}")
      val results = s.getJsonArray("results")
      val fields = s.getJsonArray("fields").getList.asScala
      assertTrue("Should have an id field", fields.contains("id"))
      assertTrue("Should have a name field", fields.contains("name"))
      assertEquals(names.zipWithIndex.map(_.swap) ++ List((id, name)), results.getList.asScala.map { x =>
        val arr = x.asInstanceOf[JsonArray]
        (arr.getLong(0), arr.getString(1))
      }.toList)
    }
  }

  private def arhToFuture[T](fn: Handler[AsyncResult[T]] => Unit): Future[T] = {
    val p = Promise[T]()
    fn(new Handler[AsyncResult[T]] {
      override def handle(event: AsyncResult[T]): Unit =
        if (event.succeeded()) p.success(event.result()) else p.failure(event.cause())
    })
    p.future
  }

  private def takeConnection(): Future[Connection] = arhToFuture(asyncsqlService.take)

  private def beginTransaction(): Future[Transaction] = arhToFuture(asyncsqlService.begin)

  private def completeTest(f: Future[_]): Unit = {
    f map (_ => testComplete()) recover {
      case ex: Throwable =>
        log.error("should not get this exception", ex)
        fail("got exception")
    }
    await()
  }

  private def setupSimpleTestTable: Future[Unit] = {
    for {
      t <- arhToFuture(asyncsqlService.begin)
      _ <- arhToFuture((t.raw _).curried("DROP TABLE IF EXISTS test_table;"))
      _ <- arhToFuture((t.raw _).curried(
        """CREATE TABLE test_table (
          |  id BIGINT,
          |  name VARCHAR(255)
          |);""".stripMargin))
      _ <- arhToFuture((t.raw _).curried(s"INSERT INTO test_table (id, name) VALUES ${simpleTestTable.mkString(",")};"))
      _ <- arhToFuture(t.commit)
    } yield ()
  }

}
