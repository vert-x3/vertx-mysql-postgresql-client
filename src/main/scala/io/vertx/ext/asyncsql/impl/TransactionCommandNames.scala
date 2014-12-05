package io.vertx.ext.asyncsql.impl

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
trait TransactionCommandNames {

  protected def startTransactionCommand: String = "BEGIN"

  protected def rollbackCommand: String = "ROLLBACK"

  protected def commitCommand: String = "COMMIT"

}
