package io.vertx.ext.asyncsql

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class PostgresqlScalaTest extends DirectTestBase with PostgresqlConfig {

  override lazy val asyncSqlService = AsyncSqlService.createPostgreSqlService(vertx, config)

}
