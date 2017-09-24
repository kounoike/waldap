import java.util.EnumSet

import io.github.gitbucket.scalatra.forms.ValidationJavaScriptProvider
import org.scalatra._
import javax.servlet._

import waldap.core.controller.{IndexController, PreprocessController}
import waldap.core.servlet.TransactionFilter
import waldap.core.util.Database

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.addFilter("transactionFilter", new TransactionFilter)
    context.getFilterRegistration("transactionFilter").addMappingForUrlPatterns(EnumSet.allOf(classOf[DispatcherType]), true, "/*")

    context.mount(new PreprocessController, "/*")

    context.mount(new IndexController, "/*")
    context.mount(new waldap.core.controller.admin.IndexController, "/*")
    context.mount(new waldap.core.controller.admin.UserController, "/*")
    context.mount(new waldap.core.controller.admin.WebAppsController, "/*")
    context.mount(new waldap.core.controller.admin.SettingsController, "/*")

    context.mount(new waldap.core.controller.user.IndexController, "/*")

    context.mount(new ValidationJavaScriptProvider, "/assets/js/*")
  }

  override def destroy(context: ServletContext): Unit = {
    Database.closeDataSource()
    super.destroy(context)
  }
}
