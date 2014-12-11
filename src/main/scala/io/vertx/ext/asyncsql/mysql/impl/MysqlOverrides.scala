package io.vertx.ext.asyncsql.mysql.impl

import io.vertx.ext.asyncsql.impl.CommandImplementations

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
trait MysqlOverrides extends CommandImplementations {
  override protected def escapeField(str: String): String = "`" + str.replace("`", "\\`") + "`"

  override protected def selectCommand(table: String, fields: Stream[String], limit: Option[Int], offset: Option[Int]): String = {
    val fieldsStr = if (fields.isEmpty) "*" else fields.map(escapeField).mkString(",")
    val tableStr = escapeField(table)
    val limitStr = limit.map(l => s"LIMIT $l").getOrElse(s"LIMIT ${Long.MaxValue}")
    val offsetStr = offset.map(o => s"OFFSET $o").getOrElse("")

    s"SELECT $fieldsStr FROM $tableStr $limitStr $offsetStr"
  }
}
