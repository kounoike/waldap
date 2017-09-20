package waldap.core.controller.admin

import waldap.core.controller.ControllerBase
import org.scalatra.FlashMapSupport

trait ApplicationControllerBase extends ControllerBase with FlashMapSupport {
  get("/admin/apps"){
    s"""apps ${context.loginAccount.isEmpty}"""
  }
}

class ApplicationController extends ApplicationControllerBase{

}
