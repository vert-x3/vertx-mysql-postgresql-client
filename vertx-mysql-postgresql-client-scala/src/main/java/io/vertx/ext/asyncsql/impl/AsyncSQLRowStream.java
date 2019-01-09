package io.vertx.ext.asyncsql.impl;

import com.github.mauricio.async.db.QueryResult;
import com.github.mauricio.async.db.ResultSet;
import com.github.mauricio.async.db.RowData;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.SQLRowStream;
import scala.Option;
import scala.collection.Iterator;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class AsyncSQLRowStream implements SQLRowStream {

  private final ResultSet rs;
  private final Iterator<RowData> cursor;
  private List<String> columns;

  private long demand = 0L;
  private final AtomicBoolean ended = new AtomicBoolean(false);

  private Handler<JsonArray> handler;
  private Handler<Void> endHandler;
  private Handler<Void> rsClosedHandler;

  AsyncSQLRowStream(QueryResult qr) {
    final Option<ResultSet> rows = qr.rows();
    if (rows.isDefined()) {
      rs = rows.get();
      cursor = rs.iterator();
    } else {
      rs = null;
      cursor = null;
    }
  }

  @Override
  public int column(String name) {
    if (rs == null) {
      throw new IndexOutOfBoundsException("'" + name + "' not found");
    }

    // the columns value will be cached
    return columns().indexOf(name);
  }

  @Override
  public List<String> columns() {
    // populate the cache
    if (columns == null) {
      // quick escape
      if (rs == null) {
        return Collections.emptyList();
      }
      // this list is always read only
      columns = Collections.unmodifiableList(ScalaUtils.toJavaList(rs.columnNames().toList()));
    }
    return columns;
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
    demand = 0L;
    return this;
  }

  @Override
  public synchronized SQLRowStream fetch(long amount) {
    if (amount > 0L) {
      if ((demand += amount) < 0L) {
        demand = Long.MAX_VALUE;
      }
      nextRow();
    }
    return this;
  }

  @Override
  public SQLRowStream resume() {
    return fetch(Long.MAX_VALUE);
  }

  private void nextRow() {
    while (demand > 0L) {
      if (cursor.hasNext()) {
        if (demand != Long.MAX_VALUE) {
          demand--;
        }
        handler.handle(ScalaUtils.rowToJsonArray(cursor.next()));
      } else {
        // mark as ended if the handler was registered too late
        ended.set(true);
        // automatically close resources
        if (rsClosedHandler != null) {
          // only notify (since the rs is closed by the underlying driver)
          rsClosedHandler.handle(null);
        } else {
          // default behavior notify that everything ended
          close(c -> {
            if (endHandler != null) {
              endHandler.handle(null);
            }
          });
        }
        break;
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
  public SQLRowStream resultSetClosedHandler(Handler<Void> handler) {
    this.rsClosedHandler = handler;
    // registration was late but we're already ended, notify
    if (ended.compareAndSet(true, false)) {
      // only notify once
      rsClosedHandler.handle(null);
    }
    return this;
  }

  @Override
  public void moreResults() {
    // NO-OP since the underlying driver only returns 1 ResultSet
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    // make sure we stop pumping data
    pause();
    // call the provided handler
    if (handler != null) {
      handler.handle(Future.succeededFuture());
    }
  }
}
