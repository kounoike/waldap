package waldap.core.model

trait WebAppComponent extends TemplateComponent { self: Profile =>
  import profile.api._
  import self._

  lazy val WebApps = TableQuery[WebApps]

  class WebApps(tag: Tag) extends Table[WebApp](tag, "WEBAPP") {
    val id = column[Int]("ID", O PrimaryKey)
    val name = column[String]("NAME")
    val url = column[String]("URL")
    val guideTemplate = column[String]("GUIDE_TEMPLATE")
    val userType = column[String]("USER_TYPE")

    def * = (id, name, url, guideTemplate, userType) <> (WebApp.tupled, WebApp.unapply)
  }
}

case class WebApp(
  id: Int,
  name: String,
  url: String,
  guideTemplate: String,
  userType: String
)
