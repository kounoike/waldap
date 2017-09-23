package waldap.core.model

trait WebAppComponent extends TemplateComponent { self: Profile =>
  import profile.api._
  import self._

  lazy val WebApps = TableQuery[WebApps]

  class WebApps(tag: Tag) extends Table[WebApp](tag, "WEBAPP") {
    val name = column[String]("NAME", O PrimaryKey)
    val url = column[Option[String]]("URL")
    val guide = column[String]("GUIDE")

    def * = (name, url, guide) <> (WebApp.tupled, WebApp.unapply)
  }
}

case class WebApp(
  name: String,
  url: Option[String],
  guide: String
)
