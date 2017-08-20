package ldapanda.core.controller.admin

import ldapanda.core.controller.ControllerBase
import ldapanda.core.util.{AdminAuthenticator, Keys}
import org.scalatra.FlashMapSupport
import io.github.gitbucket.scalatra.forms._
import ldapanda.core.ldap.{LDAPUtil, LdapandaLdapServer}
import ldapanda.core.service.AccountService
import ldapanda.core.admin.html
import ldapanda.core.model.Account
import org.apache.directory.api.ldap.model.entry.DefaultEntry
import org.apache.directory.api.ldap.model.filter.FilterParser
import org.apache.directory.api.ldap.model.message.{AliasDerefMode, SearchScope}
import org.apache.directory.api.ldap.model.name.Dn
import org.slf4j.LoggerFactory

trait IndexControllerBase extends ControllerBase with FlashMapSupport with AccountService with AdminAuthenticator{
  private val logger = LoggerFactory.getLogger(getClass)

  case class UserAddForm(username: String, password: String, sn: String, cn: String, displayName: String, mail: String)
  val useraddform = mapping(
    "username" -> text(required, maxlength(40)),
    "password" -> text(required, maxlength(40)),
    "sn" -> text(required, maxlength(40)),
    "cn" -> text(required, maxlength(40)),
    "displayName" -> text(required, maxlength(40)),
    "mail" -> text(required, maxlength(40))
  )(UserAddForm.apply)

  case class SignInForm(username: String, password: String)
  val signinForm = mapping(
    "username" -> trim(label("Username", text(required))),
    "password" -> trim(label("Password", text(required)))
  )(SignInForm.apply)

  get("/admin") {
    redirect("/admin/users")
  }

  get("/admin/users"){
    val adminSession = LdapandaLdapServer.directoryService.getAdminSession()
    val dn = new Dn(LdapandaLdapServer.directoryService.getSchemaManager, "ou=Users,o=ldapanda")
    val usersCursor = adminSession.search(dn, SearchScope.ONELEVEL,
      FilterParser.parse("(objectClass=inetOrgPerson)"), AliasDerefMode.DEREF_ALWAYS,
      "uid", "sn", "cn", "displayName", "mail", "objectClass"
    )
    ldapanda.core.admin.user.html.userlist(usersCursor)
  }

  get("/admin/users/add"){
    ldapanda.core.admin.user.html.useradd()
  }

  get("/admin/users/:name/delete"){
    val name = params.get("name")
    name.map { n =>
      val dn = s"uid=${n},ou=Users,o=ldapanda"
      if (context.ldapSession.exists(dn)){
        context.ldapSession.delete(new Dn(context.ldapSession.getDirectoryService.getSchemaManager, dn))
        redirect("/admin/users")
      }else{
        s"Not found!? ${dn}"
      }
    }.getOrElse(NotFound())
  }

  post("/admin/users/add", useraddform){form =>
    val dn = s"uid=${form.username},ou=Users,o=ldapanda"
    if(!context.ldapSession.exists(dn)){
      logger.info(s"${LDAPUtil.encodePassword(form.password)}")
      val entry = new DefaultEntry(context.ldapSession.getDirectoryService.getSchemaManager())
      entry.setDn(dn)
      entry.add("objectClass", "top", "person", "inetOrgPerson")
      entry.add("uid", form.username)
      entry.add("cn", form.cn)
      entry.add("sn", form.sn)
      entry.add("mail", form.mail)
      entry.add("userPassword", LDAPUtil.encodePassword(form.password))
      entry.add("displayName", form.displayName)
      context.ldapSession.add(entry)
    }

    redirect("/admin/users")
  }

  get("/admin/signin"){
    val redirect = params.get("redirect")
    if(redirect.isDefined && redirect.get.startsWith("/")){
      flash += Keys.Flash.Redirect -> redirect.get
    }
    html.signin(flash.get("userName"), flash.get("password"), flash.get("error"))
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
