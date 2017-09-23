package waldap.core.model

trait WebAppInstanceComponent extends TemplateComponent { self: Profile =>
  import profile.api._
  import self._

  lazy val WebAppInstances = TableQuery[WebAppInstances]

  class WebAppInstances(tag: Tag) extends Table[WebAppInstance](tag, "WEBAPP_INSTANCE") {
    val id = column[Int]("ID", O AutoInc)
    val webAppName = column[String]("WEBAPP_NAME")
    val instanceSuffix = column[String]("INSTANCE_SUFFIX")
    val url = column[String]("URL")

    def * = (id, webAppName, instanceSuffix, url) <> (WebAppInstance.tupled, WebAppInstance.unapply)
  }
}

case class WebAppInstance (
  id: Int = 0,
  webAppName: String,
  instanceSuffix: String,
  url: String
)
