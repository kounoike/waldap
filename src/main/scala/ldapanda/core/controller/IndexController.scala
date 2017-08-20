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

  get("/"){
    html.index("LdapAndA")
  }

  get("/signout"){
    session.invalidate
    redirect("/")
  }

}
