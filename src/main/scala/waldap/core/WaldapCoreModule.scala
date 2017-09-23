package waldap.core

import io.github.gitbucket.solidbase.migration.{SqlMigration, LiquibaseMigration}
import io.github.gitbucket.solidbase.model.{Version, Module}

object WaldapCoreModule extends Module("waldap-core",
  new Version("1.0.0", new LiquibaseMigration("update/waldap-core_1.0.xml"))
)
