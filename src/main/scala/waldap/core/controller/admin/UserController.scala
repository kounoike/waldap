package waldap.core.controller.admin

import io.github.gitbucket.scalatra.forms._
import waldap.core.controller.ControllerBase
import waldap.core.ldap.{LDAPUtil, LdapandaLdapServer}
import org.apache.directory.api.ldap.model.entry.{DefaultEntry, DefaultModification, ModificationOperation}
import org.apache.directory.api.ldap.model.filter.FilterParser
import org.apache.directory.api.ldap.model.message.{AliasDerefMode, SearchScope}
import org.apache.directory.api.ldap.model.name.Dn
import org.scalatra.FlashMapSupport
import org.slf4j.LoggerFactory

trait UserControllerBase extends ControllerBase with FlashMapSupport {
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

  case class UserEditForm(sn: String, cn: String, displayName: String, mail: String)
  val usereditform = mapping(
    "sn" -> text(required, maxlength(40)),
    "cn" -> text(required, maxlength(40)),
    "displayName" -> text(required, maxlength(40)),
    "mail" -> text(required, maxlength(40))
  )(UserEditForm.apply)

  case class PasswordForm(password: String)
  val passwordform = mapping(
    "password" -> trim(label("Password", text(required)))
  )(PasswordForm.apply)

  get("/admin/users"){
    val adminSession = LdapandaLdapServer.directoryService.getAdminSession()
    val dn = new Dn(LdapandaLdapServer.directoryService.getSchemaManager, "ou=Users,o=waldap")
    val usersCursor = adminSession.search(dn, SearchScope.ONELEVEL,
      FilterParser.parse("(objectClass=inetOrgPerson)"), AliasDerefMode.DEREF_ALWAYS,
      "uid", "sn", "cn", "displayName", "mail", "objectClass"
    )
    waldap.core.admin.user.html.userlist(usersCursor)
  }

  get("/admin/users/add"){
    waldap.core.admin.user.html.useradd()
  }

  post("/admin/users/:name/edit", usereditform) { form =>
    params.get("name").map{ n =>
      val dn = s"uid=${n},ou=Users,o=waldap"
      if (context.ldapSession.exists(dn)) {
        val cnMod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "cn", form.cn)
        val snMod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "sn", form.sn)
        val displayNameMod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "displayName", form.displayName)
        val mailMod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "mail", form.mail)
        context.ldapSession.modify(new Dn(context.ldapSession.getDirectoryService.getSchemaManager, dn),
          cnMod, snMod, displayNameMod, mailMod)
        redirect("/admin/users")
      }else{
        NotFound()
      }
    }getOrElse(NotFound())
  }

  post("/admin/users/:name/password", passwordform) { form =>
    params.get("name").map{ n =>
      val dn = s"uid=${n},ou=Users,o=waldap"
      if (context.ldapSession.exists(dn)) {
        val passwordMod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE,
          "userPassword", LDAPUtil.encodePassword(form.password))
        context.ldapSession.modify(new Dn(context.ldapSession.getDirectoryService.getSchemaManager, dn),
          passwordMod)
        redirect("/admin/users")
      }else{
        NotFound()
      }
    }getOrElse(NotFound())
  }

  get("/admin/users/:name/delete"){
    val name = params.get("name")
    name.map { n =>
      val dn = s"uid=${n},ou=Users,o=waldap"
      if (context.ldapSession.exists(dn)){
        context.ldapSession.delete(new Dn(context.ldapSession.getDirectoryService.getSchemaManager, dn))
        redirect("/admin/users")
      }else{
        s"Not found!? ${dn}"
      }
    }.getOrElse(NotFound())
  }

  post("/admin/users/add", useraddform){form =>
    val dn = s"uid=${form.username},ou=Users,o=waldap"
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
}

class UserController extends UserControllerBase
