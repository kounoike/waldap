package ldapanda.core.util

import java.net.{URLDecoder, URLEncoder}

object StringUtil {
  def urlEncode(value: String): String = URLEncoder.encode(value, "UTF-8").replace("+", "%20")

  def urlDecode(value: String): String = URLDecoder.decode(value, "UTF-8")
}
