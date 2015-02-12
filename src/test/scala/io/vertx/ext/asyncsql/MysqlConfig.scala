package io.vertx.ext.asyncsql

import io.vertx.core.json.JsonObject

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
trait MysqlConfig extends ConfigProvider {
  override val address = "campudus.mysql"

  override val config: JsonObject = new JsonObject().put("address", address)
}
