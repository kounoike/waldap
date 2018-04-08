package waldap.core.util

import scala.util.control.Exception.ignoring

object SyntaxSugars {

  def defining[A, B](value: A)(f: A => B): B = f(value)

  def using[A <% { def close(): Unit }, B](resource: A)(f: A => B): B =
    try f(resource)
    finally {
      if (resource != null) {
        ignoring(classOf[Throwable]) {
          resource.close()
        }
      }
    }
}
