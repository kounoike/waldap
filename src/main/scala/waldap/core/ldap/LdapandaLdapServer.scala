package waldap.core.ldap

import org.apache.directory.server.core.api.{CoreSession, DirectoryService}
import org.apache.directory.server.ldap.LdapServer
import org.apache.directory.server.protocol.shared.transport.TcpTransport

object LdapandaLdapServer{
  val dsFactory = new LdapandaDirectoryServiceFactory()
  dsFactory.init("waldap")
  val directoryService = dsFactory.getDirectoryService()
  val ldapServer = new org.apache.directory.server.ldap.LdapServer()

  def init(): Unit = {
    ldapServer.setTransports(new TcpTransport("0.0.0.0", 10389))
    ldapServer.setDirectoryService(directoryService)
    ldapServer.start()
  }

  def getAdminSession(): CoreSession = directoryService.getAdminSession()
}
