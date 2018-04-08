package waldap.core.model

import com.github.takezoe.slick.blocking.BlockingJdbcProfile
import waldap.core.service.SystemSettingsService

trait Profile {
  val profile: BlockingJdbcProfile
  import profile.blockingApi._

}

trait ProfileProvider { self: Profile with SystemSettingsService =>
  lazy val profile = loadSystemSettings().db.slickDriver
}

trait CoreProfile
    extends ProfileProvider
    with Profile
    with SystemSettingsService
    with WebAppComponent
    with WebAppInstanceComponent

object Profile extends CoreProfile
