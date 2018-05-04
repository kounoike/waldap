package waldap.core.controller.admin

import io.github.gitbucket.scalatra.forms._
import waldap.core.controller.ControllerBase
import org.scalatra.{FlashMapSupport, Ok}
import org.slf4j.LoggerFactory
import waldap.core.service.LDAPAccountService

trait UserControllerBase extends ControllerBase with FlashMapSupport with LDAPAccountService {
  private val logger = LoggerFactory.getLogger(getClass)

  case class UserAddForm(
    username: String,
    password: String,
    sn: String,
    givenName: String,
    displayName: String,
    mail: String
  )
  val useraddform = mapping(
    "username" -> text(required, maxlength(40)),
    "password" -> text(required, maxlength(40)),
    "sn" -> text(required, maxlength(40)),
    "givenName" -> text(required, maxlength(40)),
    "displayName" -> text(required, maxlength(40)),
    "mail" -> text(required, maxlength(40))
  )(UserAddForm.apply)

  case class UserEditForm(sn: String, givenName: String, displayName: String, mail: String)
  val usereditform = mapping(
    "sn" -> text(required, maxlength(40)),
    "givenName" -> text(required, maxlength(40)),
    "displayName" -> text(required, maxlength(40)),
    "mail" -> text(required, maxlength(40))
  )(UserEditForm.apply)

  case class PasswordForm(password: String)
  val passwordform = mapping(
    "password" -> trim(label("Password", text(required)))
  )(PasswordForm.apply)

  get("/admin/users") {
    waldap.core.admin.user.html.userlist(GetLDAPUsers, GetLDAPGroups)
  }

  post("/admin/users/:name/edit", usereditform) { form =>
    params.get("name").map { n =>
      EditLDAPUser(n, form.givenName, form.sn, form.displayName, form.mail)
      redirect("/admin/users")
    } getOrElse (NotFound())
  }

  post("/admin/users/:name/password", passwordform) { form =>
    params.get("name").map { n =>
      ChangeLDAPUserPassword(n, form.password)
      redirect("/admin/users")
    } getOrElse (NotFound())
  }

  get("/admin/users/:name/delete") {
    val name = params.get("name")
    name
      .map { n =>
        DeleteLDAPUser(n)
        redirect("/admin/users")
      }
      .getOrElse(NotFound())
  }

  post("/admin/users/add", useraddform) { form =>
    AddLDAPUser(form.username, form.password, form.givenName, form.sn, form.displayName, form.mail)

    redirect("/admin/users")
  }

  get("/admin/users/join/:user/:group") {
    val userName = params.get("user").get
    val groupName = params.get("group").get
    println(s"join user:$userName group:$groupName")
    JoinToLDAPGroup(userName, groupName)
    redirect("/admin/users")
  }

  get("/admin/users/disjoin/:user/:group") {
    val userName = params.get("user").get
    val groupName = params.get("group").get
    println(s"disjoin user:$userName group:$groupName")
    DisjoinFromLDAPGroup(userName, groupName)
    redirect("/admin/users")
  }
}

class UserController extends UserControllerBase
