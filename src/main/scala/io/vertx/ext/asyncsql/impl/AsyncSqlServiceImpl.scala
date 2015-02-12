package io.vertx.ext.asyncsql.impl

import io.vertx.core.{AsyncResult, Handler, Vertx}
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.AsyncSqlService
import io.vertx.ext.sql.SqlConnection

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class AsyncSqlServiceImpl(vertx: Vertx, config: JsonObject, mysql: Boolean) extends AsyncSqlService {

  val baseService: BaseSqlService = {
    if (mysql) {
      new MysqlService(vertx, config)
    } else {
      new PostgresqlService(vertx, config)
    }
  }

  override def start(whenDone: Handler[AsyncResult[Void]]): Unit = baseService.start(whenDone)

  override def stop(whenDone: Handler[AsyncResult[Void]]): Unit = baseService.stop(whenDone)

  override def getConnection(handler: Handler[AsyncResult[SqlConnection]]): Unit = {
    baseService.getConnection(handler)
  }
}
