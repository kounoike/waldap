package waldap.core.ldap

import org.apache.directory.server.core.api.CoreSession
import org.apache.directory.server.protocol.shared.transport.TcpTransport

object WaldapLdapServer{
  val dsFactory = new WaldapDirectoryServiceFactory()
  dsFactory.init(LDAPUtil.ldapName)
  val directoryService = dsFactory.getDirectoryService()
  val ldapServer = new org.apache.directory.server.ldap.LdapServer()

  def init(): Unit = {
    ldapServer.setTransports(new TcpTransport("0.0.0.0", 10389))
    ldapServer.setDirectoryService(directoryService)
    ldapServer.start()
  }

  def getAdminSession(): CoreSession = directoryService.getAdminSession()
}
