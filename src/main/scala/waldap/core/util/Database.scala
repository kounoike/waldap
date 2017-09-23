package waldap.core.util

import javax.servlet.ServletRequest

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.slf4j.LoggerFactory
import waldap.core.service.SystemSettingsService
import slick.jdbc.JdbcBackend.{Session, Database => SlickDatabase}

object Database extends SystemSettingsService{
  private val settings = loadSystemSettings()

  private val logger = LoggerFactory.getLogger(getClass)

  private val dataSource: HikariDataSource = {
    val config = new HikariConfig()
    config.setDriverClassName(settings.db.jdbcDriver)
    config.setJdbcUrl(settings.db.url)
    config.setUsername(settings.db.user)
    config.setPassword(settings.db.password)
    config.setAutoCommit(false)
    settings.db.connectionTimeout.foreach(config.setConnectionTimeout)
    settings.db.idleTimeout.foreach(config.setIdleTimeout)
    settings.db.maxLifetime.foreach(config.setMaxLifetime)
    settings.db.minimumIdle.foreach(config.setMinimumIdle)
    settings.db.maximumPoolSize.foreach(config.setMaximumPoolSize)

    logger.debug("load database connection pool")
    new HikariDataSource(config)
  }

  private val db: SlickDatabase = {
    SlickDatabase.forDataSource(dataSource, Some(dataSource.getMaximumPoolSize))
  }

  def apply(): SlickDatabase = db

  def getSession(req: ServletRequest): Session =
    req.getAttribute(Keys.Request.DBSession).asInstanceOf[Session]

  def closeDataSource(): Unit = dataSource.close

}
