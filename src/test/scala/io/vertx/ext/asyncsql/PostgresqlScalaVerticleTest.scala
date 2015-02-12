package io.vertx.ext.asyncsql

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class PostgresqlScalaVerticleTest extends VerticleTestBase with PostgresqlConfig {
  override def serviceName = "io.vertx:postgresql-service"
}
