package io.vertx.ext.asyncsql

import java.util.concurrent.CountDownLatch

import io.vertx.core.{AsyncResult, Handler, DeploymentOptions}

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

  override def tearDown(): Unit = {
    log.info(s"Tearing down Service Verticle at $address -> ${getClass.getName}")
    deploymentId.map(vertx.undeploy(_, new Handler[AsyncResult[Void]] {
      override def handle(event: AsyncResult[Void]): Unit = {
        log.info(s"Done tearing down Service Verticle at $address -> ${getClass.getName}")
        VerticleTestBase.super.tearDown()
        log.info(s"After super.tearDown()")
      }
    }))
  }
}
