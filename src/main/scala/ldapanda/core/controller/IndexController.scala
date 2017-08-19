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
import ldapanda.core.ldap.LdapandaLdapServer
import org.apache.directory.api.ldap.model.entry.{AttributeUtils, DefaultEntry}


class IndexController extends ControllerBase with JacksonJsonSupport with I18nSupport with ClientSideValidationFormSupport {

  case class HelloForm(username: String, password: String, sn: String, cn: String)

  val form = mapping(
    "username" -> text(required, maxlength(40)),
    "password" -> text(required, maxlength(40)),
    "sn" -> text(required, maxlength(40)),
    "cn" -> text(required, maxlength(40))
  )(HelloForm.apply)

  get("/"){
    html.index()
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
        s"userPassword: ${form.password}"
      )) match {
        case Some(entry) =>
          adminSession.add(entry)
        case None =>
          println("DefaultEntry null!")
      }
    }

    html.hello(form.username, form.sn, form.cn)
  }

}
