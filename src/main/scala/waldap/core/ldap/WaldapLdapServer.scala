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
    val settings = loadSystemSettings()
    val bindHost = if(settings.ldapBindOnlyLocal) "127.0.0.1" else "0.0.0.0"
    ldapServer.setTransports(new TcpTransport(bindHost, settings.ldapPort))
    ldapServer.setDirectoryService(directoryService)
    ldapServer.start()
  }

  def restart(): Unit = {
    ldapServer.stop()
    ldapServer = new org.apache.directory.server.ldap.LdapServer()
    init()
  }

  def stop(): Unit = {
    ldapServer.stop()
  }

  def getAdminSession(): CoreSession = directoryService.getAdminSession()
}
