package ldapanda.core.controller.admin

import ldapanda.core.controller.ControllerBase
import ldapanda.core.util.{AdminAuthenticator, Keys}
import org.scalatra.FlashMapSupport
import io.github.gitbucket.scalatra.forms._
import ldapanda.core.ldap.{LDAPUtil, LdapandaLdapServer}
import ldapanda.core.model.Account
import ldapanda.core.service.AccountService
import ldapanda.core.admin.html
import org.apache.directory.api.ldap.model.filter.FilterParser
import org.apache.directory.api.ldap.model.message.{AliasDerefMode, SearchScope}
import org.apache.directory.api.ldap.model.name.Dn

trait IndexControllerBase extends ControllerBase with FlashMapSupport with AccountService with AdminAuthenticator{
  case class SignInForm(username: String, password: String)

  val signinForm = mapping(
    "username" -> trim(label("Username", text(required))),
    "password" -> trim(label("Password", text(required)))
  )(SignInForm.apply)

  get("/admin")( adminOnly {
    val adminSession = LdapandaLdapServer.directoryService.getAdminSession()
    val dn = new Dn(LdapandaLdapServer.directoryService.getSchemaManager, "ou=Users,o=ldapanda")
    val usersCursor = adminSession.search(dn, SearchScope.ONELEVEL,
      FilterParser.parse("(objectClass=inetOrgPerson)"), AliasDerefMode.DEREF_ALWAYS,
      "uid", "sn", "cn", "displayName", "mail", "objectClass"
    )
    html.index(usersCursor)
  })

  get("/admin/signin"){
    val redirect = params.get("redirect")
    if(redirect.isDefined && redirect.get.startsWith("/")){
      flash += Keys.Flash.Redirect -> redirect.get
    }
    ldapanda.core.admin.html.signin(flash.get("userName"), flash.get("password"), flash.get("error"))
  }

  post("/admin/signin", signinForm){ form =>
    adminAuthenticate(context.settings, form.username, form.password) match {
      case Some(account) => signin(account)
      case None          => {
        flash += "userName" -> form.username
        flash += "password" -> form.password
        flash += "error" -> "Sorry, your Username and/or Password is incorrect. Please try again."
        redirect("/admin/signin")
      }
    }
  }

  private def signin(account: Account) = {
    session.setAttribute(Keys.Session.LoginAccount, account)

    flash.get(Keys.Flash.Redirect).asInstanceOf[Option[String]].map { redirectUrl =>
      println(redirectUrl)
      redirect(redirectUrl)
    }.getOrElse {
      redirect("/admin")
    }
  }
}

class IndexController extends IndexControllerBase
