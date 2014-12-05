package io.vertx.ext.asyncsql.mysql;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.asyncsql.ConnectionCommands;
import io.vertx.ext.asyncsql.DatabaseCommands;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@VertxGen
@ProxyGen
public interface MysqlConnection extends ConnectionCommands, DatabaseCommands {

}
