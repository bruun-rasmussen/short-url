import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "short-url"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      javaCore, javaJdbc, javaEbean,
      // Add your project dependencies here,
      "mysql"            % "mysql-connector-java" % "5.1.18",
   // "org.apache.derby" % "derby"                % "10.9.1.0",
      "com.google.zxing" % "core"                 % "2.1",
      "com.google.zxing" % "javase"               % "2.1"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here
    )

}
