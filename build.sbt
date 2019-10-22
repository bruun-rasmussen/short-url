name := "short-url"
organization := "dk.br"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.0"

libraryDependencies += guice
libraryDependencies += ehcache

libraryDependencies += "mysql"            % "mysql-connector-java" % "5.1.18"
libraryDependencies += "com.google.zxing" % "core"                 % "2.1"
libraryDependencies += "com.google.zxing" % "javase"               % "2.1"

libraryDependencies += "commons-lang"     % "commons-lang"         % "2.4"
libraryDependencies += "org.yaml"         % "snakeyaml"            % "1.17"
