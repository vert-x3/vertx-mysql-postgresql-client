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

  import scala.collection.JavaConverters._

  def config: JsonObject

  def asyncSqlService: AsyncSqlService

  @Test
  def simpleConnection(): Unit = completeTest {
    for {
      conn <- arhToFuture(asyncSqlService.getConnection _)
      res <- arhToFuture((conn.query _).curried("SELECT 1 AS something"))
    } yield {
      assertNotNull(res)
      val expected = new JsonObject()
        .put("columnNames", new JsonArray().add("something"))
        .put("results", new JsonArray().add(new JsonArray().add(1)))
      assertEquals(expected, res.toJson)
      conn
    }
  }

  @Test
  def simpleSelect(): Unit = completeTest {
    for {
      conn <- arhToFuture(asyncSqlService.getConnection _)
      _ <- setupSimpleTestTable(conn)
      res <- arhToFuture((conn.queryWithParams _).curried("SELECT name FROM test_table WHERE id=?")(new JsonArray().add(2)))
    } yield {
      assertNotNull(res)
      assertEquals(List("name"), res.getColumnNames.asScala)
      assertEquals(names(2), res.getResults.get(0).getString(0))
      conn
    }
  }

  @Test
  def updateRow(): Unit = completeTest {
    val id = 0
    val name = "Adele"
    for {
      conn <- arhToFuture(asyncSqlService.getConnection _)
      _ <- setupSimpleTestTable(conn)
      updateRes <- arhToFuture((conn.updateWithParams _).curried("UPDATE test_table SET name=? WHERE id=?")(new JsonArray().add(name).add(id)))
      selectRes <- arhToFuture((conn.query _).curried("SELECT name FROM test_table ORDER BY id"))
    } yield {
      assertNotNull(updateRes)
      assertNotNull(selectRes)
      assertEquals(1, updateRes.getUpdated)
      assertEquals(names.map(n => new JsonArray().add(if (n == "Albert") name else n)), selectRes.getResults.asScala)
      conn
    }
  }

  @Test
  def rollingBack(): Unit = completeTest {
    val id = 0
    val name = "Adele"

    for {
      conn <- arhToFuture(asyncSqlService.getConnection _)
      _ <- setupSimpleTestTable(conn)
      _ <- arhToFuture((conn.execute _).curried("BEGIN"))
      updateRes <- arhToFuture((conn.updateWithParams _).curried("UPDATE test_table SET name=? WHERE id=?")(new JsonArray().add(name).add(id)))
      selectRes <- arhToFuture((conn.query _).curried("SELECT name FROM test_table ORDER BY id"))
      _ <- Future.successful {
        assertNotNull(updateRes)
        assertNotNull(selectRes)
        assertEquals(1, updateRes.getUpdated)
        assertEquals(names.map(n => new JsonArray().add(if (n == names(id)) name else n)), selectRes.getResults.asScala)
      }
      _ <- arhToFuture(conn.rollback _)
      res <- arhToFuture((conn.query _).curried("SELECT name FROM test_table ORDER BY id"))
    } yield {
      assertNotNull(res)
      assertEquals(names.map(n => new JsonArray().add(n)), res.getResults.asScala)
      conn
    }
  }

  @Test
  def multipleConnections(): Unit = completeTest {
    val id = 0
    val name = "Adele"

    for {
      conn <- arhToFuture(asyncSqlService.getConnection _)
      _ <- setupSimpleTestTable(conn)
      c1 <- arhToFuture(asyncSqlService.getConnection _)
      c2 <- arhToFuture(asyncSqlService.getConnection _)
      _ <- arhToFuture((c1.execute _).curried("BEGIN"))
      c1Update <- arhToFuture((c1.updateWithParams _).curried("UPDATE test_table SET name=? WHERE id=?")(new JsonArray().add(name).add(id)))
      c2Select <- arhToFuture((c2.query _).curried("SELECT name FROM test_table ORDER BY id"))
      _ <- arhToFuture(c1.rollback _)
      _ <- arhToFuture(c1.close _)
      _ <- arhToFuture(c2.close _)
    } yield {
      assertEquals(1, c1Update.getUpdated)
      assertEquals(names.map(n => new JsonArray().add(n)), c2Select.getResults.asScala)
      conn
    }
  }

  @Test
  def insert(): Unit = completeTest {
    val id = 27L
    val name = "Adele"

    for {
      c <- arhToFuture(asyncSqlService.getConnection _)
      _ <- setupSimpleTestTable(c)
      i <- arhToFuture((c.updateWithParams _).curried("INSERT INTO test_table (id, name) VALUES (?, ?)")(new JsonArray().add(id).add(name)))
      s <- arhToFuture((c.query _).curried("SELECT id, name FROM test_table ORDER BY id"))
    } yield {
      val results = s.getResults
      val fields = s.getColumnNames.asScala
      assertEquals(List("id", "name"), fields)
      assertEquals(names.zipWithIndex.map(_.swap) ++ List((id, name)), results.asScala.map { arr =>
        (arr.getLong(0), arr.getString(1))
      }.toList)
      c
    }
  }

  protected def arhToFuture[T](fn: Handler[AsyncResult[T]] => _): Future[T] = {
    val p = Promise[T]()
    fn(new Handler[AsyncResult[T]] {
      override def handle(event: AsyncResult[T]): Unit =
        if (event.succeeded()) p.success(event.result()) else p.failure(event.cause())
    })
    p.future
  }

  private def completeTest(f: Future[AsyncSqlConnection]): Unit = {
    f flatMap {
      conn =>
        arhToFuture(conn.close _) map {
          _ =>
            testComplete()
        }
    } recover {
      case ex: Throwable =>
        log.error("should not get this exception", ex)
        fail("got exception")
    }

    await()
  }

  private def setupSimpleTestTable(conn: AsyncSqlConnection): Future[AsyncSqlConnection] = {
    for {
      _ <- arhToFuture((conn.execute _).curried("BEGIN;"))
      _ <- arhToFuture((conn.execute _).curried("DROP TABLE IF EXISTS test_table;"))
      _ <- arhToFuture((conn.execute _).curried(
        """CREATE TABLE test_table (
          |  id BIGINT,
          |  name VARCHAR(255)
          |);""".stripMargin))
      _ <- arhToFuture((conn.update _).curried(s"INSERT INTO test_table (id, name) VALUES ${
        simpleTestTable.mkString(",")
      };"))
      _ <- arhToFuture(conn.commit _)
    } yield conn
  }

}
