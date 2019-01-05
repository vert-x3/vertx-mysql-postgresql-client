package io.vertx.ext.asyncsql;

public class PostgreSQLClientWithNativeTransportTest extends PostgreSQLClientTest {

  static {
    vertxOptions.setPreferNativeTransport(true);
  }
}
