package io.vertx.ext.asyncsql.impl

import com.github.mauricio.async.db
import com.github.mauricio.async.db.{Connection, QueryResult, RowData}
import io.vertx.core.json.JsonArray
import io.vertx.core.logging.{LoggerFactory, Logger}
import io.vertx.core.{AsyncResult, Future => VFuture, Handler}
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool
import io.vertx.ext.sql.{UpdateResult, ResultSet, SQLConnection}
import org.joda.time.format.ISODateTimeFormat

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success, Try}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class AsyncSQLConnectionScalaImpl(connection: Connection, pool: AsyncConnectionPool)(implicit executionContext: ExecutionContext) extends SQLConnection {

  private val logger: Logger = LoggerFactory.getLogger(classOf[AsyncSQLConnectionImpl])

  import scala.collection.JavaConverters._

  var inTransaction: Boolean = false
  var inAutoCommit: Boolean = true

  override def setAutoCommit(autoCommit: Boolean, resultHandler: Handler[AsyncResult[Void]]): SQLConnection = {
    val fut = if (inTransaction && autoCommit) {
      inTransaction = false
      connection.sendQuery("COMMIT")
    } else {
      Future.successful()
    }

    inAutoCommit = autoCommit

    fut onComplete doneVoid(resultHandler)

    this
  }

  override def execute(sql: String, resultHandler: Handler[AsyncResult[Void]]): SQLConnection = {
    (for {
      _ <- beginTransactionIfNeeded()
      r <- connection.sendQuery(sql)
    } yield r) onComplete doneVoid(resultHandler)
    this
  }

  override def query(sql: String, resultHandler: Handler[AsyncResult[ResultSet]]): SQLConnection = {
    (for {
      _ <- beginTransactionIfNeeded()
      r <- connection.sendQuery(sql)
    } yield r) onComplete done(resultHandler, queryResultToResultSet)
    this
  }

  override def queryWithParams(sql: String, params: JsonArray, resultHandler: Handler[AsyncResult[ResultSet]]): SQLConnection = {
    (for {
      _ <- beginTransactionIfNeeded()
      r <- connection.sendPreparedStatement(sql, params.getList.asScala)
    } yield r) onComplete done(resultHandler, queryResultToResultSet)
    this
  }

  override def update(sql: String, resultHandler: Handler[AsyncResult[UpdateResult]]): SQLConnection = {
    (for {
      _ <- beginTransactionIfNeeded()
      r <- connection.sendQuery(sql)
    } yield r) onComplete done(resultHandler, queryResultToUpdateResult)
    this
  }

  override def updateWithParams(sql: String, params: JsonArray, resultHandler: Handler[AsyncResult[UpdateResult]]): SQLConnection = {
    (for {
      _ <- beginTransactionIfNeeded()
      r <- connection.sendPreparedStatement(sql, params.getList.asScala)
    } yield r) onComplete done(resultHandler, queryResultToUpdateResult)
    this
  }

  override def commit(handler: Handler[AsyncResult[Void]]): SQLConnection = endAndStartTransaction("COMMIT", handler)

  override def rollback(handler: Handler[AsyncResult[Void]]): SQLConnection = endAndStartTransaction("ROLLBACK", handler)

//  override def close() : Unit = {
//    close(new Handler[AsyncResult[Void]] {
//      override def handle(event: AsyncResult[Void]): Unit = {
//        if (event.failed()) {
//          logger.error("Failure in closing connection", event.cause())
//        }
//      }
//    })
//  }

  override def close(handler: Handler[AsyncResult[Void]]): Unit = {
    inAutoCommit = true
    if (inTransaction) {
      inTransaction = false
      connection.sendQuery("COMMIT") andThen {
        case _ =>
          pool.giveBack(connection)
      } onComplete doneVoid(handler)
    } else {
      pool.giveBack(connection) onComplete doneVoid(handler)
    }
  }

  private def beginTransactionIfNeeded(): Future[_] = {
    if (!inAutoCommit && !inTransaction) {
      inTransaction = true
      connection.sendQuery("BEGIN")
    } else {
      Future.successful()
    }
  }

  private def endAndStartTransaction(command: String, handler: Handler[AsyncResult[Void]]): SQLConnection = {
    if (inTransaction) {
      inTransaction = false
      (for {
        _ <- connection.sendQuery(command)
        _ <- connection.sendQuery("BEGIN")
      } yield {
        inTransaction = true
      }) onComplete doneVoid(handler)
    } else {
      handler.handle(VFuture.failedFuture("Not in a transaction currently"))
    }
    this
  }

  private def queryResultToUpdateResult(qr: QueryResult): UpdateResult = {
    new UpdateResult(qr.rowsAffected.toInt, qr.rows.map(rs => new JsonArray(rs.columnNames.toList.asJava)).getOrElse(new JsonArray()))
  }

  private def queryResultToResultSet(qr: QueryResult): ResultSet = {
    (for {
      rows <- qr.rows
    } yield {
      val names = rows.columnNames.toList
      val results = rowDataSeqToJsonArray(rows)

      new ResultSet(names.asJava, results.asJava)
    }) getOrElse {
      new ResultSet(List.empty[String].asJava, List.empty[JsonArray].asJava)
    }
  }

  private def rowDataSeqToJsonArray(set: db.ResultSet): List[JsonArray] = {
    (for {
      row <- set
    } yield {
      rowToJsonArray(row)
    }).toList
  }

  private def rowToJsonArray(row: RowData): JsonArray = {
    val json = new JsonArray()
    for {
      elem <- row
    } yield {
      elem match {
        case null => json.addNull()
        case localDateTime: org.joda.time.LocalDateTime => json.add(localDateTime.toString)
        case localDate: org.joda.time.LocalDate => json.add(localDate.toString)
        case other => json.add(other)
      }
    }
    json
  }

  private def doneVoid(handler: Handler[AsyncResult[Void]]): Try[_] => Unit = {
    case Success(_) => handler.handle(VFuture.succeededFuture())
    case Failure(ex) => handler.handle(VFuture.failedFuture(ex))
  }

  private def done[B](handler: Handler[AsyncResult[B]], f: QueryResult => B): Try[QueryResult] => Unit = {
    case Success(res) => handler.handle(VFuture.succeededFuture(f(res)))
    case Failure(ex) => handler.handle(VFuture.failedFuture(ex))
  }
}
