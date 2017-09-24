package waldap.core.controller.user

import io.github.gitbucket.scalatra.forms._
import waldap.core.util.Implicits._
import waldap.core.user.html
import waldap.core.controller.ControllerBase
import waldap.core.ldap.LDAPUtil
import waldap.core.model.Account
import waldap.core.service.{AccountService, LDAPAccountService, SystemSettingsService, WebAppService}
import waldap.core.util.Keys

class IndexController extends ControllerBase with AccountService with LDAPAccountService with WebAppService {

  case class SignInForm(username: String, password: String)
  val signinForm = mapping(
    "username" -> label("Username", text(required)),
    "password" -> label("Password", text(required))
  )(SignInForm.apply)

  private val passwordForm = mapping(
    "userPassword" -> text(required),
    "userPasswordRetype" -> text(required)
  )(PasswordForm.apply)
  case class PasswordForm(userPassword: String, userPasswordRetype: String)

  get("/user"){
    val entry = getLdapEntry(context.loginAccount.get)
    entry.map{ e =>
      html.index(e)
    }.getOrElse{
      NotFound()
    }
  }

  get("/user/apps"){
    val groups = GetLDAPUsersGroups(context.loginAccount.get.userName)
    val instances = groups.map{g => getWebAppInstance(g.get("o").getString, g.get("ou").getString)}.flatten.distinct
    html.apps(instances)
  }

  get("/user/password"){
    html.password(flash.get("info"))
  }

  post("/user/password", passwordForm){ form =>
    if(form.userPassword != form.userPasswordRetype){
      flash += "info" -> context.messages.get("settings.passwordMismatch")
    }
    else{
      ChangeLDAPUserPassword(context.loginAccount.get.userName, form.userPassword)

      flash += "info" -> context.messages.get("settings.passwordChanged")
    }
    redirect("/user/password")
  }

  get("/user/signin"){
    val redirect = params.get("redirect")
    if(redirect.isDefined && redirect.get.startsWith("/")){
      flash += Keys.Flash.Redirect -> redirect.get
    }
    html.signin(flash.get("userName"), flash.get("password"), flash.get("error"))
  }

  post("/user/signin", signinForm){ form =>
    userAuthenticate(context.settings, form.username, form.password) match {
      case Some(account) => signin(account)
      case None          => {
        flash += "userName" -> form.username
        flash += "password" -> form.password
        flash += "error" -> context.messages.get("loginform.error")
        redirect("/user/signin")
      }
    }
  }

  private def signin(account: Account) = {
    session.setAttribute(Keys.Session.LoginAccount, account)

    flash.get(Keys.Flash.Redirect).asInstanceOf[Option[String]].map { redirectUrl =>
      redirect(redirectUrl)
    }.getOrElse {
      redirect("/user")
    }
  }
}
