package io.vertx.ext.asyncsql

import java.util.concurrent.CountDownLatch

import io.vertx.core.{AsyncResult, Handler}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
abstract class DirectTestBase extends SqlTestBase with ConfigProvider {

  override lazy val asyncSqlService = AsyncSqlService.create(vertx, config)

  override def await() = super.await()

  override def assertEquals(a: Any, b: Any) = super.assertEquals(a, b)

  override def assertNotNull(x: AnyRef) = super.assertNotNull(x)

  override def fail(msg: String) = super.fail(msg)

  override def setUp(): Unit = {
    super.setUp()
    log.info(s"Setting up service at $address")
    val latch: CountDownLatch = new CountDownLatch(1)

    asyncSqlService.start(new Handler[AsyncResult[Void]]() {
      override def handle(event: AsyncResult[Void]): Unit = {
        log.info(s"Service set up at $address!")
        latch.countDown()
      }
    })

    awaitLatch(latch)
  }

  override def tearDown(): Unit = {
    log.info(s"Tearing down service at $address!")
    val latch: CountDownLatch = new CountDownLatch(1)
    asyncSqlService.stop(new Handler[AsyncResult[Void]]() {
      override def handle(event: AsyncResult[Void]): Unit = latch.countDown()
    })
    awaitLatch(latch)
    log.info(s"Done tearing down service at $address!")
    super.tearDown()
  }

}
