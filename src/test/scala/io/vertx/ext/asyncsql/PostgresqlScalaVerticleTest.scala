package io.vertx.ext.asyncsql

import java.util.concurrent.CountDownLatch
import java.util.function.Consumer

import io.vertx.core.json.{JsonArray, JsonObject}
import io.vertx.core.logging.Logger
import io.vertx.core.logging.impl.LoggerFactory
import io.vertx.core.{DeploymentOptions, AsyncResult, Handler}
import io.vertx.ext.asyncsql.postgresql.{PostgresqlTransaction, PostgresqlService}
import io.vertx.proxygen.ProxyHelper
import io.vertx.test.core.VertxTestBase
import org.junit.{Ignore, Test}

import scala.concurrent.{Future, Promise}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class PostgresqlScalaVerticleTest extends SqlTestBase {
  val address = "campudus.postgresql"


  override lazy val asyncsqlService: SqlService = PostgresqlService.createEventBusProxy(vertx, address)

  override def setUp(): Unit = {
    super.setUp()
    log.info("setting up postgresqlService")
    val latch: CountDownLatch = new CountDownLatch(1)
    val config: JsonObject = new JsonObject().put("postgresql", new JsonObject().put("address", address))
    val options: DeploymentOptions = new DeploymentOptions().setConfig(config)

    vertx.deployVerticle("service:io.vertx:ext-mysql-postgresql", options, new Handler[AsyncResult[String]] {
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
