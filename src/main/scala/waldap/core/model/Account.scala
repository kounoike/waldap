package waldap.core.model

trait Account {
  val userName: String
  val isAdmin: Boolean
}

case class AdminAccount(username: String) extends Account {
  override val userName: String = username
  override val isAdmin: Boolean = true
}
