package waldap.core.util

object Keys {
  object Session {
    val LoginAccount = "loginAccount"
  }

  object Request {
    def Cache(key: String) = s"cache.$key"
    val Ajax = "AJAX"
    val DBSession = "DB_SESSION"
  }

  object Flash {
    val Redirect = "redirect"
    val Info = "info"
  }
}
