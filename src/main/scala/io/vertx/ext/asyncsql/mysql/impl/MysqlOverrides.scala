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
    val limitStr = (limit, offset) match {
      case (Some(l), Some(o)) => s"LIMIT $o, $l"
      case (None, Some(o)) => s"LIMIT $o, ${Long.MaxValue}"
      case (Some(l), None) => s"LIMIT $l"
      case _ => ""
    }

    s"SELECT $fieldsStr FROM $tableStr $limitStr"
  }
}
