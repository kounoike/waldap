package waldap.core.util

import SyntaxSugars.defining
import scala.reflect.ClassTag

object ConfigUtil {
  def getEnvironmentVariable[A](key: String): Option[A] = {
    val value = System.getenv("WALDAP_" + key.toUpperCase.replace('.', '_'))
    if(value != null && value.nonEmpty){
      Some(convertType(value)).asInstanceOf[Option[A]]
    } else {
      None
    }
  }

  def getSystemProperty[A](key: String): Option[A] = {
    val value = System.getProperty("waldap." + key)
    if (value != null && value.nonEmpty) {
      Some(convertType(value)).asInstanceOf[Option[A]]
    } else {
      None
    }
  }

  def convertType[A: ClassTag](value: String) =
    defining(implicitly[ClassTag[A]].runtimeClass){ c =>
      if(c == classOf[Boolean])  value.toBoolean
      else if(c == classOf[Long]) value.toLong
      else if(c == classOf[Int]) value.toInt
      else value
    }
}
