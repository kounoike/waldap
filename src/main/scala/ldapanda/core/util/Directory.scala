package ldapanda.core.util

import java.io.File

object Directory {
  val LdapandaHome = (System.getProperty("ldapanda.home") match {
    case path if (path != null) =>
      new File(path)
    case _ =>
      scala.util.Properties.envOrNone("LDAPANDA_HOME") match {
        case Some(env) =>
          new File(env)
        case None => {
          new File(System.getProperty("user.home"), ".ldapanda")
        }
      }
  }).getAbsolutePath

  val LdapandaConf = new File(LdapandaHome, "ldapanda.conf")

  val IncetanceHome = s"${LdapandaHome}/server"
}
