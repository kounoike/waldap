package waldap.core.controller.admin

import io.github.gitbucket.scalatra.forms._
import org.scalatra.FlashMapSupport
import waldap.core.controller.ControllerBase
import waldap.core.admin.webapp.html
import waldap.core.service.WebAppService
import waldap.core.util.Implicits._

class WebAppsController extends ControllerBase with WebAppService with FlashMapSupport {

  case class AddInstanceForm(webAppName: String, instanceSuffix: String, url: String)

  val addInstanceForm = mapping(
    "webAppName" -> text(required),
    "instanceSuffix" -> text(required),
    "url" -> text(required)
  )(AddInstanceForm.apply)

  case class EditInstanceForm(webAppName: String, instanceSuffix: String, url: String)

  val editInstanceForm = mapping(
    "webAppName" -> text(required),
    "instanceSuffix" -> text(required),
    "url" -> text(required)
  )(EditInstanceForm.apply)

  get("/admin/apps") {
    val apps = getAllWebApps()
    val instanceMap = getWebAppInstanceMap()
    html.list(apps, instanceMap, flash.get("info"))
  }

  post("/admin/apps/instance/add", addInstanceForm) { form =>
    insertWebAppInstance(form.webAppName, form.instanceSuffix, form.url)
    redirect("/admin/apps")
  }

  post("/admin/apps/instance/:id/edit", editInstanceForm) { form =>
    val id = params("id").toInt
    editWebAppInstance(id, form.webAppName, form.instanceSuffix, form.url)
    redirect("/admin/apps")
  }

  get("/admin/apps/instance/:id/delete") {
    deleteWebAppInstance(params.get("id").get.toInt)
    redirect("/admin/apps")
  }
}
