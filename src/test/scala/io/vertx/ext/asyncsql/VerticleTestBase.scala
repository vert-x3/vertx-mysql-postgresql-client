package io.vertx.ext.asyncsql

import java.util.concurrent.CountDownLatch

import io.vertx.core.{AsyncResult, Handler, DeploymentOptions}
import org.junit.Test

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
abstract class VerticleTestBase extends SqlTestBase with ConfigProvider {

  override lazy val asyncSqlService = AsyncSqlService.createEventBusProxy(vertx, address)
  var deploymentId: Option[String] = None

  override def setUp(): Unit = {
    super.setUp()
    log.info(s"Setting up service at $address via Verticle with config ${config.encode}")
    val latch: CountDownLatch = new CountDownLatch(1)
    val options: DeploymentOptions = new DeploymentOptions().setConfig(config)

    vertx.deployVerticle("service:io.vertx:mysql-postgresql-service", options, new Handler[AsyncResult[String]] {
      override def handle(event: AsyncResult[String]): Unit = {
        if (event.succeeded()) {
          deploymentId = Some(event.result())
          log.info(s"Deployment of service at $address succeeded => ${asyncSqlService.getClass}")
        } else {
          fail(s"deployment failed: ${event.cause()} - got config: ${config.encode}")
        }
        latch.countDown()
      }
    })

    awaitLatch(latch)
  }

  @Test
  def closingConnection(): Unit = {
    (for {
      conn <- arhToFuture(asyncSqlService.getConnection _)
      _ <- arhToFuture(conn.close _)
      res <- arhToFuture((conn.query _).curried("SELECT 1"))
    } yield {
      fail(s"Should not be able to use connection after closing, but got ${res.toJson.encode()}")
    }) recover {
      case ex: Throwable =>
        log.info(s"closed connection stays closed $ex")
        testComplete()
    }
    await()
  }

}
