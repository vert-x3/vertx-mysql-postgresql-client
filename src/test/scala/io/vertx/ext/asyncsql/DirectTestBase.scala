package io.vertx.ext.asyncsql

import java.util.concurrent.CountDownLatch

import io.vertx.core.{AsyncResult, Handler}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
abstract class DirectTestBase extends SQLTestBase with ConfigProvider {

  def asyncSqlService: AsyncSQLClient

  override def await() = super.await()

  override def setUp(): Unit = {
    super.setUp()
    val latch: CountDownLatch = new CountDownLatch(1)
  }

  override def tearDown(): Unit = {
    val latch: CountDownLatch = new CountDownLatch(1)
    asyncSqlService.close(new Handler[AsyncResult[Void]]() {
      override def handle(event: AsyncResult[Void]): Unit = latch.countDown()
    })
    awaitLatch(latch)
    super.tearDown()
  }

}
