package waldap.core.service

import waldap.core.controller.Context
import waldap.core.model.{Session, WebApp, WebAppInstance}
import waldap.core.model.Profile._
import waldap.core.model.Profile.profile.blockingApi._

trait WebAppService extends LDAPAccountService {
  def getAllWebApps()(implicit s: Session): List[WebApp] = {
    WebApps sortBy(_.name) list
  }

  def getWebAppInstance(appName: String, instanceSuffix: String)(implicit s: Session): Option[WebAppInstance] = {
    WebAppInstances.filter(x => x.webAppName === appName.bind && x.instanceSuffix === instanceSuffix.bind).firstOption
  }

  def getWebAppInstanceMap(implicit s:Session): Map[WebApp, List[WebAppInstance]] = {
    getAllWebApps().map(app =>
      (app -> (WebAppInstances.filter(_.webAppName === app.name.bind) list))
    ).toMap
  }

  def insertWebAppInstance(webAppName: String, instanceSuffix: String, url: String)(implicit context: Context, s: Session): Unit = {
    WebAppInstances insert WebAppInstance(
      webAppName = webAppName,
      instanceSuffix = instanceSuffix,
      url = url
    )
    val app = WebApps.filter(_.name === webAppName.bind).first
    app.userType match {
      case "USER" =>
        AddLDAPGroup(s"${webAppName}_${instanceSuffix}_Users", webAppName, instanceSuffix)
      case "USER_ADMIN" =>
        AddLDAPGroup(s"${webAppName}_${instanceSuffix}_Users", webAppName, instanceSuffix)
        AddLDAPGroup(s"${webAppName}_${instanceSuffix}_Admins", webAppName, instanceSuffix)
    }
  }

  def deleteWebAppInstance(id: Int)(implicit context: Context, s: Session): Unit = {
    val instances = WebAppInstances.filter(_.id === id.bind)
    val instance = instances.first
    val webapp = WebApps.filter(_.name === instance.webAppName).first
    webapp.userType match {
      case "USER" =>
        DeleteLDAPGroup(s"${webapp.name}_${instance.instanceSuffix}_Users")
      case "USER_ADMIN" =>
        DeleteLDAPGroup(s"${webapp.name}_${instance.instanceSuffix}_Users")
        DeleteLDAPGroup(s"${webapp.name}_${instance.instanceSuffix}_Admins")
    }
    instances.delete
  }
}

object WebAppService extends WebAppService with LDAPAccountService
