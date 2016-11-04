package io.vertx.groovy.ext.asyncsql;
public class GroovyStaticExtension {
  public static io.vertx.ext.asyncsql.AsyncSQLClient createNonShared(io.vertx.ext.asyncsql.MySQLClient j_receiver, io.vertx.core.Vertx vertx, java.util.Map<String, Object> config) {
    return io.vertx.lang.groovy.ConversionHelper.wrap(io.vertx.ext.asyncsql.MySQLClient.createNonShared(vertx,
      config != null ? io.vertx.lang.groovy.ConversionHelper.toJsonObject(config) : null));
  }
  public static io.vertx.ext.asyncsql.AsyncSQLClient createShared(io.vertx.ext.asyncsql.MySQLClient j_receiver, io.vertx.core.Vertx vertx, java.util.Map<String, Object> config, java.lang.String poolName) {
    return io.vertx.lang.groovy.ConversionHelper.wrap(io.vertx.ext.asyncsql.MySQLClient.createShared(vertx,
      config != null ? io.vertx.lang.groovy.ConversionHelper.toJsonObject(config) : null,
      poolName));
  }
  public static io.vertx.ext.asyncsql.AsyncSQLClient createShared(io.vertx.ext.asyncsql.MySQLClient j_receiver, io.vertx.core.Vertx vertx, java.util.Map<String, Object> config) {
    return io.vertx.lang.groovy.ConversionHelper.wrap(io.vertx.ext.asyncsql.MySQLClient.createShared(vertx,
      config != null ? io.vertx.lang.groovy.ConversionHelper.toJsonObject(config) : null));
  }
  public static io.vertx.ext.asyncsql.AsyncSQLClient createNonShared(io.vertx.ext.asyncsql.PostgreSQLClient j_receiver, io.vertx.core.Vertx vertx, java.util.Map<String, Object> config) {
    return io.vertx.lang.groovy.ConversionHelper.wrap(io.vertx.ext.asyncsql.PostgreSQLClient.createNonShared(vertx,
      config != null ? io.vertx.lang.groovy.ConversionHelper.toJsonObject(config) : null));
  }
  public static io.vertx.ext.asyncsql.AsyncSQLClient createShared(io.vertx.ext.asyncsql.PostgreSQLClient j_receiver, io.vertx.core.Vertx vertx, java.util.Map<String, Object> config, java.lang.String poolName) {
    return io.vertx.lang.groovy.ConversionHelper.wrap(io.vertx.ext.asyncsql.PostgreSQLClient.createShared(vertx,
      config != null ? io.vertx.lang.groovy.ConversionHelper.toJsonObject(config) : null,
      poolName));
  }
  public static io.vertx.ext.asyncsql.AsyncSQLClient createShared(io.vertx.ext.asyncsql.PostgreSQLClient j_receiver, io.vertx.core.Vertx vertx, java.util.Map<String, Object> config) {
    return io.vertx.lang.groovy.ConversionHelper.wrap(io.vertx.ext.asyncsql.PostgreSQLClient.createShared(vertx,
      config != null ? io.vertx.lang.groovy.ConversionHelper.toJsonObject(config) : null));
  }
}
