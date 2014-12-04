package io.vertx.ext.asyncsql

import java.util.concurrent.CountDownLatch

import io.vertx.core.json.JsonObject
import io.vertx.core.logging.impl.LoggerFactory
import io.vertx.core.{AsyncResult, Handler}
import io.vertx.ext.asyncsql.postgresql.{PostgresqlTransaction, PostgresqlService}
import io.vertx.test.core.VertxTestBase

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class PostgresqlScalaTest extends SqlTestBase {
  val address = "campudus.postgresql"

  override def await() = super.await()

  override def assertEquals(a: Any, b: Any) = super.assertEquals(a, b)

  override def assertNotNull(x: AnyRef) = super.assertNotNull(x)

  override def fail(msg: String) = super.fail(msg)

  lazy val config: JsonObject = new JsonObject().put("postgresql", new JsonObject().put("address", address))
  override lazy val asyncsqlService = PostgresqlService.create(vertx, config)

  override def setUp(): Unit = {
    super.setUp()
    log.info("setting up postgresqlService")
    val latch: CountDownLatch = new CountDownLatch(1)

    log.info("deployment succeeded")
    asyncsqlService.start(new Handler[AsyncResult[Void]]() {
      override def handle(event: AsyncResult[Void]): Unit = {
        log.info("postgresqlService set up!")
        latch.countDown()
      }
    })

    awaitLatch(latch)
  }

  override def tearDown(): Unit = {
    log.info("tear down postgresqlService!")
    val latch: CountDownLatch = new CountDownLatch(1)
    asyncsqlService.stop(new Handler[AsyncResult[Void]]() {
      override def handle(event: AsyncResult[Void]): Unit = latch.countDown()
    })
    awaitLatch(latch)
    super.tearDown()
  }

}
