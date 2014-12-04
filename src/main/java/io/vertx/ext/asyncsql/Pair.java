package io.vertx.ext.asyncsql;

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
public class Pair<T1, T2> {
  final T1 t1;
  final T2 t2;

  public Pair(T1 t1, T2 t2) {
    this.t1 = t1;
    this.t2 = t2;
  }
}
