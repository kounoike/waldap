import io.github.gitbucket.scalatra.forms.ValidationJavaScriptProvider
import org.scalatra._
import javax.servlet._

import ldapanda.core.controller.{AdminController, IndexController}
import ldapanda.core.ldap.LdapandaLdapServer

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new IndexController, "/")
    context.mount(new AdminController, "/admin")
    context.mount(new ValidationJavaScriptProvider, "/assets/js/*")

    LdapandaLdapServer.init()
  }
}