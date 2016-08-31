name := "historical-data"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "2.4.2",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "org.scalikejdbc" %% "scalikejdbc-config" % "2.4.2",
  "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % "2.4.2"
)