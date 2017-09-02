package ldapanda.core.controller.admin

import ldapanda.core.controller.ControllerBase
import org.scalatra.FlashMapSupport

trait ApplicationControllerBase extends ControllerBase with FlashMapSupport {
  get("/admin/apps"){
    """apps"""
  }
}

class ApplicationController extends ApplicationControllerBase{

}
