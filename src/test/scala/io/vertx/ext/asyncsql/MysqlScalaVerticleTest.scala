package io.vertx.ext.asyncsql

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class MysqlScalaVerticleTest extends VerticleTestBase with MysqlConfig {
  override def serviceName = "io.vertx.mysql-service"
}
