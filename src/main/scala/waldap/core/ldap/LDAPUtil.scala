package waldap.core.ldap

import org.apache.commons.codec.digest.Sha2Crypt

object LDAPUtil {
  def encodePassword(plain: String): String = {
    "{CRYPT}" + Sha2Crypt.sha256Crypt(plain.getBytes("UTF-8"))
  }
}
