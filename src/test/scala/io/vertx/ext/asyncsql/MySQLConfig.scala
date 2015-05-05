package io.vertx.ext.asyncsql

import io.vertx.core.json.JsonObject

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
trait MySQLConfig extends ConfigProvider {
  this: SQLTestBase =>

  override val dateTimeInUtc1 = "2015-02-22T07:15:01.000"
  override val dateTimeInUtc2 = "2014-06-27T17:50:02.000"
}
