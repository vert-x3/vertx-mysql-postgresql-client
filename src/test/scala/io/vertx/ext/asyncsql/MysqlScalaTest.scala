package io.vertx.ext.asyncsql

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class MysqlScalaTest extends DirectTestBase with MysqlConfig {

  override lazy val asyncSqlService = AsyncSqlService.createMySqlService(vertx, config)

}
