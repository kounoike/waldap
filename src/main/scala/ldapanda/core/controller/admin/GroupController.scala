package ldapanda.core.controller.admin

import ldapanda.core.controller.ControllerBase
import ldapanda.core.ldap.LdapandaLdapServer
import ldapanda.core.admin.group.html
import org.apache.directory.api.ldap.model.filter.FilterParser
import org.apache.directory.api.ldap.model.message.{AliasDerefMode, SearchScope}
import org.apache.directory.api.ldap.model.name.Dn
import org.scalatra.FlashMapSupport


trait GroupControllerBase extends ControllerBase with FlashMapSupport {
  get("/admin/groups"){
    val adminSession = LdapandaLdapServer.directoryService.getAdminSession()
    val dn = new Dn(LdapandaLdapServer.directoryService.getSchemaManager, "ou=Groups,o=ldapanda")
    val groupsCursor = adminSession.search(dn, SearchScope.ONELEVEL,
      FilterParser.parse("(objectClass=groupOfNames)"), AliasDerefMode.DEREF_ALWAYS,
      "cn", "businesscategory", "member", "description"
    )
    html.grouplist(groupsCursor)
  }
}


class GroupController extends GroupControllerBase{

}
