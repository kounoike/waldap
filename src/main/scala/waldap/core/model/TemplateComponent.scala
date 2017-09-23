package waldap.core.model

trait TemplateComponent { self: Profile =>
  import profile.api._

  trait BasicTemplate { self: Table[_] =>
  }
}
