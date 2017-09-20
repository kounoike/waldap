import io.github.gitbucket.scalatra.forms.ValidationJavaScriptProvider
import org.scalatra._
import javax.servlet._

import waldap.core.controller.IndexController
import waldap.core.ldap.LdapandaLdapServer

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    // access filter
    context.mount(new waldap.core.controller.admin.PreprocessController, "/*")

    context.mount(new IndexController, "/*")
    context.mount(new waldap.core.controller.admin.IndexController, "/*")
    context.mount(new waldap.core.controller.admin.UserController, "/*")
    context.mount(new waldap.core.controller.admin.GroupController, "/*")
    context.mount(new waldap.core.controller.admin.ApplicationController, "/*")

    context.mount(new waldap.core.controller.user.IndexController, "/*")

    context.mount(new ValidationJavaScriptProvider, "/assets/js/*")

    LdapandaLdapServer.init()
  }
}
