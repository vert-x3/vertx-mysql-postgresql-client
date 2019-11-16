package io.vertx.ext.asyncsql.impl.pool;

import com.github.jasync.sql.db.Connection;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.impl.pool.AsyncConnectionPool;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@RunWith(VertxUnitRunner.class)
public class AsyncConnectionPoolReleaseTest {
  private static final int MAX_POOL_SIZE = 3;
  private static final JsonObject globalConfiguration = new JsonObject()
      .put("maxPoolSize", MAX_POOL_SIZE)
      .put("connectionReleaseDelay", 10);

  private Vertx vertx;
  /** Timers in first in first out order */
  private Map<Long, Handler<Long>> timers = new LinkedHashMap<>();

  @Before
  public void setUp() {
    this.vertx = Mockito.mock(Vertx.class);
    Mockito.when(vertx.setTimer(Mockito.anyLong(), Mockito.any())).then(invocation -> {
      long timerId = ThreadLocalRandom.current().nextLong();
      Handler<Long> handler = invocation.getArgument(1);
      timers.put(timerId, handler);
      return timerId;
    });
    Mockito.when(vertx.cancelTimer(Mockito.anyLong())).then(invocation -> {
      long timerId = invocation.getArgument(0);
      Handler<Long> handler = timers.remove(timerId);
      return handler != null;
    });
  }

  @After
  public void allTimersWereExcuted(TestContext context) {
    context.assertEquals(0, timers.size());
  }

  private void executeNextTimer() {
    Iterator<Entry<Long, Handler<Long>>> iterator = timers.entrySet().iterator();
    Entry<Long, Handler<Long>> entry = iterator.next();
    iterator.remove();
    Long timerId = entry.getKey();
    Handler<Long> handler = entry.getValue();
    handler.handle(timerId);
  }

  @Test
  public void releaseOneConnection(TestContext context) throws Exception {
    final AsyncConnectionPool pool = Mockito.spy(new AsyncConnectionPoolMock());
    pool.take(context.asyncAssertSuccess(connection -> {
      context.assertEquals(0, pool.getTimersSize());
      pool.giveBack(connection);
      context.assertEquals(1, pool.getTimersSize());
      executeNextTimer();
      context.assertEquals(0, pool.getTimersSize());
    }));
  }

  @Test
  public void releaseThreeConnections(TestContext context) throws Exception {
    final AsyncConnectionPool pool = Mockito.spy(new AsyncConnectionPoolMock());
    pool.take(context.asyncAssertSuccess(connection1 -> {
      pool.take(context.asyncAssertSuccess(connection2 -> {
        pool.take(context.asyncAssertSuccess(connection3 -> {
          context.assertEquals(0, pool.getTimersSize());
          pool.giveBack(connection2);
          pool.giveBack(connection3);
          pool.giveBack(connection1);
          context.assertEquals(3, pool.getTimersSize());
          executeNextTimer();
          executeNextTimer();
          executeNextTimer();
          context.assertEquals(0, pool.getTimersSize());
        }));
      }));
    }));
  }

  @Test
  public void cancelReleaseTimer(TestContext context) throws Exception {
    final AsyncConnectionPool pool = Mockito.spy(new AsyncConnectionPoolMock());
    pool.take(context.asyncAssertSuccess(connection -> {
      context.assertEquals(0, pool.getTimersSize());
      pool.giveBack(connection);
      context.assertEquals(1, pool.getTimersSize());
      pool.take(context.asyncAssertSuccess(connection2 -> {
        context.assertEquals(connection, connection2);
        context.assertEquals(0, pool.getTimersSize());
        pool.giveBack(connection);
        context.assertEquals(1, pool.getTimersSize());
        executeNextTimer();
        context.assertEquals(0, pool.getTimersSize());
      }));
    }));
  }

  private class AsyncConnectionPoolMock extends AsyncConnectionPool {
    AsyncConnectionPoolMock() {
      super(AsyncConnectionPoolReleaseTest.this.vertx, globalConfiguration, null);
    }

    @Override
    protected Connection create() {
      final Connection connection = Mockito.mock(Connection.class);
      Mockito.when(connection.connect()).then(answer -> CompletableFuture.completedFuture(connection));
      Mockito.when(connection.isConnected()).thenReturn(true);
      return connection;
    }
  }
}
