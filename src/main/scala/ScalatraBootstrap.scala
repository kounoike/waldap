import io.github.gitbucket.scalatra.forms.ValidationJavaScriptProvider
import org.scalatra._
import javax.servlet._

import waldap.core.controller.IndexController
import waldap.core.ldap.LdapandaLdapServer

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new IndexController, "/")
    context.mount(new waldap.core.controller.admin.IndexController, "/admin")
    context.mount(new waldap.core.controller.admin.UserController, "/admin/users")
    context.mount(new waldap.core.controller.admin.GroupController, "/admin/groups")
    context.mount(new waldap.core.controller.admin.ApplicationController, "/admin/apps")
    context.mount(new ValidationJavaScriptProvider, "/assets/js/*")

    LdapandaLdapServer.init()
  }
}
