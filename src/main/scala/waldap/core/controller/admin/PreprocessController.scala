package waldap.core.controller.admin

import waldap.core.controller.{Context, ControllerBase}
import waldap.core.util.Keys
import waldap.core.util.Implicits._

class PreprocessController extends ControllerBase {

  get(context.currentPath.startsWith("/user") && context.loginAccount.map{_.isAdmin}.getOrElse(false)){
    org.scalatra.Forbidden("Access Denied")
  }

  get(context.currentPath.startsWith("/admin") && context.loginAccount.map{!_.isAdmin}.getOrElse(false)){
    org.scalatra.Forbidden("Access Denied")
  }

  get(context.loginAccount.isEmpty && context.currentPath != "/" && !context.currentPath.startsWith("/assets")
    && !context.currentPath.startsWith("/webjars")
    && !context.currentPath.startsWith("/admin/signin") && !context.currentPath.startsWith("/user/signin") ) {
    Unauthorized()
  }

  protected def Unauthorized()(implicit context: Context): Unit = {
    if(request.hasAttribute(Keys.Request.Ajax)){
      org.scalatra.Unauthorized()
    } else {
      if(context.loginAccount.isDefined){
        org.scalatra.Unauthorized(redirect("/"))
      } else {
        if(context.currentPath.startsWith("/admin")){
          org.scalatra.Unauthorized(redirect("/admin/signin"))
        }
        else{
          org.scalatra.Unauthorized(redirect("/user/signin"))
        }
      }
    }
  }
}
