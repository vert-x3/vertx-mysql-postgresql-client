package io.vertx.ext.asyncsql.mysql;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.asyncsql.DatabaseCommands;
import io.vertx.ext.asyncsql.TransactionCommands;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
@VertxGen
@ProxyGen
public interface MysqlTransaction extends TransactionCommands, DatabaseCommands {

}
