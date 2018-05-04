package waldap.core.service

import java.io.File
import javax.servlet.http.HttpServletRequest

import waldap.core.util.SyntaxSugars._
import waldap.core.util.ConfigUtil._
import waldap.core.util.Directory
import waldap.core.util.Implicits._
import SystemSettingsService._
import com.github.takezoe.slick.blocking.{BlockingH2Driver, BlockingJdbcProfile, BlockingMySQLDriver}
import liquibase.database.AbstractJdbcDatabase
import liquibase.database.core.{H2Database, MySQLDatabase, PostgresDatabase}

trait SystemSettingsService {

  def saveSystemSettings(settings: SystemSettings): Unit = {
    defining(new java.util.Properties()) { props =>
      settings.baseUrl.foreach(x => props.setProperty(BaseURL, x.replace("/\\Z", "")))
      props.setProperty(AdminPassword, settings.adminPassword)
      props.setProperty(LdapBindOnlyLocal, settings.ldapBindOnlyLocal.toString)
      props.setProperty(LdapPort, settings.ldapPort.toString)
      props.setProperty(DatabaseUrl, settings.db.url)
      props.setProperty(DatabaseUser, settings.db.user)
      props.setProperty(DatabasePassword, settings.db.password)
      settings.db.connectionTimeout.foreach(x => props.setProperty(DatabaseConnectionTimeout, x.toString))
      settings.db.idleTimeout.foreach(x => props.setProperty(DatabaseIdleTimeout, x.toString))
      settings.db.maxLifetime.foreach(x => props.setProperty(DatabaseMaxLifetime, x.toString))
      settings.db.minimumIdle.foreach(x => props.setProperty(DatabaseMinimumIdle, x.toString))
      settings.db.maximumPoolSize.foreach(x => props.setProperty(DatabaseMaximumPoolSize, x.toString))
      val home = new File(Directory.WaldapHome)
      if (!home.exists()) {
        home.mkdirs()
      }
      using(new java.io.FileOutputStream(Directory.WaldapConf)) { out =>
        props.store(out, null)
      }
    }
  }

  def loadSystemSettings(): SystemSettings = {
    defining(new java.util.Properties()) { props =>
      if (Directory.WaldapConf.exists) {
        using(new java.io.FileInputStream(Directory.WaldapConf)) { in =>
          props.load(in)
        }
      }
      val settings = SystemSettings(
        getOptionValue[String](props, BaseURL, None).map(x => x.replaceFirst("/\\Z", "")),
        getValue[String](props, AdminPassword, "secret"),
        getValue[Boolean](props, LdapBindOnlyLocal, true),
        getValue[Int](props, LdapPort, 10389),
        Database(
          getValue[String](props, DatabaseUrl, s"jdbc:h2:${Directory.DatabaseHome};MVCC=true"),
          getValue[String](props, DatabaseUser, "sa"),
          getValue[String](props, DatabasePassword, "sa"),
          getOptionValue[Long](props, DatabaseConnectionTimeout, None),
          getOptionValue[Long](props, DatabaseIdleTimeout, None),
          getOptionValue[Long](props, DatabaseMaxLifetime, None),
          getOptionValue[Int](props, DatabaseMinimumIdle, None),
          getOptionValue[Int](props, DatabaseMaximumPoolSize, None)
        )
      )
      if (!Directory.WaldapConf.exists()) {
        saveSystemSettings(settings)
      }
      settings
    }
  }
}

object SystemSettingsService {
  import scala.reflect.ClassTag

  case class SystemSettings(
    baseUrl: Option[String],
    adminPassword: String,
    ldapBindOnlyLocal: Boolean,
    ldapPort: Int,
    db: Database
  ) {
    def baseUrl(request: HttpServletRequest): String = baseUrl.fold(request.baseUrl)(_.stripSuffix("/"))
  }

  case class Database(
    url: String,
    user: String,
    password: String,
    connectionTimeout: Option[Long],
    idleTimeout: Option[Long],
    maxLifetime: Option[Long],
    minimumIdle: Option[Int],
    maximumPoolSize: Option[Int]
  ) {
    def jdbcDriver: String = DatabaseType(url).jdbcDriver
    def slickDriver: BlockingJdbcProfile = DatabaseType(url).slickDriver
    def liquiDriver: AbstractJdbcDatabase = DatabaseType(url).liquiDriver
  }

  private val BaseURL = "base_url"
  private val AdminPassword = "admin.password"
  private val LdapBindOnlyLocal = "ldap.onlyLocal"
  private val LdapPort = "ldap.port"

  private val DatabaseUrl = "db.url"
  private val DatabaseUser = "db.user"
  private val DatabasePassword = "db.pw"
  private val DatabaseConnectionTimeout = "db.connectionTimeout"
  private val DatabaseIdleTimeout = "db.idleTimeout"
  private val DatabaseMaxLifetime = "db.maxLifetime"
  private val DatabaseMinimumIdle = "db.minimumIdle"
  private val DatabaseMaximumPoolSize = "db.maximumPoolSize"

  private def getValue[A: ClassTag](props: java.util.Properties, key: String, default: A): A = {
    getSystemProperty(key).getOrElse(getEnvironmentVariable(key).getOrElse {
      defining(props.getProperty(key)) { value =>
        if (value == null || value.isEmpty) {
          default
        } else {
          convertType(value).asInstanceOf[A]
        }
      }
    })
  }

  private def getOptionValue[A: ClassTag](props: java.util.Properties, key: String, default: Option[A]): Option[A] = {
    getSystemProperty(key).orElse(getEnvironmentVariable(key).orElse {
      defining(props.getProperty(key)) { value =>
        if (value == null || value.isEmpty) {
          default
        } else {
          Some(convertType(value)).asInstanceOf[Option[A]]
        }
      }
    })
  }
}

sealed trait DatabaseType {
  val jdbcDriver: String
  val slickDriver: BlockingJdbcProfile
  val liquiDriver: AbstractJdbcDatabase
}
object DatabaseType {

  def apply(url: String): DatabaseType = {
    if (url.startsWith("jdbc:h2:")) {
      H2
    } else if (url.startsWith("jdbc:mysql:")) {
      MySQL
    } else if (url.startsWith("jdbc:postgresql:")) {
      PostgreSQL
    } else {
      throw new IllegalArgumentException(s"$url is not supported.")
    }
  }

  object H2 extends DatabaseType {
    val jdbcDriver = "org.h2.Driver"
    val slickDriver = BlockingH2Driver
    val liquiDriver = new H2Database()
  }

  object MySQL extends DatabaseType {
    val jdbcDriver = "org.mariadb.jdbc.Driver"
    val slickDriver = BlockingMySQLDriver
    val liquiDriver = new MySQLDatabase()
  }

  object PostgreSQL extends DatabaseType {
    val jdbcDriver = "org.postgresql.Driver2"
    val slickDriver = BlockingPostgresDriver
    val liquiDriver = new PostgresDatabase()
  }

  object BlockingPostgresDriver extends slick.jdbc.PostgresProfile with BlockingJdbcProfile {
    override def quoteIdentifier(id: String): String = {
      val s = new StringBuilder(id.length + 4) append '"'
      for (c <- id) if (c == '"') s append "\"\"" else s append c.toLower
      (s append '"').toString
    }
  }
}
