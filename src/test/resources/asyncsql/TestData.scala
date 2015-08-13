package io.vertx.ext.asyncsql

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
trait TestData {

  val names = List("Albert", "Bertram", "Cornelius", "Dieter", "Emil", "Friedrich", "Gustav", "Heinrich", "Ingolf",
    "Johann", "Klaus", "Ludwig", "Max", "Norbert", "Otto", "Paul", "Quirin", "Rudolf", "Stefan", "Thorsten", "Ulrich",
    "Viktor", "Wilhelm", "Xaver", "Yoda", "Zacharias")
  val simpleTestTable = names.map(x => "'" + x + "'").zipWithIndex.map(_.swap)

}
