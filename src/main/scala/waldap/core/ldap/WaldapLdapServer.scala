package waldap.core.ldap

import org.apache.directory.server.constants.ServerDNConstants
import org.apache.directory.api.ldap.model.entry.{DefaultModification, ModificationOperation}
import org.apache.directory.api.ldap.model.name.Dn
import waldap.core.service.SystemSettingsService
import org.apache.directory.server.core.api.CoreSession
import org.apache.directory.server.ldap.handlers.extended.StartTlsHandler
import org.apache.directory.server.protocol.shared.transport.TcpTransport
import waldap.core.util.Directory


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

    println("TESTTESTTEST")
    ldapServer.setCertificatePassword("hogehoge")
    ldapServer.setKeystoreFile(Directory.WaldapHome + "/cert/hoge.ks")
    ldapServer.loadKeyStore()

    val tlsHandler = new StartTlsHandler()
    tlsHandler.setLdapServer(ldapServer)

    val transport = new TcpTransport(bindHost, 10636)
    transport.setEnableSSL(true)
    ldapServer.addTransports(transport)

    ldapServer.start()

    val con = directoryService.getAdminSession
    val passwordRemove = new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE, "userPassword")
    val passwordAdd = new DefaultModification(ModificationOperation.ADD_ATTRIBUTE, "userPassword",
      LDAPUtil.encodePassword(settings.adminPassword))
    con.modify(new Dn(con.getDirectoryService.getSchemaManager, ServerDNConstants.ADMIN_SYSTEM_DN), passwordRemove, passwordAdd)
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
