import org.scalatra.sbt._
import org.scalatra.sbt.DistPlugin._
import org.scalatra.sbt.DistPlugin.DistKeys._

organization := "io.github.kounoike"
name := "waldap"
version := "0.9.1"
scalaVersion := "2.12.4"
scalacOptions := Seq("-deprecation", "-language:postfixOps")
val JettyVersion = "9.3.19.v20170502"

libraryDependencies ++= Seq(
  "org.eclipse.jetty"    % "jetty-webapp"      % JettyVersion % "provided",
  "javax.servlet"        % "javax.servlet-api" % "3.1.0" % "provided",
  "org.scalatra"        %% "scalatra"          % "2.5.1",
  "org.scalatra"        %% "scalatra-specs2"   % "2.5.1" % "test",
  "org.scalatra"        %% "scalatra-json"     % "2.5.1",
  "org.json4s"          %% "json4s-jackson"    % "3.5.0",
  "io.github.gitbucket" %% "scalatra-forms"    % "1.1.0",
  "io.github.gitbucket"             %  "solidbase"                    % "1.0.2",
  "com.github.takezoe"              %% "blocking-slick-32"            % "0.0.10",
  "com.h2database"                  %  "h2"                           % "1.4.195",
  "org.mariadb.jdbc"                %  "mariadb-java-client"          % "2.0.3",
  "org.postgresql"                  %  "postgresql"                   % "42.0.0",
  "com.zaxxer"                      %  "HikariCP"                     % "2.6.1",
  "ch.qos.logback" % "logback-classic" % "1.1.5" % "runtime",
  "org.codehaus.janino" % "janino" % "3.0.6" % "runtime",
  "org.apache.directory.server" % "apacheds-all" % "2.0.0-M24",
  "commons-io" % "commons-io" % "2.5",
  "commons-codec" % "commons-codec" % "1.10",
  "org.webjars" % "webjars-locator" % "0.32-1",
  "org.webjars.npm" % "bulma" % "0.5.1",
  "org.webjars" % "font-awesome" % "4.7.0",
  "org.webjars.npm" % "jquery" % "3.2.1",
  "org.webjars.npm" % "jquery-confirm" % "3.3.2",
  "org.webjars" % "tablesorter" % "2.25.4",
  "org.webjars.npm" % "mustache" % "2.3.0",
  "org.webjars.npm" % "bulmaswatch" % "0.4.1"
)

javacOptions in compile ++= Seq("-target", "8", "-source", "8")

testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "junitxml", "console")

packageOptions += Package.MainClass("JettyLauncher")

//ScalatraPlugin.scalatraWithJRebel

enablePlugins(SbtTwirl, JettyPlugin, SbtWeb)

containerPort := 10080

artifactName := { (v: ScalaVersion, m: ModuleID, a: Artifact) =>
  a.name + "." + a.extension
}

import org.irundaia.sbt.sass._

SassKeys.assetRootURL := "/main/assets"
SassKeys.cssStyle := Maxified

import com.typesafe.sbt.web.Import.WebKeys.webTarget
//webTarget = target.value / "web"
resourceManaged in SassKeys.sassify in Assets := target.value / "webapp" /  "assets" / "sass"

// Assembly settings
test in assembly := {}
assemblyMergeStrategy in assembly := {
  case PathList("i18n", "messages.properties") => MergeStrategy.concat
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

// Packaging options
packageOptions += Package.MainClass("JettyLauncher")

// Create executable war file
val ExecutableConfig = config("executable").hide
Keys.ivyConfigurations += ExecutableConfig
libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-security"     % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-webapp"       % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-continuation" % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-server"       % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-xml"          % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-http"         % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-servlet"      % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-io"           % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-util"         % JettyVersion % "executable"
)

val executableKey = TaskKey[File]("executable")
executableKey := {
  import java.util.jar.Attributes.{ Name => AttrName }
  import java.util.jar.{ Manifest => JarManifest }

  val workDir   = Keys.target.value / "executable"
  val warName   = Keys.name.value + ".war"

  val log       = streams.value.log
  log info s"building executable webapp in ${workDir}"

  // initialize temp directory
  val temp  = workDir / "webapp"
  IO delete temp

  // include jetty classes
  val jettyJars = Keys.update.value select configurationFilter(name = ExecutableConfig.name)
  jettyJars foreach { jar =>
    IO unzip (jar, temp, (name:String) =>
      (name startsWith "javax/") ||
        (name startsWith "org/")
    )
  }

  // include original war file
  val warFile   = (Keys.`package`).value
  IO unzip (warFile, temp)

  // include launcher classes
  val classDir      = (Keys.classDirectory in Compile).value
  val launchClasses = Seq("JettyLauncher.class" /*, "HttpsSupportConnector.class" */)
  launchClasses foreach { name =>
    IO copyFile (classDir / name, temp / name)
  }

  // zip it up
  IO delete (temp / "META-INF" / "MANIFEST.MF")
  val contentMappings   = (temp.allPaths --- PathFinder(temp)).get pair { file => IO.relativizeFile(temp, file) }
  val manifest          = new JarManifest
  manifest.getMainAttributes put (AttrName.MANIFEST_VERSION, "1.0")
  manifest.getMainAttributes put (AttrName.MAIN_CLASS,       "JettyLauncher")
  val outputFile    = workDir / warName
  IO jar (contentMappings.map { case (file, path) => (file, path.toString) } , outputFile, manifest)

  // done
  log info s"built executable webapp ${outputFile}"
  outputFile
}
