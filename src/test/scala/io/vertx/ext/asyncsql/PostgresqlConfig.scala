package io.vertx.ext.asyncsql

import io.vertx.core.json.JsonObject

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
trait PostgresqlConfig extends ConfigProvider {
  this: SqlTestBase =>

  override val address = "campudus.postgresql"

  override val config: JsonObject = new JsonObject().put("address", address)
}
