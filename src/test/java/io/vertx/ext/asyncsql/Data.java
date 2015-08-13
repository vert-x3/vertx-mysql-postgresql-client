package io.vertx.ext.asyncsql;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Data {

  public static final List<String> NAMES = Arrays.asList(
      "Albert", "Bertram", "Cornelius", "Dieter", "Emil", "Friedrich", "Gustav", "Heinrich", "Ingolf",
      "Johann", "Klaus", "Ludwig", "Max", "Norbert", "Otto", "Paul", "Quirin", "Rudolf", "Stefan", "Thorsten", "Ulrich",
      "Viktor", "Wilhelm", "Xaver", "Yoda", "Zacharias"
  );

  public static final Map<Integer, String> TABLE = new TreeMap<>();

  static {
    int i = 0;
    for (String n : NAMES) {
      TABLE.put(i, n);
      i++;
    }
  }

  public static String get() {
    StringBuilder builder = new StringBuilder();
    for (Map.Entry<Integer, String> entry : TABLE.entrySet()) {
      if (builder.length() != 0) {
        builder.append(",");
      }
      builder
          .append("(")
          .append(entry.getKey())
          .append(",")
          .append("'")
          .append(entry.getValue())
          .append("'")
          .append(")");
    }
    return builder.toString();
  }
}
