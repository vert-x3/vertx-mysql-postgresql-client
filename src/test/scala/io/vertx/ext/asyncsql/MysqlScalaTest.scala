package io.vertx.ext.asyncsql

import java.util.concurrent.CountDownLatch

import io.vertx.core.json.JsonObject
import io.vertx.core.{AsyncResult, Handler}
import io.vertx.ext.asyncsql.mysql.{MysqlConnection, MysqlService, MysqlTransaction}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class MysqlScalaTest extends SqlTestBase[MysqlTransaction, MysqlConnection, MysqlService] {
  val address = "campudus.mysql"

  override def await() = super.await()

  override def assertEquals(a: Any, b: Any) = super.assertEquals(a, b)

  override def assertNotNull(x: AnyRef) = super.assertNotNull(x)

  override def fail(msg: String) = super.fail(msg)

  lazy val config: JsonObject = new JsonObject().put("mysql", new JsonObject().put("address", address))
  override lazy val asyncsqlService = MysqlService.create(vertx, config)

  override def setUp(): Unit = {
    super.setUp()
    log.info("setting up mysqlService")
    val latch: CountDownLatch = new CountDownLatch(1)

    log.info("deployment succeeded")
    asyncsqlService.start(new Handler[AsyncResult[Void]]() {
      override def handle(event: AsyncResult[Void]): Unit = {
        log.info("mysqlService set up!")
        latch.countDown()
      }
    })

    awaitLatch(latch)
  }

  override def tearDown(): Unit = {
    log.info("tear down mysqlService!")
    val latch: CountDownLatch = new CountDownLatch(1)
    asyncsqlService.stop(new Handler[AsyncResult[Void]]() {
      override def handle(event: AsyncResult[Void]): Unit = latch.countDown()
    })
    awaitLatch(latch)
    super.tearDown()
  }

}
