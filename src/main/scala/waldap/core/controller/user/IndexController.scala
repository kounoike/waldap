package waldap.core.controller.user

import io.github.gitbucket.scalatra.forms._
import waldap.core.user.html
import waldap.core.controller.ControllerBase
import waldap.core.model.Account
import waldap.core.service.AccountService
import waldap.core.util.Keys

class IndexController extends ControllerBase with AccountService {

  case class SignInForm(username: String, password: String)
  val signinForm = mapping(
    "username" -> label("Username", text(required)),
    "password" -> label("Password", text(required))
  )(SignInForm.apply)

  get("/user"){
    val entry = getLdapEntry(context.loginAccount.get)
    entry.map{ e =>
      html.index(e)
    }.getOrElse{
      NotFound()
    }
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
