package waldap.core.view

import org.webjars.WebJarAssetLocator
import waldap.core.controller.Context

object helpers {
  val webJarsLocator: WebJarAssetLocator = new WebJarAssetLocator()

  def webjars(webjar: String, exactPath: String)(implicit context: Context): String = {
    Option(webJarsLocator.getFullPathExact(webjar, exactPath)) match {
      case Some(fullPath) =>
        fullPath.replaceFirst("^META-INF/resources", context.path)
      case None =>
        throw new IllegalArgumentException(s"WebJar resource not found: ${webjar}, ${exactPath}")
    }
  }
}
