package waldap.core.util

import java.util.regex.Pattern.quote
import javax.servlet.http.HttpServletRequest

import slick.jdbc.JdbcBackend
import waldap.core.model.Session

object Implicits {

  implicit def request2Session(implicit request: HttpServletRequest): Session = Database.getSession(request)

  implicit class RichRequest(private val request: HttpServletRequest) extends AnyVal {

    def paths: Array[String] = request.getRequestURI.substring(request.getContextPath.length + 1).split("/")

    def hasQueryString: Boolean = request.getQueryString != null

    def hasAttribute(name: String): Boolean = request.getAttribute(name) != null

    def gitRepositoryPath: String = request.getRequestURI.replaceFirst("^" + quote(request.getContextPath) + "/git/", "/")

    def baseUrl:String = {
      val url = request.getRequestURL.toString
      val len = url.length - (request.getRequestURI.length - request.getContextPath.length)
      url.substring(0, len).stripSuffix("/")
    }
  }
}
