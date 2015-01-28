package io.vertx.ext.asyncsql.impl

import com.github.mauricio.async.db
import com.github.mauricio.async.db.{Connection, QueryResult, RowData}
import io.vertx.core.json.JsonArray
import io.vertx.core.{AsyncResult, Future => VFuture, Handler}
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool
import io.vertx.ext.asyncsql.{AsyncSqlConnection, ResultSet, UpdateResult}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
class AsyncSqlConnectionImpl(connection: Connection, pool: AsyncConnectionPool)(implicit executionContext: ExecutionContext) extends AsyncSqlConnection {

  import scala.collection.JavaConverters._

  override def execute(sql: String, resultHandler: Handler[AsyncResult[Void]]): AsyncSqlConnection = {
    connection.sendQuery(sql) onComplete doneVoid(resultHandler)
    this
  }

  override def query(sql: String, params: JsonArray, resultHandler: Handler[AsyncResult[ResultSet]]): AsyncSqlConnection = {
    connection.sendQuery(sql) onComplete done(resultHandler, queryResultToResultSet)
    this
  }

  override def update(sql: String, params: JsonArray, resultHandler: Handler[AsyncResult[UpdateResult]]): AsyncSqlConnection = {
    (Option(params) match {
      case Some(ps) => connection.sendPreparedStatement(sql, ps.getList.asScala)
      case None => connection.sendQuery(sql)
    }) onComplete done(resultHandler, queryResultToUpdateResult)
    this
  }

  override def commit(handler: Handler[AsyncResult[Void]]): AsyncSqlConnection = {
    connection.sendQuery("COMMIT") onComplete doneVoid(handler)
    this
  }

  override def rollback(handler: Handler[AsyncResult[Void]]): AsyncSqlConnection = {
    connection.sendQuery("ROLLBACK") onComplete doneVoid(handler)
    this
  }

  override def close(handler: Handler[AsyncResult[Void]]): Unit = {
    pool.giveBack(connection) onComplete doneVoid(handler)
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
    }).getOrElse(new ResultSet())
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
      json.add(elem)
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
