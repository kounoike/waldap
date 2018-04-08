package waldap.core.servlet

import java.io.File
import javax.servlet.{ServletContextEvent, ServletContextListener}

import io.github.gitbucket.solidbase.manager.JDBCVersionManager
import org.slf4j.LoggerFactory
import waldap.core.service.SystemSettingsService
import waldap.core.util.Database
import waldap.core.WaldapCoreModule
import waldap.core.model.Profile.profile.blockingApi._
import io.github.gitbucket.solidbase.Solidbase
import waldap.core.ldap.WaldapLdapServer

import scala.collection.JavaConverters._

class InitializeListner extends ServletContextListener with SystemSettingsService {
  private val logger = LoggerFactory.getLogger(getClass)

  override def contextInitialized(event: ServletContextEvent): Unit = {
    val settings = loadSystemSettings()

    org.h2.Driver.load()
    Database() withTransaction { session =>
      val conn = session.conn
      val manager = new JDBCVersionManager(conn)

      logger.info("Start Database schema update")
      new Solidbase()
        .migrate(conn, Thread.currentThread.getContextClassLoader, settings.db.liquiDriver, WaldapCoreModule)

      val databaseVersion = manager.getCurrentVersion(WaldapCoreModule.getModuleId)
      val waldapVersion = WaldapCoreModule.getVersions.asScala.last.getVersion
      if (databaseVersion != waldapVersion) {
        throw new IllegalStateException(
          s"Initialization failed. WALDAP version is ${waldapVersion}, but database version is ${databaseVersion}"
        )
      }

      WaldapLdapServer.init()
    }
  }

  override def contextDestroyed(event: ServletContextEvent): Unit = {
    Database.closeDataSource()
    WaldapLdapServer.stop()
  }
}
