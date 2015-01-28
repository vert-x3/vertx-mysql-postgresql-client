package io.vertx.ext.asyncsql.impl

import io.vertx.core.{AsyncResult, Handler, Vertx}
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.{AsyncSqlConnection, AsyncSqlService}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class AsyncSqlServiceImpl(vertx: Vertx, config: JsonObject) extends AsyncSqlService {

  val baseService: BaseSqlService = {
    println(s"CONFIG: ${config.encode}")
    Option(config.getJsonObject("postgresql")).map(c => new PostgresqlService(vertx, c)).orElse {
      Option(config.getJsonObject("mysql")).map(c => new MysqlService(vertx, c))
    }.getOrElse(throw new IllegalArgumentException(s"invalid configuration given: ${config.encode}"))
  }

  override def start(whenDone: Handler[AsyncResult[Void]]): Unit = baseService.start(whenDone)

  override def stop(whenDone: Handler[AsyncResult[Void]]): Unit = baseService.stop(whenDone)

  override def getConnection(handler: Handler[AsyncResult[AsyncSqlConnection]]): Unit = {
    baseService.getConnection(handler)
  }
}
