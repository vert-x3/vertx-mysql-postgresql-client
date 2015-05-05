package io.vertx.ext.asyncsql.impl

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.impl.pool.PostgresqlAsyncConnectionPool

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class PostgreSQLClient(val vertx: Vertx, val config: JsonObject) extends BaseSQLClient {

  override protected val poolFactory = PostgresqlAsyncConnectionPool.apply _

  override protected val defaultHost: String = "localhost"

  override protected val defaultPort: Int = 5432

  override protected val defaultDatabase: Option[String] = Some("testdb")

  override protected val defaultUser: String = "vertx"

  override protected val defaultPassword: Option[String] = Some("password")

}
