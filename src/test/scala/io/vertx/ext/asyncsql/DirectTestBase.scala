package io.vertx.ext.asyncsql

import java.util.concurrent.CountDownLatch

import io.vertx.core.{AsyncResult, Handler}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
abstract class DirectTestBase extends SQLTestBase with ConfigProvider {

  def asyncSqlClient: AsyncSQLClient

  override def await() = super.await()

  override def setUp(): Unit = {
    super.setUp()
  }

  override def tearDown(): Unit = {
    val latch: CountDownLatch = new CountDownLatch(1)
    asyncSqlClient.close(new Handler[AsyncResult[Void]]() {
      override def handle(event: AsyncResult[Void]): Unit = latch.countDown()
    })
    awaitLatch(latch)
    super.tearDown()
  }

}
