package io.vertx.ext.asyncsql

import java.util.concurrent.CountDownLatch

import io.vertx.core.json.JsonObject
import io.vertx.core.{AsyncResult, DeploymentOptions, Handler}
import io.vertx.ext.asyncsql.mysql.{MysqlConnection, MysqlService, MysqlTransaction}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class MysqlScalaVerticleTest extends SqlTestBase[MysqlTransaction, MysqlConnection, MysqlService] {
  val address = "campudus.mysql"

  override lazy val asyncsqlService = MysqlService.createEventBusProxy(vertx, address)

  override def setUp(): Unit = {
    super.setUp()
    log.info("setting up postgresqlService")
    val latch: CountDownLatch = new CountDownLatch(1)
    val config: JsonObject = new JsonObject().put("mysql", new JsonObject().put("address", address))
    val options: DeploymentOptions = new DeploymentOptions().setConfig(config)

    vertx.deployVerticle("service:io.vertx:mysql-postgresql-service", options, new Handler[AsyncResult[String]] {
      override def handle(event: AsyncResult[String]): Unit = {
        if (event.succeeded()) {
          log.info("deployment succeeded")
          asyncsqlService.start(new Handler[AsyncResult[Void]]() {
            override def handle(event: AsyncResult[Void]): Unit = {
              log.info("mysqlService set up!")
              latch.countDown()
            }
          })
        } else {
          fail(s"deployment failed: ${event.cause()}")
        }
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
