package io.vertx.ext.asyncsql.impl;

import com.github.mauricio.async.db.Configuration;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.CharsetUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool;
import io.vertx.ext.sql.SQLConnection;
import scala.Option;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public abstract class BaseSQLClient {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());
  protected final Vertx vertx;

  protected int maxPoolSize;
  protected int transactionTimeout;
  protected String registerAddress;

  public BaseSQLClient(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.maxPoolSize = config.getInteger("maxPoolSize", 10);
    this.transactionTimeout = config.getInteger("transactionTimeout", 500);
    this.registerAddress = config.getString("address");
  }

  protected abstract AsyncConnectionPool pool();

  public void getConnection(Handler<AsyncResult<SQLConnection>> handler) {
    pool().take(ar -> {
      if (ar.succeeded()) {
        final AsyncConnectionPool pool = pool();
        ExecutionContext ec = pool.executionContext();
        handler.handle(Future.succeededFuture(new AsyncSQLConnectionImpl(ar.result(), pool, ec)));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  public void close(Handler<AsyncResult<Void>> handler) {
    log.info("Stopping async SQL client " + this);
    pool().close(ar -> {
          if (ar.succeeded()) {
            if (handler != null) {
              handler.handle(Future.succeededFuture());
            }
          } else {
            if (handler != null) {
              handler.handle(Future.failedFuture(ar.cause()));
            }
          }
        }
    );
  }

  public void close() {
    close(null);
  }

  protected Configuration getConfiguration(String defaultHost, int defaultPort, String defaultDatabase, String defaultUser, String defaultPassword, JsonObject config) {
    String host = config.getString("host", defaultHost);
    int port = config.getInteger("port", defaultPort);
    String username = config.getString("username", defaultUser);
    String password = config.getString("password", defaultPassword);
    String database = config.getString("database", defaultDatabase);

    log.info("Creating configuration for " + host + ":" + port);
    return new Configuration(username, host, port, Option.apply(password), Option.apply(database),
        CharsetUtil.UTF_8, 16777216, PooledByteBufAllocator.DEFAULT,
        Duration.apply(5, TimeUnit.SECONDS), Duration.apply(5, TimeUnit.SECONDS));
  }


}
