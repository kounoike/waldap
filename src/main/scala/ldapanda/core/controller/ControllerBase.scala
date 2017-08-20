package ldapanda.core.controller

import javax.servlet.http.HttpServletRequest
import javax.servlet.{FilterChain, ServletRequest, ServletResponse}

import io.github.gitbucket.scalatra.forms.ClientSideValidationFormSupport
import ldapanda.core.ldap.LdapandaLdapServer
import ldapanda.core.model.Account
import ldapanda.core.util.{Keys, StringUtil}
import ldapanda.core.util.Implicits._
import ldapanda.core.util.SyntaxSugars._
import ldapanda.core.service.SystemSettingsService
import org.apache.directory.server.core.api.CoreSession
import org.scalatra.{FlashMap, FlashMapSupport, ScalatraFilter}
import org.scalatra.i18n.{I18nSupport, Messages}
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

  protected def UnauthorizedAdmin()(implicit context: Context) =
    if(request.hasAttribute(Keys.Request.Ajax)){
      org.scalatra.Unauthorized()
    } else {
      if(context.loginAccount.isDefined){
        org.scalatra.Unauthorized(redirect("/"))
      } else {
        if(request.getMethod.toUpperCase == "POST"){
          org.scalatra.Unauthorized(redirect("/admin/signin"))
        } else {
          org.scalatra.Unauthorized(redirect("/admin/signin?redirect=" + StringUtil.urlEncode(
            defining(request.getQueryString){ queryString =>
              request.getRequestURI.substring(request.getContextPath.length) + (if(queryString != null) "?" + queryString else "")
            }
          )))
        }
      }
    }

  protected def NotFound() =
    if(request.hasAttribute(Keys.Request.Ajax)){
      org.scalatra.NotFound()
    } else {
      org.scalatra.NotFound(ldapanda.core.html.error("Not Found"))
    }


  private val contextCache = new java.lang.ThreadLocal[Context]()
  implicit def context: Context = {
    contextCache.get match {
      case null => {
        val context = Context(loadSystemSettings(), LoginAccount, request, messages)
        contextCache.set(context)
        context
      }
      case context => context
    }
  }

  implicit def ldapSession: CoreSession = context.ldapSession

  private def LoginAccount: Option[Account] = request.getAs[Account](Keys.Session.LoginAccount).orElse(session.getAs[Account](Keys.Session.LoginAccount))
}

case class Context(settings: SystemSettingsService.SystemSettings, loginAccount: Option[Account], request: HttpServletRequest, messages: Messages){
  val path = settings.baseUrl.getOrElse(request.getContextPath)
  val currentPath = request.getRequestURI.substring(request.getContextPath.length)
  val baseUrl = settings.baseUrl(request)
  val host = new java.net.URL(baseUrl).getHost
  val ldapSession = LdapandaLdapServer.directoryService.getAdminSession

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
