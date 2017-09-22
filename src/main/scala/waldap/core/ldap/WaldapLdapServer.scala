package waldap.core.ldap

import waldap.core.service.SystemSettingsService
import org.apache.directory.server.core.api.CoreSession
import org.apache.directory.server.protocol.shared.transport.TcpTransport

case class WaldapLdapServer() extends SystemSettingsService {
}

object WaldapLdapServer extends WaldapLdapServer {
  val dsFactory = new WaldapDirectoryServiceFactory()
  dsFactory.init(LDAPUtil.ldapName)
  val directoryService = dsFactory.getDirectoryService()
  var ldapServer = new org.apache.directory.server.ldap.LdapServer()

  def init(): Unit = {
    ldapServer.setTransports(new TcpTransport("0.0.0.0", loadSystemSettings().ldapPort))
    ldapServer.setDirectoryService(directoryService)
    ldapServer.start()
  }

  def restart(): Unit = {
    ldapServer.stop()
    ldapServer = new org.apache.directory.server.ldap.LdapServer()
    init()
  }

  def getAdminSession(): CoreSession = directoryService.getAdminSession()
}
