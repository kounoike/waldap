package waldap.core.controller.admin

import waldap.core.controller.ControllerBase
import waldap.core.service.{LDAPAccountService, SystemSettingsService}
import waldap.core.ldap.{LDAPUtil, WaldapLdapServer}
import waldap.core.admin.html
import io.github.gitbucket.scalatra.forms._
import org.scalatra.FlashMapSupport

class SettingsController extends ControllerBase with FlashMapSupport with SystemSettingsService with LDAPAccountService {
  case class SettingsForm(baseUrl: Option[String], ldapPort: Int)

  private val settingForm = mapping(
    "baseUrl" -> trim(optional(text())),
    "ldapPort" -> number(required)
  )(SettingsForm.apply)

  private val passwordForm = mapping(
    "adminPassword" -> text(required),
    "adminPasswordRetype" -> text(required)
  )(PasswordForm.apply)
  case class PasswordForm(adminPassword: String, adminPasswordRetype: String)

  get("/admin/password"){
    html.password(flash.get("info"))
  }

  post("/admin/password", passwordForm){ form =>
    if(form.adminPassword != form.adminPasswordRetype){
      flash += "info" -> context.messages.get("settings.passwordMismatch")
    }
    else{
      val adminPassword = LDAPUtil.encodePassword(form.adminPassword)
      val newSettings = SystemSettingsService.SystemSettings(context.settings.baseUrl, adminPassword, context.settings.ldapPort, context.settings.db)
      saveSystemSettings(newSettings)
      ChangeLDAPAdminPassword(form.adminPassword)

      flash += "info" -> context.messages.get("settings.passwordChanged")
    }
    redirect("/admin/password")
  }

  get("/admin/system"){
    html.setting(flash.get("info"))
  }

  post("/admin/system", settingForm){ form =>
    val newSettings = SystemSettingsService.SystemSettings(form.baseUrl, context.settings.adminPassword, form.ldapPort, context.settings.db)
    val needRestartLdap: Boolean = context.settings.ldapPort != newSettings.ldapPort
    saveSystemSettings(newSettings)

    if(needRestartLdap){
      WaldapLdapServer.restart()
    }

    flash += "info" -> context.messages.get("settings.saved")

    redirect("/admin/system")
  }
}
