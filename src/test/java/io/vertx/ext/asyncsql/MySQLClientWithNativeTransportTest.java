package io.vertx.ext.asyncsql;

public class MySQLClientWithNativeTransportTest extends MySQLClientTest {

  static {
    vertxOptions.setPreferNativeTransport(true);
  }
}
