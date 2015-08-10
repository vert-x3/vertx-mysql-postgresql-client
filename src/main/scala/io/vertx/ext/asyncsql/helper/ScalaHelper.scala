package io.vertx.ext.asyncsql.helper

import scala.concurrent.{Future, ExecutionContext}
import io.vertx.core.{Future => VFuture}

import scala.util.{Failure, Success}

/**
 * @author <a href="http://www.campudus.com">Joern Bernhardt</a>.
 */
object ScalaHelper {

  def toVertxFuture[T](someFuture: Future[T], executionContext: ExecutionContext): VFuture[T] = {
    implicit val ec = executionContext
    val vertxFuture = VFuture.future[T]()

    someFuture onComplete {
      case Success(result) => vertxFuture.complete(result)
      case Failure(ex) => vertxFuture.fail(ex)
    }

    vertxFuture
  }
}
