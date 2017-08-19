import org.scalatra.sbt._
import org.scalatra.sbt.DistPlugin._
import org.scalatra.sbt.DistPlugin.DistKeys._

organization := "io.github.kounoike"

name := "ldapanda"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.1"

scalacOptions := Seq("-deprecation", "-language:postfixOps")

libraryDependencies ++= Seq(
  "org.eclipse.jetty"    % "jetty-webapp"      % "9.3.9.v20160517" % "provided",
  "javax.servlet"        % "javax.servlet-api" % "3.1.0" % "provided",
  "org.scalatra"        %% "scalatra"          % "2.5.1",
  "org.scalatra"        %% "scalatra-specs2"   % "2.5.1" % "test",
  "org.scalatra"        %% "scalatra-json"     % "2.5.1",
  "org.json4s"          %% "json4s-jackson"    % "3.5.0",
  "io.github.gitbucket" %% "scalatra-forms"    % "1.1.0",
  "org.apache.directory.server" % "apacheds-all" % "2.0.0-M24",
  "commons-io" % "commons-io" % "2.5",
  "commons-codec" % "commons-codec" % "1.10",
  "org.webjars" % "webjars-locator" % "0.32-1",
  "org.webjars" % "bootstrap" % "3.3.7-1",
  "org.webjars" % "jquery" % "1.11.1"
)

javacOptions in compile ++= Seq("-target", "8", "-source", "8")

testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "junitxml", "console")

packageOptions += Package.MainClass("JettyLauncher")

//ScalatraPlugin.scalatraWithJRebel

enablePlugins(SbtTwirl, JettyPlugin)

containerPort := 10080

val myDistSettings = DistPlugin.distSettings ++ Seq(
  mainClass in Dist := Some("ScalatraLauncher"),
  memSetting in Dist := "2g",
  permGenSetting in Dist := "256m",
  envExports in Dist := Seq("LC_CTYPE=en_US.UTF-8", "LC_ALL=en_US.utf-8"),
  javaOptions in Dist ++= Seq("-Xss4m", "-Dfile.encoding=UTF-8")
)

artifactName := { (v: ScalaVersion, m: ModuleID, a: Artifact) =>
  a.name + "." + a.extension
}