package waldap.core

import io.github.gitbucket.solidbase.migration.{SqlMigration, LiquibaseMigration}
import io.github.gitbucket.solidbase.model.{Version, Module}

object WaldapCoreModule extends Module("waldap-core",
  new Version("0.9.0", new LiquibaseMigration("update/waldap-core_0.9.xml")),
  new Version("0.9.1")
)
