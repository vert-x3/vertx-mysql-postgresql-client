package io.vertx.ext.asyncsql

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class PostgreSQLScalaTest extends DirectTestBase with PostgreSQLConfig {

  override lazy val asyncSqlService = PostgreSQLClient.createPostgreSQLService(vertx, config)

}
