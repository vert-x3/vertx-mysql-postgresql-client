package io.vertx.ext.asyncsql.impl.pool;

import com.github.mauricio.async.db.Configuration;
import com.github.mauricio.async.db.Connection;
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection;
import com.github.mauricio.async.db.postgresql.column.PostgreSQLColumnDecoderRegistry;
import com.github.mauricio.async.db.postgresql.column.PostgreSQLColumnEncoderRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.ext.asyncsql.impl.VertxExecutionContext;

public class PostgresqlAsyncConnectionPool extends AsyncConnectionPool {

  public PostgresqlAsyncConnectionPool(Vertx vertx, int maxPoolSize, Configuration configuration) {
    super(vertx, maxPoolSize, configuration);
  }

  @Override
  protected Connection create() {
    return new PostgreSQLConnection(
        configuration,
        PostgreSQLColumnEncoderRegistry.Instance(),
        PostgreSQLColumnDecoderRegistry.Instance(),
        vertx.nettyEventLoopGroup().next(),
        executionContext);
  }

}
