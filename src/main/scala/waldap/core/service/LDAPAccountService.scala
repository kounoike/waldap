package waldap.core.service

import org.apache.directory.api.ldap.model.entry.{DefaultEntry, DefaultModification, Entry, ModificationOperation}
import org.apache.directory.api.ldap.model.filter.FilterParser
import org.apache.directory.api.ldap.model.message.{AliasDerefMode, SearchScope}
import org.apache.directory.api.ldap.model.name.Dn
import org.apache.directory.api.ldap.model.schema.registries.DefaultAttributeTypeRegistry
import waldap.core.controller.Context
import waldap.core.ldap.{LDAPUtil, WaldapLdapServer}

import scala.collection.JavaConverters._

trait LDAPAccountService {
  def GetLDAPUsers(implicit context: Context): List[Entry] = {
    val dn = new Dn(WaldapLdapServer.directoryService.getSchemaManager, LDAPUtil.usersDn)
    val usersCursor = context.ldapSession.search(
      dn,
      SearchScope.ONELEVEL,
      FilterParser.parse("(objectClass=inetOrgPerson)"),
      AliasDerefMode.DEREF_ALWAYS
    )
    usersCursor.iterator().asScala.toList
  }

  def GetLDAPGroups(implicit context: Context): List[Entry] = {
    val dn = new Dn(WaldapLdapServer.directoryService.getSchemaManager, LDAPUtil.groupsDn)
    val cursor = context.ldapSession.search(
      dn,
      SearchScope.ONELEVEL,
      FilterParser.parse("(objectClass=groupOfNames)"),
      AliasDerefMode.DEREF_ALWAYS
    )
    cursor.iterator().asScala.toList
  }

  def GetLDAPUsersGroups(userName: String)(implicit context: Context): List[Entry] = {
    val dn = s"uid=${userName},${LDAPUtil.usersDn}"
    val entry = context.ldapSession.lookup(new Dn(context.ldapSession.getDirectoryService.getSchemaManager, dn))
    Option(entry.get("memberOf"))
      .map {
        _.iterator().asScala.map { dn =>
          context.ldapSession.lookup(new Dn(context.ldapSession.getDirectoryService.getSchemaManager, dn.getString))
        }.toList
      }
      .getOrElse(List[Entry]())
  }

  def AddLDAPUser(userName: String, password: String, givenName: String, sn: String, displayName: String, mail: String)(
    implicit context: Context
  ): Unit = {
    val dn = s"uid=${userName},${LDAPUtil.usersDn}"
    if (!context.ldapSession.exists(dn)) {
      val entry = new DefaultEntry(context.ldapSession.getDirectoryService.getSchemaManager())
      entry.setDn(dn)
      entry.add("objectClass", "top", "person", "inetOrgPerson", "simulatedMemberOfObjectClass")
      entry.add("uid", userName)
      entry.add("givenName", givenName)
      entry.add("sn", sn)
      entry.add("mail", mail)
      entry.add("userPassword", LDAPUtil.encodePassword(password))
      entry.add("cn", displayName)
      entry.add("displayName", displayName)
      context.ldapSession.add(entry)
    }
  }

  def DeleteLDAPUser(userName: String)(implicit context: Context): Unit = {
    val userDn = s"uid=${userName},${LDAPUtil.usersDn}"
    if (context.ldapSession.exists(userDn)) {
      context.ldapSession
        .search(
          new Dn(WaldapLdapServer.directoryService.getSchemaManager, LDAPUtil.groupsDn),
          SearchScope.ONELEVEL,
          FilterParser.parse(s"(member=${userDn})"),
          AliasDerefMode.DEREF_ALWAYS
        )
        .asScala
        .foreach { entry =>
          entry.remove("member", userDn)
        }

      context.ldapSession.delete(new Dn(context.ldapSession.getDirectoryService.getSchemaManager, userDn))
    }
  }

  def EditLDAPUser(userName: String, givenName: String, sn: String, displayName: String, mail: String)(
    implicit context: Context
  ): Unit = {
    val dn = s"uid=${userName},${LDAPUtil.usersDn}"
    if (context.ldapSession.exists(dn)) {
      val givenNameMod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "givenName", givenName)
      val snMod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "sn", sn)
      val cnMod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "cn", displayName)
      val displayNameMod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "displayName", displayName)
      val mailMod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "mail", mail)
      context.ldapSession.modify(
        new Dn(context.ldapSession.getDirectoryService.getSchemaManager, dn),
        cnMod,
        snMod,
        displayNameMod,
        givenNameMod,
        mailMod
      )
    }
  }

  def ChangeLDAPUserPassword(userName: String, password: String)(implicit context: Context): Unit = {
    val dn = s"uid=${userName},${LDAPUtil.usersDn}"
    if (context.ldapSession.exists(dn)) {
      val passwordMod = new DefaultModification(
        ModificationOperation.REPLACE_ATTRIBUTE,
        "userPassword",
        LDAPUtil.encodePassword(password)
      )
      context.ldapSession.modify(new Dn(context.ldapSession.getDirectoryService.getSchemaManager, dn), passwordMod)
    }
  }

  def ChangeLDAPAdminPassword(password: String)(implicit context: Context): Unit = {
    val dn = LDAPUtil.systemAdmin
    if (context.ldapSession.exists(dn)) {
      val passwordRemove = new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE, "userPassword")
      val passwordAdd =
        new DefaultModification(ModificationOperation.ADD_ATTRIBUTE, "userPassword", LDAPUtil.encodePassword(password))
      context.ldapSession.modify(
        new Dn(context.ldapSession.getDirectoryService.getSchemaManager, dn),
        passwordRemove,
        passwordAdd
      )
    }
  }

  def AddLDAPGroup(groupName: String, webAppName: String, instanceSuffix: String)(implicit context: Context): Unit = {
    val dn = s"cn=${groupName},${LDAPUtil.groupsDn}"
    if (!context.ldapSession.exists(dn)) {
      val entry = new DefaultEntry(context.ldapSession.getDirectoryService.getSchemaManager())
      entry.setDn(dn)
      entry.add("objectClass", "top", "groupOfNames")
      entry.add("cn", groupName)
      entry.add("member", s"${LDAPUtil.systemAdmin}")
      entry.add("o", webAppName)
      entry.add("ou", instanceSuffix)
      context.ldapSession.add(entry)
    }
  }

  def DeleteLDAPGroup(groupName: String)(implicit context: Context): Unit = {
    val groupDn = s"cn=${groupName},${LDAPUtil.groupsDn}"
    if (context.ldapSession.exists(groupDn)) {
      val usersCursor = context.ldapSession.search(
        new Dn(WaldapLdapServer.directoryService.getSchemaManager, LDAPUtil.usersDn),
        SearchScope.ONELEVEL,
        FilterParser.parse(s"(memberOf=${groupDn})"),
        AliasDerefMode.DEREF_ALWAYS
      )
      usersCursor.asScala.foreach { entry =>
        entry.remove("memberOf", groupDn)
      }

      context.ldapSession.delete(new Dn(context.ldapSession.getDirectoryService.getSchemaManager, groupDn))
    }
  }

  def JoinToLDAPGroup(userName: String, groupName: String)(implicit context: Context): Unit = {
    val userDn = s"uid=${userName},${LDAPUtil.usersDn}"
    val groupDn = s"cn=${groupName},${LDAPUtil.groupsDn}"
    if (context.ldapSession.exists(userDn) && context.ldapSession.exists(groupDn)) {
      val userMod = new DefaultModification(ModificationOperation.ADD_ATTRIBUTE, "memberOf", groupDn)
      context.ldapSession.modify(new Dn(context.ldapSession.getDirectoryService.getSchemaManager, userDn), userMod)

      val groupMod = new DefaultModification(ModificationOperation.ADD_ATTRIBUTE, "member", userDn)
      context.ldapSession.modify(new Dn(context.ldapSession.getDirectoryService.getSchemaManager, groupDn), groupMod)
    }
  }

  def DisjoinFromLDAPGroup(userName: String, groupName: String)(implicit context: Context): Unit = {
    val userDn = s"uid=${userName},${LDAPUtil.usersDn}"
    val groupDn = s"cn=${groupName},${LDAPUtil.groupsDn}"
    if (context.ldapSession.exists(userDn) && context.ldapSession.exists(groupDn)) {
      val userMod = new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE, "memberOf", groupDn)
      context.ldapSession.modify(new Dn(context.ldapSession.getDirectoryService.getSchemaManager, userDn), userMod)

      val groupMod = new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE, "member", userDn)
      context.ldapSession.modify(new Dn(context.ldapSession.getDirectoryService.getSchemaManager, groupDn), groupMod)
    }
  }
}

object LDAPAccountService extends LDAPAccountService
