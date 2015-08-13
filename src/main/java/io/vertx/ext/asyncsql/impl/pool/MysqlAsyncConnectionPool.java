package io.vertx.ext.asyncsql.impl.pool;

import com.github.mauricio.async.db.Configuration;
import com.github.mauricio.async.db.Connection;
import com.github.mauricio.async.db.mysql.MySQLConnection;
import com.github.mauricio.async.db.mysql.util.CharsetMapper;
import io.vertx.core.Vertx;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.ext.asyncsql.impl.VertxExecutionContext;

public class MysqlAsyncConnectionPool extends AsyncConnectionPool {

  public MysqlAsyncConnectionPool(Vertx vertx, int maxPoolSize, Configuration configuration) {
    super(vertx, maxPoolSize, configuration);
  }

  @Override
  protected Connection create() {
    return new MySQLConnection(configuration, CharsetMapper.Instance(),
        ((EventLoopContext) vertx.getOrCreateContext()).eventLoop(),
        VertxExecutionContext.create(vertx));
  }

}
