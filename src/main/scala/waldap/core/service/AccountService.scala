package waldap.core.service

import org.apache.directory.api.ldap.model.entry.Entry
import waldap.core.model.{Account, AdminAccount, UserAccount}
import waldap.core.service.SystemSettingsService.SystemSettings
import org.slf4j.LoggerFactory
import waldap.core.controller.Context
import waldap.core.ldap.LdapandaLdapServer


trait AccountService {

  private val logger = LoggerFactory.getLogger(classOf[AccountService])

  def adminAuthenticate(settings: SystemSettings, userName: String, password: String): Option[Account] = {
    val account = if (password == "secret") Some(AdminAccount(userName)) else None

    account
  }

  def userAuthenticate(settings: SystemSettings, userName: String, password: String)(implicit context: Context): Option[Account] = {
    val ds = LdapandaLdapServer.directoryService
    val dnString = s"uid=${userName},ou=Users,o=waldap"

    val account = if (context.ldapSession.exists(dnString)) {
      val dn = context.ldapSession.getDirectoryService.getDnFactory().create(dnString)
      try {
        val session = ds.getSession(dn, password.getBytes())
        Some(UserAccount(userName, dn))
      }catch{
        case _:Throwable => None
      }
    } else {
      None
    }
    account
  }

  def getLdapEntry(account: Account)(implicit context: Context): Option[Entry] = {
    if(account.isInstanceOf[UserAccount]) {
      val dn = account.asInstanceOf[UserAccount].dn
      Some(context.ldapSession.lookup(dn))
    } else {
      None
    }
  }
}
