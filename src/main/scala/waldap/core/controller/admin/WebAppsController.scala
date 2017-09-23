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

  get("/admin/apps") {
    val instanceMap = getWebAppInstanceMap
    html.list(instanceMap, flash.get("info"))
  }

  post("/admin/apps/instance/add", addInstanceForm) { form =>
    insertWebAppInstance(form.webAppName, form.instanceSuffix, form.url)
    redirect("/admin/apps")
  }

  get("/admin/apps/instance/:id/delete") {
    deleteWebAppInstance(params.get("id").get.toInt)
    redirect("/admin/apps")
  }
}
