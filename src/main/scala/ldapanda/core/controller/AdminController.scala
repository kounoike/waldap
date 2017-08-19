package ldapanda.core.controller

import org.scalatra.{FlashMapSupport, ScalatraFilter}
import io.github.gitbucket.scalatra.forms._
import ldapanda.core.util.Keys

trait AdminControllerBase extends ControllerBase with FlashMapSupport {
  case class SignInForm(userName: String, password: String)
  val signinForm = mapping(
    "userName" -> trim(label("Username", text(required))),
    "password" -> trim(label("Password", text(required)))
  )(SignInForm.apply)

  get("/admin/signin"){
    val redirect = params.get("redirect")
    if(redirect.isDefined && redirect.get.startsWith("/admin")){
      flash += Keys.Flash.Redirect -> redirect.get
    }

    ldapanda.core.admin.html.signin(flash.get("userName"), flash.get("password"), flash.get("error"))
  }
}

class AdminController extends AdminControllerBase
