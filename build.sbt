organization  := "com.beachape"

version       := "0.1"

scalaVersion  := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/"
)

libraryDependencies ++= {
  val akkaV = "2.3.0"
  val sprayV = "1.3.0"
  Seq(
    "io.spray"            %   "spray-servlet" % sprayV,
    "io.spray" %%  "spray-json" % "1.2.5",
    "io.spray"            %   "spray-routing" % sprayV,
    "io.spray"            %   "spray-client" % sprayV,
    "io.spray"            %   "spray-testkit" % sprayV % "test",
    "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "container",
    "org.eclipse.jetty" % "jetty-plus"   % "9.1.0.v20131115" % "container",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container"  artifacts Artifact("javax.servlet", "jar", "jar"),
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV % "test",
    "com.wordnik" %% "swagger-core" % "1.3.0",
    "org.scalatest" %% "scalatest" % "2.1.0" % "test",
    "com.beachape.metascraper" %% "metascraper" % "0.2.5"
  )
}

seq(webSettings: _*)
