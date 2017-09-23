package waldap.core.ldap

import org.apache.commons.codec.digest.Sha2Crypt

object LDAPUtil {
  val ldapName: String = "waldap"
  val baseDnName: String = "o=waldap"

  val usersDn: String = s"ou=Users,${baseDnName}"
  val groupsDn: String = s"ou=Groups,${baseDnName}"

  val systemAdmin: String = "uid=admin,ou=system"

  def encodePassword(plain: String): String = {
    "{CRYPT}" + Sha2Crypt.sha256Crypt(plain.getBytes("UTF-8"))
  }

  def checkPassword(encodedPassword: String, password: String): Boolean = {
    val re = "(\\{CRYPT\\})(.*)".r
    encodedPassword match {
      case re(_, p) =>
        p == Sha2Crypt.sha256Crypt(password.getBytes("UTF-8"), p)
      case _ =>
        encodedPassword == password
    }
  }
}
