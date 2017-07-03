name := "shopping-basket"

version := "1.0"

scalaVersion := "2.12.2"

lazy val akkaVersion = "2.5.2"
lazy val akkaHttpVersion = "10.0.7"

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" %% "akka-actor"           % akkaVersion,
    "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
    "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.3",
    "com.iheart"        %% "ficus"                % "1.4.0",
    "net.liftweb"       %% "lift-json"            % "3.0.1",
    "com.typesafe.akka" %% "akka-testkit"         % akkaVersion % "test",
    "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion,
    "org.scalatest"     %% "scalatest"            % "3.0.1"  % "test"
  )
}
