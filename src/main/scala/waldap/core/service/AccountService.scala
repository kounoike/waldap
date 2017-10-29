package waldap.core.service

import org.apache.directory.api.ldap.model.entry.Entry
import waldap.core.model.{Account, AdminAccount, UserAccount}
import waldap.core.ldap.LDAPUtil
import waldap.core.service.SystemSettingsService.SystemSettings
import org.slf4j.LoggerFactory
import waldap.core.controller.Context
import waldap.core.ldap.WaldapLdapServer


trait AccountService {

  private val logger = LoggerFactory.getLogger(classOf[AccountService])

  def adminAuthenticate(settings: SystemSettings, userName: String, password: String): Option[Account] = {
    if (LDAPUtil.checkPassword(settings.adminPassword, password)) Some(AdminAccount(userName)) else None
  }

  def userAuthenticate(settings: SystemSettings, userName: String, password: String)(implicit context: Context): Option[Account] = {
    val ds = WaldapLdapServer.directoryService
    val dnString = s"uid=$userName,ou=Users,o=waldap"

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
