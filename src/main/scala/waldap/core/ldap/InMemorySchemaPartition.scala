package waldap.core.ldap

import java.util.UUID
import java.util.regex.Pattern

import org.apache.directory.api.ldap.model.constants.SchemaConstants
import org.apache.directory.api.ldap.model.csn.CsnFactory
import org.apache.directory.api.ldap.model.entry.{DefaultEntry, Entry}
import org.apache.directory.api.ldap.model.ldif.LdifReader

import scala.collection.JavaConverters._
import org.apache.directory.api.ldap.model.schema.SchemaManager
import org.apache.directory.api.ldap.schema.extractor.impl.{DefaultSchemaLdifExtractor, ResourceMap}
import org.apache.directory.server.core.api.interceptor.context.AddOperationContext
import org.apache.directory.server.core.partition.ldif.AbstractLdifPartition
import org.slf4j.{Logger, LoggerFactory}

class InMemorySchemaPartition(schemaManager: SchemaManager) extends AbstractLdifPartition(schemaManager) {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  override protected def doInit(): Unit = {
    if (!initialized) {
      logger.debug("Initializing schema partition " + getId())
      suffixDn.apply(schemaManager)
      super.doInit()

      // load schema
      val resMap = ResourceMap.getResources(Pattern.compile("schema[/\\Q\\\\E]ou=schema.*"))
      val keySet = new java.util.TreeSet[String](resMap.keySet()).asScala
      keySet.foreach { resourcePath =>
        if (resourcePath.endsWith(".ldif")) {
          val resource = DefaultSchemaLdifExtractor.getUniqueResource(resourcePath, "Schema LDIF file")
          val reader = new LdifReader(resource.openStream)
          val ldifEntry = reader.next
          reader.close()

          val entry: Entry = new DefaultEntry(schemaManager, ldifEntry.getEntry)
          //add mandatory attributes
          if (Option(entry.get(SchemaConstants.ENTRY_CSN_AT)).isEmpty) {
            val csnFactory = new CsnFactory(1)
            entry.add(SchemaConstants.ENTRY_CSN_AT, csnFactory.newInstance.toString)
          }
          if (Option(entry.get(SchemaConstants.ENTRY_UUID_AT)).isEmpty) {
            entry.add(SchemaConstants.ENTRY_UUID_AT, UUID.randomUUID.toString)
          }
          super.add(new AddOperationContext(null, entry))
        }
      }
    }
  }
}
