package waldap.core.util

import org.json4s.{DefaultFormats, NoTypeHints}
import org.json4s.jackson.Serialization

object JsonFormat {

  case class Context(baseUrl: String)

  val jsonFormats = DefaultFormats

}
