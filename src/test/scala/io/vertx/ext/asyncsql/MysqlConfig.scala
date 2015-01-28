package io.vertx.ext.asyncsql

import io.vertx.core.json.JsonObject

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
trait MysqlConfig extends ConfigProvider {
  override val address = "campudus.mysql"

  override val config: JsonObject = super.config.mergeIn(new JsonObject().put("mysql", new JsonObject().put("address", address)))
}
