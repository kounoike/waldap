package waldap.core.controller.admin

import waldap.core.controller.ControllerBase
import waldap.core.util.{AdminAuthenticator, Keys}
import org.scalatra.FlashMapSupport
import io.github.gitbucket.scalatra.forms._
import waldap.core.service.AccountService
import waldap.core.admin.html
import waldap.core.model.Account
import org.slf4j.LoggerFactory

trait IndexControllerBase extends ControllerBase with FlashMapSupport with AccountService with AdminAuthenticator{
  private val logger = LoggerFactory.getLogger(getClass)

  case class SignInForm(username: String, password: String)
  val signinForm = mapping(
    "username" -> label("Username", text(required)),
    "password" -> label("Password", text(required))
  )(SignInForm.apply)

  get("/admin") {
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
