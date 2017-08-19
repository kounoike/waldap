package ldapanda.core.controller

import java.io.{BufferedReader, File, InputStream}

import org.scalatra._
import io.github.gitbucket.scalatra.forms._
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.i18n.I18nSupport
import org.json4s.DefaultFormats
import ldapanda.core.html
import org.apache.commons.io.FileUtils
import org.apache.commons.codec.binary.Base64
import ldapanda.core.ldap.{LDAPUtil, LdapandaLdapServer}
import org.apache.directory.api.ldap.model.entry.{AttributeUtils, DefaultEntry}
import org.apache.directory.api.ldap.model.message.{AliasDerefMode, SearchScope}
import org.apache.directory.api.ldap.model.name.Dn
import org.apache.directory.api.ldap.model.filter.FilterParser

import scala.collection.JavaConverters._

class IndexController extends ControllerBase with JacksonJsonSupport with I18nSupport with ClientSideValidationFormSupport {

  case class HelloForm(username: String, password: String, sn: String, cn: String, displayName: String, mail: String)

  val form = mapping(
    "username" -> text(required, maxlength(40)),
    "password" -> text(required, maxlength(40)),
    "sn" -> text(required, maxlength(40)),
    "cn" -> text(required, maxlength(40)),
    "displayName" -> text(required, maxlength(40)),
    "mail" -> text(required, maxlength(40))
  )(HelloForm.apply)

  get("/"){
    val adminSession = LdapandaLdapServer.directoryService.getAdminSession()
    val dn = new Dn(LdapandaLdapServer.directoryService.getSchemaManager, "ou=Users,o=ldapanda")
    val usersCursor = adminSession.search(dn, SearchScope.ONELEVEL,
      FilterParser.parse("(objectClass=inetOrgPerson)"), AliasDerefMode.DEREF_ALWAYS,
      "uid", "sn", "cn", "displayName", "mail", "objectClass"
    )
    html.index(usersCursor)
  }

  post("/hello", form){ form =>
    val dn = s"uid=${form.username},ou=Users,o=ldapanda"
    val adminSession = LdapandaLdapServer.getAdminSession()
    if(!adminSession.exists(dn)){
      Option(new DefaultEntry(LdapandaLdapServer.directoryService.getSchemaManager, dn,
        "objectClass: top",
        "objectClass: person",
        "objectClass: inetOrgPerson",
        s"cn: ${form.cn}",
        s"sn: ${form.sn}",
        s"uid: ${form.username}",
        s"mail: ${form.mail}",
        s"userPassword: ${LDAPUtil.encodePassword(form.password)}"
      )) match {
        case Some(entry) =>
          entry.add("displayName", form.displayName)
          adminSession.add(entry)
        case None =>
          println("DefaultEntry null!")
      }
    }

    redirect("/")
  }

}
