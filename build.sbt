name := "shopping-basket"

version := "1.0"

scalaVersion := "2.12.4"

lazy val akkaVersion = "2.5.9"
lazy val akkaHttpVersion = "10.0.11"

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" %% "akka-actor"           % akkaVersion,
    "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
    "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.11",
    "com.iheart"        %% "ficus"                % "1.4.3",
    "net.liftweb"       %% "lift-json"            % "3.2.0",
    "com.typesafe.akka" %% "akka-testkit"         % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion,
    "org.scalatest"     %% "scalatest"            % "3.0.5"  % Test
  )
}
