package io.vertx.ext.asyncsql

import java.util.concurrent.CountDownLatch

import io.vertx.core.json.JsonObject
import io.vertx.core.{AsyncResult, DeploymentOptions, Handler}
import io.vertx.ext.asyncsql.postgresql.{PostgresqlConnection, PostgresqlService, PostgresqlTransaction}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class PostgresqlScalaVerticleTest extends SqlTestBase[PostgresqlTransaction, PostgresqlConnection, PostgresqlService] {
  val address = "campudus.postgresql"

  override lazy val asyncsqlService = PostgresqlService.createEventBusProxy(vertx, address)

  override def setUp(): Unit = {
    super.setUp()
    log.info("setting up postgresqlService")
    val latch: CountDownLatch = new CountDownLatch(1)
    val config: JsonObject = new JsonObject().put("postgresql", new JsonObject().put("address", address))
    val options: DeploymentOptions = new DeploymentOptions().setConfig(config)

    vertx.deployVerticle("service:io.vertx:mysql-postgresql-service", options, new Handler[AsyncResult[String]] {
      override def handle(event: AsyncResult[String]): Unit = {
        if (event.succeeded()) {
          log.info("deployment succeeded")
          asyncsqlService.start(new Handler[AsyncResult[Void]]() {
            override def handle(event: AsyncResult[Void]): Unit = {
              log.info("postgresqlService set up!")
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
    log.info("tear down postgresqlService!")
    val latch: CountDownLatch = new CountDownLatch(1)
    asyncsqlService.stop(new Handler[AsyncResult[Void]]() {
      override def handle(event: AsyncResult[Void]): Unit = latch.countDown()
    })
    awaitLatch(latch)
    super.tearDown()
  }

}
