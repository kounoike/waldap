package waldap.core.controller.admin

import waldap.core.controller.ControllerBase
import waldap.core.ldap.WaldapLdapServer
import waldap.core.admin.group.html
import org.apache.directory.api.ldap.model.filter.FilterParser
import org.apache.directory.api.ldap.model.message.{AliasDerefMode, SearchScope}
import org.apache.directory.api.ldap.model.name.Dn
import org.scalatra.FlashMapSupport


trait GroupControllerBase extends ControllerBase with FlashMapSupport {
  get("/admin/groups"){
    val adminSession = WaldapLdapServer.directoryService.getAdminSession()
    val dn = new Dn(WaldapLdapServer.directoryService.getSchemaManager, "ou=Groups,o=waldap")
    val groupsCursor = adminSession.search(dn, SearchScope.ONELEVEL,
      FilterParser.parse("(objectClass=groupOfNames)"), AliasDerefMode.DEREF_ALWAYS,
      "cn", "businesscategory", "member", "description"
    )
    html.grouplist(groupsCursor)
  }
}


class GroupController extends GroupControllerBase{

}
