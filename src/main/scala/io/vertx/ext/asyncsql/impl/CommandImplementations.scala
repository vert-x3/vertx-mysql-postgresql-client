package io.vertx.ext.asyncsql.impl

import java.util

import com.github.mauricio.async.db.{Connection, QueryResult, RowData}
import io.vertx.core.json.{JsonArray, JsonObject}
import io.vertx.core.logging.Logger
import io.vertx.core.logging.impl.LoggerFactory
import io.vertx.core.{AsyncResult, Handler, Future => VFuture}
import io.vertx.ext.asyncsql.{SelectOptions, DatabaseCommands}
import io.vertx.ext.asyncsql.impl.pool.SimpleExecutionContext

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import java.util.Objects._

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
trait CommandImplementations extends DatabaseCommands {
  private val logger: Logger = LoggerFactory.getLogger(super.getClass)
  private implicit val executionContext: ExecutionContext = SimpleExecutionContext(logger)

  protected def withConnection[T](fn: Connection => Future[T]): Future[T]

  override def raw(command: String, resultHandler: Handler[AsyncResult[JsonObject]]): Unit = {
    logger.info(s"raw command -> $command")
    withConnection { connection =>
      (for {
        json <- connection.sendQuery(command).map(resultToJsonObject)
      } yield {
        resultHandler.handle(VFuture.succeededFuture(json))
      }) recover {
        case ex: Throwable =>
          logger.info(s"there was a problem with the connection: $ex")
          resultHandler.handle(VFuture.failedFuture(ex))
      }
    }
  }

  private def sendPreparedCommand(statement: String, values: Seq[Any]): Future[QueryResult] = withConnection { conn =>
    conn.sendPreparedStatement(statement, values)
  }

  private def sendRawCommand(statement: String): Future[QueryResult] = withConnection { conn =>
    logger.info(s"sending raw command: $statement")
    conn.sendQuery(statement)
  }

  private def applyToHandler(f: Future[QueryResult], handler: Handler[AsyncResult[JsonObject]]): Unit = {
    f.map(resultToJsonObject).onComplete {
      case Success(res) => handler.handle(VFuture.succeededFuture(res))
      case Failure(ex) => handler.handle(VFuture.failedFuture(ex))
    }
  }

  override def insert(table: String, fields: util.List[String], values: util.List[JsonArray], resultHandler: Handler[AsyncResult[JsonObject]]): Unit = {
    import collection.JavaConverters._
    val stmt = insertCommand(table, fields.asScala.toStream, values.asScala.toStream.map(_.getList.asScala.toStream.map(escapeValue)))
    logger.info(s"insert command = $stmt")
    applyToHandler(sendRawCommand(stmt), resultHandler)
  }

  override def prepared(statement: String, values: JsonArray, resultHandler: Handler[AsyncResult[JsonObject]]): Unit = {
    import collection.JavaConverters._
    applyToHandler(sendPreparedCommand(statement, values.getList.asScala), resultHandler)
  }

  override def select(table: String, selectOptions: SelectOptions, resultHandler: Handler[AsyncResult[JsonObject]]): Unit = {
    requireNonNull(table, "needs to know which table to select")

    import collection.JavaConverters._

    val fields = selectOptions.getFields
    val limit = Option(selectOptions.getLimit).map(_.intValue())
    val offset = Option(selectOptions.getOffset).map(_.intValue())

    val stmt = selectCommand(table, fields.iterator().asScala.toStream.asInstanceOf[Stream[String]], limit, offset)
    applyToHandler(sendRawCommand(stmt), resultHandler)
  }

  protected def stringDelimiter: String = "'"

  protected def stringDelimiterEscape: String = "''"

  /**
   * Escapes a field. May be overridden if database needs other escaping.
   * @param str The field string to escape.
   * @return The escaped string to use as a field.
   */
  protected def escapeField(str: String): String = "\"" + str.replace("\"", "\"\"") + "\""

  /**
   * Escapes a string. May be overridden if database needs other escaping.
   * @param str The string to escape.
   * @return The escaped string to use as a string.
   */
  protected def escapeString(str: String): String =
    s"$stringDelimiter${str.replace(stringDelimiter, stringDelimiterEscape)}$stringDelimiter"

  /**
   * Converts a value into database readable form. May be overridden if database needs other escaping.
   * @param v The value to rewrite.
   * @return The value as a string to use in a database query.
   */
  protected def escapeValue(v: Any): String = v match {
    case null => "NULL"
    case x: Int => x.toString
    case x: Boolean => x.toString
    case x => escapeString(x.toString)
  }

  private def insertCommand(table: String, fields: Stream[String], listOfLines: Stream[Stream[String]]): String =
    s"INSERT INTO ${escapeField(table)} ${fields.map(f => escapeField(f.toString)).mkString("(", ",", ")")} VALUES ${listOfLines.mkString(",")}"

  private def selectCommand(table: String, fields: Stream[String], limit: Option[Int], offset: Option[Int]): String =
    if (fields.isEmpty) {
      s"SELECT * FROM ${escapeField(table)}${limit.map(l => s" LIMIT $l").getOrElse("")}${offset.map(o => s" OFFSET $o").getOrElse("")}"
    } else {
      s"SELECT ${fields.map(escapeField).mkString(",")} FROM ${escapeField(table)}${limit.map(l => s" LIMIT $l").getOrElse("")}${offset.map(o => s" OFFSET $o").getOrElse("")}"
    }

  private def resultToJsonObject(qr: QueryResult): JsonObject = {
    val result = new JsonObject()
    result.put("message", qr.statusMessage)
    result.put("rows", qr.rowsAffected)

    qr.rows match {
      case Some(resultSet) =>
        val fields = (new JsonArray() /: resultSet.columnNames) {
          (arr, name) =>
            arr.add(name)
        }

        val rows = (new JsonArray() /: resultSet) {
          (arr, rowData) =>
            arr.add(rowDataToJsonArray(rowData))
        }

        result.put("fields", fields)
        result.put("results", rows)
      case None =>
    }

    result
  }

  private def rowDataToJsonArray(rowData: RowData): JsonArray = {
    val arr = new JsonArray()
    for {
      elem <- rowData.map(dataToJson).toList
    } {
      arr.add(elem)
    }
    arr
  }

  private def dataToJson(data: Any): Any = data match {
    case null => null
    case x: Array[Byte] => x
    case x: Boolean => x
    case x: Number => x
    case x: String => x
    case x: JsonObject => x
    case x: JsonArray => x
    case x => x.toString
  }

}
