package io.vertx.ext.asyncsql.tx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.asyncsql.AbstractTestBase;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.TransactionIsolation;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public abstract class IsolationLevelTest extends AbstractTestBase {

  @Test
  public void testChangeIsolationLevel(TestContext context) {
    Async async = context.async();
    client.getConnection(ar -> {
      if (ar.failed()) {
        context.fail(ar.cause());
        return;
      }

      conn = ar.result();

//      select current_setting('transaction_isolation');
//      START TRANSACTION isolation level read uncommitted;
//      select current_setting('transaction_isolation');
//      COMMIT;
//      SELECT current_setting('transaction_isolation');

      final AtomicReference<TransactionIsolation> ref = new AtomicReference<>();

      conn.getTransactionIsolation(ar1 -> {
        ensureSuccess(context, ar1);
        // base line
        ref.set(ar1.result());
        conn.setTransactionIsolation(TransactionIsolation.READ_UNCOMMITTED, ar2 -> {
          ensureSuccess(context, ar2);
          conn.setAutoCommit(false, ar3 -> {
            ensureSuccess(context, ar3);
            conn.getTransactionIsolation(ar4 -> {
              ensureSuccess(context, ar4);
              assertEquals(TransactionIsolation.READ_UNCOMMITTED, ar4.result());
              conn.commit(ar5 -> {
                ensureSuccess(context, ar5);
                conn.setAutoCommit(true, ar6 -> {
                  ensureSuccess(context, ar6);
                  conn.getTransactionIsolation(ar7 -> {
                    ensureSuccess(context, ar7);
                    assertEquals(ref.get(), ar7.result());
                    async.complete();
                  });
                });
              });
            });
          });
        });
      });
    });
  }

  private void setupTxTestTable(SQLConnection conn, Handler<AsyncResult<Void>> handler) {
    conn.execute("BEGIN",
      ar -> conn.execute("DROP TABLE IF EXISTS test_txtable",
        ar2 -> conn.execute("CREATE TABLE test_txtable (id BIGINT, val BIGINT)",
          ar3 -> conn.update("INSERT INTO test_txtable (id, val) VALUES (1, 8), (2, 8)",
            ar4 -> conn.execute("COMMIT", handler)))));
  }

  interface IsolationHandler {
    void handle(SQLConnection conn1, SQLConnection conn2);
  }

  private void runIsolationLevelTest(TestContext context, IsolationHandler handler) {
    client.getConnection(ar -> {
      if (ar.failed()) {
        context.fail(ar.cause());
        return;
      }

      // Create table
      conn = ar.result();
      setupTxTestTable(conn, ar2 -> {
        if (ar2.failed()) {
          context.fail(ar2.cause());
          return;
        }

        // acquire 2 new connections
        client.getConnection(ar3 -> {
          if (ar3.failed()) {
            context.fail(ar3.cause());
            return;
          }

          // set auto commit off to connection
          final SQLConnection conn1 = ar3.result();

          client.getConnection(ar5 -> {
            if (ar5.failed()) {
              context.fail(ar5.cause());
              return;
            }

            // set auto commit off to connection
            final SQLConnection conn2 = ar5.result();

            // close the setup connection
            conn.close();

            // run test
            handler.handle(conn1, conn2);
          });
        });
      });
    });
  }

  @Test
  @Ignore
  public void testIsolationReadUncommited(TestContext context) {
    Async async = context.async();

    runIsolationLevelTest(context, (conn1, conn2) -> {
//      TX A: start transaction;
//      TX B: set session transaction isolation level read uncommitted;
//      TX B: start transaction;
//      TX A: select * from test_txtable;                   -- val = 8
//      TX B: select * from test_txtable;                   -- val = 8
//      TX A: update test_txtable set val = val + 1;        -- val = 9
//      TX B: select * from test_txtable;                   -- val = 9, dirty read
//      TX A: rollback;
//      TX B: select * from test_txtable;                   -- val = 8
//      TX B: commit;
      conn1.setAutoCommit(false, onSuccess(context, res1 -> {
        conn1.execute("SELECT 0", res1_1 -> {
          conn2.setTransactionIsolation(TransactionIsolation.READ_UNCOMMITTED, onSuccess(context, res2 -> {
            conn2.setAutoCommit(false, onSuccess(context, res3 -> {
              conn2.execute("SELECT 0", res3_1 -> {
                conn1.query("select * from test_txtable", res4 -> {
                  ensureSuccess(context, res4);
                  assertEquals(Integer.valueOf(8), res4.result().getRows().get(0).getInteger("val"));
                  conn2.query("select * from test_txtable", res5 -> {
                    ensureSuccess(context, res5);
                    assertEquals(Integer.valueOf(8), res5.result().getRows().get(0).getInteger("val"));
                    conn1.update("update test_txtable set val = val + 1", onSuccess(context, res6 -> {
                      conn2.query("select * from test_txtable", res7 -> {
                        ensureSuccess(context, res7);
                        // dirty read
                        assertEquals(Integer.valueOf(9), res7.result().getRows().get(0).getInteger("val"));
                        conn1.rollback(res8 -> {
                          ensureSuccess(context, res8);
                          conn2.query("select * from test_txtable", res9 -> {
                            ensureSuccess(context, res9);
                            assertEquals(Integer.valueOf(8), res9.result().getRows().get(0).getInteger("val"));
                            conn2.commit(res10 -> {
                              ensureSuccess(context, res10);
                              conn2.setAutoCommit(true, res10_1 -> {
                                ensureSuccess(context, res10_1);
                                async.complete();
                              });
                            });
                          });
                        });
                      });
                    }));
                  });
                });
              });
            }));
          }));
        });
      }));
    });
  }

  @Test
  @Ignore
  public void testIsolationReadCommited(TestContext context) {
    Async async = context.async();

    runIsolationLevelTest(context, (conn1, conn2) -> {
//      TX A: start transaction;
//      TX B: set session transaction isolation level read committed;
//      TX B: start transaction;
//      TX A: select * from test;                   -- val = 8
//      TX B: select * from test;                   -- val = 8
//      TX A: update test set val = val + 1;        -- val = 9
//      TX B: select * from test;                   -- val = 8, No dirty read!
//      TX A: commit
//      TX B: select * from test;                   -- val = 9, commited read
//
      conn1.setAutoCommit(false, onSuccess(context, res1 -> {
        conn1.execute("SELECT 0", res1_1 -> {
          ensureSuccess(context, res1_1);
          conn2.setTransactionIsolation(TransactionIsolation.READ_COMMITTED, onSuccess(context, res2 -> {
            conn2.setAutoCommit(false, onSuccess(context, res3 -> {
              conn2.execute("SELECT 0", res3_1 -> {
                ensureSuccess(context, res3_1);
                conn1.query("select * from test_txtable", res4 -> {
                  ensureSuccess(context, res4);
                  assertEquals(Integer.valueOf(8), res4.result().getRows().get(0).getInteger("val"));
                  conn2.query("select * from test_txtable", res5 -> {
                    ensureSuccess(context, res5);
                    assertEquals(Integer.valueOf(8), res5.result().getRows().get(0).getInteger("val"));
                    conn1.update("update test_txtable set val = val + 1", onSuccess(context, res6 -> {
                      conn2.query("select * from test_txtable", res7 -> {
                        ensureSuccess(context, res7);
                        // no dirty read
                        assertEquals(Integer.valueOf(8), res7.result().getRows().get(0).getInteger("val"));
                        conn1.commit(res8 -> {
                          ensureSuccess(context, res8);
                          conn1.setAutoCommit(true, res8_1 -> {
                            ensureSuccess(context, res8_1);
                            conn2.query("select * from test_txtable", res9 -> {
                              ensureSuccess(context, res9);
                              // commited read
                              assertEquals(Integer.valueOf(9), res9.result().getRows().get(0).getInteger("val"));
                              async.complete();
                            });
                          });
                        });
                      });
                    }));
                  });
                });
              });
            }));
          }));
        });
      }));
    });
  }

  @Test
  @Ignore
  public void testIsolationRepeatableRead(TestContext context) {
    Async async = context.async();

    runIsolationLevelTest(context, (conn1, conn2) -> {
//      TX A: start transaction;
//      TX B: set session transaction isolation level repeatable read;
//      TX B: start transaction;
//      TX A: select * from test;                   -- val = 8
//      TX B: select * from test;                   -- val = 8
//      TX A: update test set val = val + 1;        -- val = 9
//      TX B: select * from test;                   -- val = 8
//      TX A: commit
//      TX B: select * from test;                   -- val = 8, repeatable read!
//      TX B: commit;
//      TX B: select * from test;                   -- val = 9 (from tx A)
      conn1.setAutoCommit(false, onSuccess(context, res1 -> {
        conn1.execute("SELECT 0", res1_1 -> {
          ensureSuccess(context, res1_1);
          conn2.setTransactionIsolation(TransactionIsolation.REPEATABLE_READ, onSuccess(context, res2 -> {
            conn2.setAutoCommit(false, onSuccess(context, res3 -> {
              conn2.execute("SELECT 0", res3_1 -> {
                conn1.query("select * from test_txtable", res4 -> {
                  ensureSuccess(context, res4);
                  assertEquals(Integer.valueOf(8), res4.result().getRows().get(0).getInteger("val"));
                  conn2.query("select * from test_txtable", res5 -> {
                    ensureSuccess(context, res5);
                    assertEquals(Integer.valueOf(8), res5.result().getRows().get(0).getInteger("val"));
                    conn1.update("update test_txtable set val = val + 1", onSuccess(context, res6 -> {
                      conn2.query("select * from test_txtable", res7 -> {
                        ensureSuccess(context, res7);
                        assertEquals(Integer.valueOf(8), res7.result().getRows().get(0).getInteger("val"));
                        conn1.commit(res8 -> {
                          ensureSuccess(context, res8);
                          conn1.setAutoCommit(true, res8_1 -> {
                            ensureSuccess(context, res8_1);
                            conn2.query("select * from test_txtable", res9 -> {
                              ensureSuccess(context, res9);
                              // repeatable read
                              assertEquals(Integer.valueOf(8), res9.result().getRows().get(0).getInteger("val"));
                              conn2.commit(res10 -> {
                                ensureSuccess(context, res10);
                                conn2.setAutoCommit(true, res10_1 -> {
                                  ensureSuccess(context, res10_1);
                                  conn2.query("select * from test_txtable", res11 -> {
                                    ensureSuccess(context, res11);
                                    // from tx A
                                    assertEquals(Integer.valueOf(9), res9.result().getRows().get(0).getInteger("val"));
                                    async.complete();
                                  });
                                });
                              });
                            });
                          });
                        });
                      });
                    }));
                  });
                });
              });
            }));
          }));
        });
      }));
    });
  }

  @Test
  @Ignore
  public void testIsolationSerializable(TestContext context) {
    Async async = context.async();

    runIsolationLevelTest(context, (conn1, conn2) -> {
//      TX A: start transaction;
//      TX B: set session transaction isolation level serializable;
//      TX B: start transaction;
//      TX A: select * from test;               -- val = 8
//      TX A: update test set val = val + 1;    -- val = 9
//      TX B: select * from test;               -- LOCKED, NO OUTPUT
//      TX A: commit;                           -- Unlocked TX B
//      TX B: select * from test;               -- val = 8 (repeatable read!)
//      TX B: commit;
//      TX B: select * from test;               -- val = 9 (now we see TX A)
      conn1.setAutoCommit(false, onSuccess(context, res1 -> {
        conn1.execute("SELECT 0", res1_1 -> {
          ensureSuccess(context, res1_1);
          conn2.setTransactionIsolation(TransactionIsolation.SERIALIZABLE, onSuccess(context, res2 -> {
            conn2.setAutoCommit(false, onSuccess(context, res3 -> {
              // since the 2 steps above can be just setup we force a query to be run
              conn2.execute("SELECT 0", res3_1 -> {
                ensureSuccess(context, res3_1);

                conn1.query("select * from test_txtable", res4 -> {
                  ensureSuccess(context, res4);
                  assertEquals(Integer.valueOf(8), res4.result().getRows().get(0).getInteger("val"));
                  conn1.update("update test_txtable set val = val + 1", onSuccess(context, res5 -> {

                    final AtomicInteger lock = new AtomicInteger(1);

                    // this will lock
                    conn2.query("select * from test_txtable", onSuccess(context, res6 -> {
                      // introduce a delay so the commit handler will update the lock
                      vertx.setTimer(100, v -> {
                        assertEquals(0, lock.get());

                        conn2.query("select * from test_txtable", res8 -> {
                          ensureSuccess(context, res8);
                          // repeatable read
                          assertEquals(Integer.valueOf(8), res8.result().getRows().get(0).getInteger("val"));
                          conn2.commit(res9 -> {
                            ensureSuccess(context, res9);
                            conn2.setAutoCommit(true, res9_1 -> {
                              ensureSuccess(context, res9_1);
                              conn2.query("select * from test_txtable", res10 -> {
                                ensureSuccess(context, res10);
                                // see TX A
                                assertEquals(Integer.valueOf(9), res10.result().getRows().get(0).getInteger("val"));
                                conn1.close();
                                conn2.close();
                                async.complete();
                              });
                            });
                          });
                        });
                      });
                    }));

                    conn1.commit(res7 -> {
                      ensureSuccess(context, res7);
                      // behave like the rdbms
                      conn1.setAutoCommit(true, res7_1 -> {
                        ensureSuccess(context, res7_1);
                        // this unlocks the lock
                        lock.decrementAndGet();
                      });
                    });
                  }));
                });
              });
            }));
          }));
        });
      }));
    });
  }
}
