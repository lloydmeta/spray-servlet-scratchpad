import sbt._

object SprayServletScratchpadBuild extends Build {

  lazy val root = Project("root", file(".")) dependsOn(spraySwaggerProject)
  lazy val spraySwaggerProject = RootProject(uri("git://github.com/gettyimages/spray-swagger.git"))

}