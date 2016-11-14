package io.vertx.ext.asyncsql.impl;

import com.github.mauricio.async.db.QueryResult;
import com.github.mauricio.async.db.RowData;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.SQLRowStream;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import scala.Option;

import com.github.mauricio.async.db.ResultSet;
import scala.collection.Iterator;
import scala.runtime.AbstractFunction1;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

class AsyncSQLRowStream implements SQLRowStream {

  private final ResultSet rs;
  private final Iterator<RowData> cursor;
  private List<String> columns;

  private final AtomicBoolean paused = new AtomicBoolean(false);
  private final AtomicBoolean ended = new AtomicBoolean(false);

  private Handler<JsonArray> handler;
  private Handler<Void> endHandler;

  AsyncSQLRowStream(QueryResult qr) {
    final Option<ResultSet> rows = qr.rows();
    if (rows.isDefined()) {
      rs = rows.get();
      cursor = rs.iterator();
    } else {
      rs = null;
      cursor = null;
    }

    paused.set(true);
  }

  @Override
  public int column(String name) {
    if (rs == null) {
      throw new IndexOutOfBoundsException("'" + name + "' not found");
    }

    if (columns == null) {
      columns = ScalaUtils.toJavaList(rs.columnNames().toList());
    }
    return columns.indexOf(name);
  }

  @Override
  public SQLRowStream exceptionHandler(Handler<Throwable> handler) {
    return this;
  }

  @Override
  public SQLRowStream handler(Handler<JsonArray> handler) {
    this.handler = handler;
    // start pumping data once the handler is set
    resume();
    return this;
  }

  @Override
  public SQLRowStream pause() {
    paused.compareAndSet(false, true);
    return this;
  }

  @Override
  public SQLRowStream resume() {
    if (paused.compareAndSet(true, false)) {
      nextRow();
    }
    return this;
  }

  private void nextRow() {
    if (!paused.get()) {
      if (cursor.hasNext()) {
        handler.handle(rowToJsonArray(cursor.next()));
        nextRow();
      } else {
        // mark as ended if the handler was registered too late
        ended.set(true);
        // automatically close resources
        close(c -> {
          if (endHandler != null) {
            endHandler.handle(null);
          }
        });
      }
    }
  }

  @Override
  public SQLRowStream endHandler(Handler<Void> handler) {
    this.endHandler = handler;
    // registration was late but we're already ended, notify
    if (ended.compareAndSet(true, false)) {
      // only notify once
      endHandler.handle(null);
    }
    return this;
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    // make sure we stop pumping data
    pause();
    // call the provided handler
    handler.handle(Future.succeededFuture());
  }

  private JsonArray rowToJsonArray(RowData data) {
    JsonArray array = new JsonArray();
    data.foreach(new AbstractFunction1<Object, Void>() {
      @Override
      public Void apply(Object value) {
        if (value == null) {
          array.addNull();
        } else if (value instanceof scala.math.BigDecimal) {
          array.add(value.toString());
        } else if (value instanceof LocalDateTime) {
          array.add(value.toString());
        } else if (value instanceof LocalDate) {
          array.add(value.toString());
        } else if (value instanceof DateTime) {
          array.add(Instant.ofEpochMilli(((DateTime) value).getMillis()));
        } else if (value instanceof UUID) {
          array.add(value.toString());
        } else {
          array.add(value);
        }
        return null;
      }
    });
    return array;
  }
}
