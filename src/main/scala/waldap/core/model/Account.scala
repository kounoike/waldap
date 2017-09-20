package waldap.core.model

import org.apache.directory.api.ldap.model.name.Dn

trait Account {
  val userName: String
  val isAdmin: Boolean
}

case class AdminAccount(username: String) extends Account {
  override val userName: String = username
  override val isAdmin: Boolean = true
}

case class UserAccount(username: String, dn: Dn) extends Account {
  override val userName: String = username
  override val isAdmin: Boolean = false
}
