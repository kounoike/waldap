package waldap.core.service

import waldap.core.model.{Account, AdminAccount}
import waldap.core.service.SystemSettingsService.SystemSettings
import org.slf4j.LoggerFactory


trait AccountService {

  private val logger = LoggerFactory.getLogger(classOf[AccountService])

  def adminAuthenticate(settings: SystemSettings, userName: String, password: String): Option[Account] = {
    val account = if (password == "secret") Some(AdminAccount(userName)) else None

    account
  }

}
