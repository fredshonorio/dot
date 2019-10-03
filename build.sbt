import ScalacOptions._

lazy val buildSettings = Seq(
  organization  :=  "net.meshapp",
  version       :=  "0.0.1",
  scalaVersion  :=  "2.12.6",
  scalacOptions ++= compilerOptions,
)

lazy val catsEffect = "1.0.0"
lazy val Integration = config("integration").extend(Test)

def inIntegrationPackage(name: String): Boolean = name.startsWith("integration.")
def notInIntegrationPackage(name: String): Boolean = !inIntegrationPackage(name)

lazy val deps = Seq(
  "org.zeroturnaround" %  "zt-exec"      % "1.10",
  "org.typelevel"      %% "cats-effect"  % catsEffect,

  "org.scalatest" %% "scalatest"    % "3.0.5"     % "integration,test"
)

lazy val dot = (project in file("."))
  .configs(Integration)
  .settings(
    buildSettings,
    inConfig(Integration)(Defaults.testTasks),
    libraryDependencies ++= deps,
    testOptions in Test        := Seq(Tests.Filter(notInIntegrationPackage)),
    testOptions in Integration := Seq(Tests.Filter(inIntegrationPackage)),
  )
