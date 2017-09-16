package waldap.core.ldap;/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

import java.net.URL;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.naming.InvalidNameException;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.schema.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.api.ldap.schema.extractor.impl.ResourceMap;
import org.apache.directory.server.core.api.interceptor.context.AddOperationContext;
import org.apache.directory.server.core.partition.ldif.AbstractLdifPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In-memory schema-only partition which loads the data in the similar way as the
 * {@link org.apache.directory.api.ldap.schemaloader.JarLdifSchemaLoader}.
 *
 * @author Josef Cacek
 */
public class InMemorySchemaPartition extends AbstractLdifPartition {

    private static Logger LOG = LoggerFactory.getLogger(InMemorySchemaPartition.class);

    /**
     * Filesystem path separator pattern, either forward slash or backslash. java.util.regex.Pattern is immutable so only one
     * instance is needed for all uses.
     */

    public InMemorySchemaPartition(SchemaManager schemaManager) {
        super(schemaManager);
    }

    /**
     * Partition initialization - loads schema entries from the files on classpath.
     *
     * @see org.apache.directory.server.core.partition.impl.avl.AvlPartition#doInit()
     */
    @Override
    protected void doInit() throws InvalidNameException, Exception {
        if (initialized)
            return;

        LOG.debug("Initializing schema partition " + getId());
        suffixDn.apply(schemaManager);
        super.doInit();

        // load schema
        final Map<String, Boolean> resMap = ResourceMap.getResources(Pattern.compile("schema[/\\Q\\\\E]ou=schema.*"));
        for (String resourcePath : new TreeSet<String>(resMap.keySet())) {
            if (resourcePath.endsWith(".ldif")) {
                URL resource = DefaultSchemaLdifExtractor.getUniqueResource(resourcePath, "Schema LDIF file");
                LdifReader reader = new LdifReader(resource.openStream());
                LdifEntry ldifEntry = reader.next();
                reader.close();

                Entry entry = new DefaultEntry(schemaManager, ldifEntry.getEntry());
                // add mandatory attributes
                if (entry.get(SchemaConstants.ENTRY_CSN_AT) == null) {
                    entry.add(SchemaConstants.ENTRY_CSN_AT, defaultCSNFactory.newInstance().toString());
                }
                if (entry.get(SchemaConstants.ENTRY_UUID_AT) == null) {
                    entry.add(SchemaConstants.ENTRY_UUID_AT, UUID.randomUUID().toString());
                }
                AddOperationContext addContext = new AddOperationContext(null, entry);
                super.add(addContext);
            }
        }
    }

}