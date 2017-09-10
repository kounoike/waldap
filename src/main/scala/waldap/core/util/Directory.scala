package waldap.core.util

import java.io.File

object Directory {
  val LdapandaHome = (System.getProperty("waldap.home") match {
    case path if (path != null) =>
      new File(path)
    case _ =>
      scala.util.Properties.envOrNone("WALDAP_HOME") match {
        case Some(env) =>
          new File(env)
        case None => {
          new File(System.getProperty("user.home"), ".waldap")
        }
      }
  }).getAbsolutePath

  val LdapandaConf = new File(LdapandaHome, "waldap.conf")

  val IncetanceHome = s"${LdapandaHome}/server"
}
