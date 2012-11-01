import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "akka_whiteboard"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "com.typesafe.akka" % "akka-testkit" % "2.0.3" % "test"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
