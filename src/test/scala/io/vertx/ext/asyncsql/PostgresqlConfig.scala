package io.vertx.ext.asyncsql

import io.vertx.core.json.JsonObject

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
trait PostgresqlConfig extends ConfigProvider {
  override val address = "campudus.postgresql"

  override val config: JsonObject = super.config.mergeIn(new JsonObject().put("postgresql", new JsonObject().put("address", address)))
}
