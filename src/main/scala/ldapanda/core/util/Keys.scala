package ldapanda.core.util

object Keys {
  object Request {
    def Cache(key: String) = s"cache.${key}"
  }

  object Flash {
    val Redirect = "redirect"
    val Info = "info"
  }
}
