package ldapanda.core.controller

import javax.servlet.http.HttpServletRequest
import javax.servlet.{FilterChain, ServletRequest, ServletResponse}

import io.github.gitbucket.scalatra.forms.ClientSideValidationFormSupport
import ldapanda.core.util.Keys
import ldapanda.core.util.SyntaxSugars._
import ldapanda.core.service.SystemSettingsService
import org.scalatra.{FlashMap, FlashMapSupport, ScalatraFilter}
import org.scalatra.i18n.I18nSupport
import org.scalatra.json.JacksonJsonSupport


abstract class ControllerBase extends ScalatraFilter
  with ClientSideValidationFormSupport with JacksonJsonSupport with I18nSupport with FlashMapSupport
  with SystemSettingsService{
  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain): Unit = try{
    super.doFilter(request, response, chain)
  } finally {
    contextCache.remove()
  }
  implicit val jsonFormats = ldapanda.core.util.JsonFormat.jsonFormats
  private val contextCache = new java.lang.ThreadLocal[Context]()

  /**
    * Returns the context object for the request.
    */
  implicit def context: Context = {
    contextCache.get match {
      case null => {
        val context = Context(loadSystemSettings(), request)
        contextCache.set(context)
        context
      }
      case context => context
    }
  }}

case class Context(settings: SystemSettingsService.SystemSettings, request: HttpServletRequest){
  val path = settings.baseUrl.getOrElse(request.getContextPath)
  val currentPath = request.getRequestURI.substring((request.getContextPath.length))
  val baseUrl = settings.baseUrl(request)
  val host = new java.net.URL(baseUrl).getHost

  /**
    * Get object from cache.
    *
    * If object has not been cached with the specified key then retrieves by given action.
    * Cached object are available during a request.
    */
  def cache[A](key: String)(action: => A): A =
    defining(Keys.Request.Cache(key)){ cacheKey =>
      Option(request.getAttribute(cacheKey).asInstanceOf[A]).getOrElse {
        val newObject = action
        request.setAttribute(cacheKey, newObject)
        newObject
      }
    }
}