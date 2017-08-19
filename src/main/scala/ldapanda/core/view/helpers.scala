package ldapanda.core.view

import org.webjars.WebJarAssetLocator

object helpers {
  val webJarsLocator: WebJarAssetLocator = new WebJarAssetLocator()

  def webjars(webjar: String, exactPath: String): String = {
    Option(webJarsLocator.getFullPathExact(webjar, exactPath)) match {
      case Some(fullPath) =>
        fullPath.replaceFirst("^META-INF/resources", "")
      case None =>
        throw new IllegalArgumentException(s"WebJar resource not found: ${webjar}, ${exactPath}")
    }
  }
}
