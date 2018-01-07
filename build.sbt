import scala.util.matching.Regex

name := "PentagonBullets"

val copyGamePlaying = taskKey[File]("Return compiled gamePlaying file.")
val copyGameMenus = taskKey[File]("Return compiled gameMenus file.")
val copyMain = taskKey[File]("Return main file.")
val copyServer = taskKey[File]("Return server file.")
val copyTooltip = taskKey[File]("Return tooltip file.")
lazy val fastOptCompileCopy = taskKey[Unit]("Compile and copy paste all the projects in the right directories.")

val copyGamePlayingFullOpt = taskKey[File]("Return full optimized compiled gamePlaying file")
val copyGameMenusFullOpt = taskKey[File]("Return full optimized compiled gameMenus file")
val copyMainFullOpt = taskKey[File]("Return full optimized compiled main file")
val copyServerFullOpt = taskKey[File]("Return full optimized compiled server file")
val copyTooltipFullOpt = taskKey[File]("Return full optimized compiled tooltip file")
lazy val fullOptCompileCopy = taskKey[Unit]("FullCompile and copy paste all the projects in the relevant directories")

val commonSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.1",
  scalacOptions ++= Seq("-deprecation", "-feature", "-encoding", "utf-8")
)



def fileNameWithoutPath(fileName: String): String = """[a-z\.-]+$""".r.findFirstIn(fileName).get

fastOptCompileCopy := {

  val gamePlayingDirectory = (copyGamePlaying in `gamePlaying`).value
  IO.delete(baseDirectory.value / "electron/gameplaying/js-files")
  IO.copyFile(
    gamePlayingDirectory,
    baseDirectory.value / ("electron/gameplaying/js-files/" + fileNameWithoutPath(gamePlayingDirectory.toString))
  )

  val gameMenusDirectory = (copyGameMenus in `gameMenus`).value
  IO.delete(baseDirectory.value / "electron/gamemenus/js-files")
  IO.copyFile(
    gameMenusDirectory,
    baseDirectory.value / ("electron/gamemenus/js-files/" + fileNameWithoutPath(gameMenusDirectory.toString))
  )

  val serverDirectory = (copyServer in `server`).value
  IO.delete(baseDirectory.value / "electron/server/js-files")
  IO.copyFile(
    serverDirectory,
    baseDirectory.value / ("electron/server/js-files/" + fileNameWithoutPath(serverDirectory.toString))
  )


  val tooltipDirectory = (copyTooltip in `tooltip`).value
  IO.delete(baseDirectory.value / "electron/tooltip/js")
  IO.copyFile(
    tooltipDirectory,
    baseDirectory.value / ("electron/tooltip/js/" + fileNameWithoutPath(tooltipDirectory.toString))
  )


  val mainProcessDirectory = (copyMain in `main`).value
  IO.delete(baseDirectory.value / "electron/mainprocess")
  IO.copyFile(
    mainProcessDirectory,
    baseDirectory.value / ("electron/mainprocess/" + fileNameWithoutPath(mainProcessDirectory.toString))
  )

  val insertCodeRegex = new Regex("//##")

  def makeHtmlFile(sourceFile: String, targetFile: String, javascript: String): Unit = {
    IO.writeLines(
      baseDirectory.value / targetFile,
      IO.readLines(baseDirectory.value / sourceFile).map(insertCodeRegex.replaceAllIn(_, javascript))
    )
  }

  makeHtmlFile(
    "source-html/index.html",
    "electron/gamemenus/mainscreen/index.html",
    "require(\"../js-files/gamemenus-fastopt.js\")"
  )

  makeHtmlFile(
    "source-html/scoreboard.html",
    "electron/gameplaying/gameplaying/scoreboard.html",
    "require(\"../js-files/gameplaying-fastopt.js\")"
  )

  makeHtmlFile(
    "source-html/scoreboardCaptureTheFlag.html",
    "electron/gameplaying/gameplaying/scoreboardCaptureTheFlag.html",
    "require(\"../js-files/gameplaying-fastopt.js\")"
  )

  makeHtmlFile(
    "source-html/gameplayinginterface.html",
    "electron/gameplaying/gameplaying/gameplayinginterface.html",
    "require(\"../js-files/gameplaying-fastopt.js\")"
  )

  makeHtmlFile(
    "source-html/createserver.html",
    "electron/gamemenus/createserver/createserver.html",
    "require(\"../js-files/gamemenus-fastopt.js\")"
  )

  makeHtmlFile(
    "source-html/package.json",
    "electron/package.json",
    "\"./mainprocess/main-fastopt.js\""
  )

  makeHtmlFile(
    "source-html/tooltip.html",
    "electron/tooltip/html/tooltip.html",
    "require(\"../js/tooltip-fastopt.js\")"
  )

  println("[info] Files copied to relevant directories")
}

fullOptCompileCopy := {
  val gamePlayingDirectory = (copyGamePlayingFullOpt in `gamePlaying`).value
  IO.delete(baseDirectory.value / "electron/gameplaying/js-files")
  IO.copyFile(
    gamePlayingDirectory,
    baseDirectory.value / ("electron/gameplaying/js-files/" + fileNameWithoutPath(gamePlayingDirectory.toString))
  )

  val gameMenusDirectory = (copyGameMenusFullOpt in `gameMenus`).value
  IO.delete(baseDirectory.value / "electron/gamemenus/js-files")
  IO.copyFile(
    gameMenusDirectory,
    baseDirectory.value / ("electron/gamemenus/js-files/" + fileNameWithoutPath(gameMenusDirectory.toString))
  )

  val serverDirectory = (copyServerFullOpt in `server`).value
  IO.delete(baseDirectory.value / "electron/server/js-files")
  IO.copyFile(
    serverDirectory,
    baseDirectory.value / ("electron/server/js-files/" + fileNameWithoutPath(serverDirectory.toString))
  )

  val tooltipDirectory = (copyTooltipFullOpt in `tooltip`).value
  IO.delete(baseDirectory.value / "electron/tooltip/js")
  IO.copyFile(
    tooltipDirectory,
    baseDirectory.value / ("electron/tooltip/js/" + fileNameWithoutPath(tooltipDirectory.toString))
  )

  val mainProcessDirectory = (copyMainFullOpt in `main`).value
  IO.delete(baseDirectory.value / "electron/mainprocess")
  IO.copyFile(
    mainProcessDirectory,
    baseDirectory.value / ("electron/mainprocess/" + fileNameWithoutPath(mainProcessDirectory.toString))
  )

  val insertCodeRegex = new Regex("//##")

  def makeHtmlFile(sourceFile: String, targetFile: String, javascript: String): Unit = {
    IO.writeLines(
      baseDirectory.value / targetFile,
      IO.readLines(baseDirectory.value / sourceFile).map(insertCodeRegex.replaceAllIn(_, javascript))
    )
  }

  makeHtmlFile(
    "source-html/index.html",
    "electron/gamemenus/mainscreen/index.html",
    "require(\"../js-files/gamemenus-opt.js\")"
  )

  makeHtmlFile(
    "source-html/scoreboard.html",
    "electron/gameplaying/gameplaying/scoreboard.html",
    "require(\"../js-files/gameplaying-opt.js\")"
  )

  makeHtmlFile(
    "source-html/scoreboardCaptureTheFlag.html",
    "electron/gameplaying/gameplaying/scoreboardCaptureTheFlag.html",
    "require(\"../js-files/gameplaying-opt.js\")"
  )

  makeHtmlFile(
    "source-html/gameplayinginterface.html",
    "electron/gameplaying/gameplaying/gameplayinginterface.html",
    "require(\"../js-files/gameplaying-opt.js\")"
  )

  makeHtmlFile(
    "source-html/createserver.html",
    "electron/gamemenus/createserver/createserver.html",
    "require(\"../js-files/gamemenus-opt.js\")"
  )

  makeHtmlFile(
    "source-html/package.json",
    "electron/package.json",
    "\"./mainprocess/main-opt.js\""
  )

  makeHtmlFile(
    "source-html/tooltip.html",
    "electron/tooltip/html/tooltip.html",
    "require(\"../js/tooltip-opt.js\")"
  )

  println("[info] Files copied to relevant directories")
}


lazy val `facades` = project.in(file("./facades"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3"
    )
  )


lazy val `messages` = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure)
  .in(file("./messages"))
  .settings(commonSettings)
  .jsConfigure(_.enablePlugins(JSDependenciesPlugin))
  .settings(
    name := "messages",
    libraryDependencies ++= Seq(
      "io.suzaku" %%% "boopickle" % "1.2.7-SNAPSHOT"
    )
  )

lazy val messagesJS = messages.js.settings(name := "messagesJS")
  .dependsOn(facades)

lazy val messagesJVM = messages.jvm.settings(name := "messagesJVM")

lazy val `shared` = project.in(file("shared"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3",
      "io.suzaku" %%% "boopickle" % "1.2.7-SNAPSHOT",
      "be.adoeraene" %%% "pixi-scalajs-gui" % "0.1.0-SNAPSHOT"
    ),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
  .dependsOn(gameLogicJS)
  .dependsOn(facades)
  .dependsOn(ui)

lazy val `tooltip` = project.in(file("tooltip"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3",
      "io.suzaku" %%% "boopickle" % "1.2.7-SNAPSHOT"
    ),
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    copyTooltip := {
      (fastOptJS in Compile).value.data
    },
    copyTooltipFullOpt := {
      (fullOptJS in Compile).value.data
    }
  )
  .dependsOn(shared)

lazy val `gamePlaying` = project.in(file("gameplaying"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3",
      "io.suzaku" %%% "boopickle" % "1.2.7-SNAPSHOT",
      "be.adoeraene" %%% "pixi-scalajs-gui" % "0.1.0-SNAPSHOT"
    ),
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    copyGamePlaying := {
      (fastOptJS in Compile).value.data
    },
    copyGamePlayingFullOpt := {
      (fullOptJS in Compile).value.data
    }
  )
  .dependsOn(`shared`)


lazy val `gameMenus` = project.in(file("gamemenus"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3",
      "io.suzaku" %%% "boopickle" % "1.2.7-SNAPSHOT",
      "be.adoeraene" %%% "pixi-scalajs-gui" % "0.1.0-SNAPSHOT"
    ),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    scalaJSUseMainModuleInitializer := true,
    copyGameMenus := {
      (fastOptJS in Compile).value.data
    },
    copyGameMenusFullOpt := {
      (fullOptJS in Compile).value.data
    }
  )
  .dependsOn(`shared`)
  .dependsOn(`server`)


lazy val `main` = project.in(file("main"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3",
      "io.suzaku" %%% "boopickle" % "1.2.7-SNAPSHOT",
      "be.adoeraene" %%% "pixi-scalajs-gui" % "0.1.0-SNAPSHOT"
    ),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    scalaJSUseMainModuleInitializer := true,
    copyMain := {
      (fastOptJS in Compile).value.data
    },
    copyMainFullOpt := {
      (fullOptJS in Compile).value.data
    }
  )
  .dependsOn(`shared`)


lazy val `server` = project.in(file("server"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3",
      "io.suzaku" %%% "boopickle" % "1.2.7-SNAPSHOT",
      "be.adoeraene" %%% "pixi-scalajs-gui" % "0.1.0-SNAPSHOT"
    ),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    scalaJSUseMainModuleInitializer := true,
    copyServer := {
      (fastOptJS in Compile).value.data
    },
    copyServerFullOpt := {
      (fullOptJS in Compile).value.data
    }
  )
  .dependsOn(`shared`)


lazy val `gameLogic` = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure).in(file("./gamelogic"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3",
      "io.suzaku" %%% "boopickle" % "1.2.7-SNAPSHOT",
      "be.adoeraene" %%% "pixi-scalajs-gui" % "0.1.0-SNAPSHOT"
    )
  )
.dependsOn(messages)

lazy val gameLogicJS = gameLogic.js.settings(name := "sharedJS")
lazy val gameLogicJVM = gameLogic.jvm.settings(name := "sharedJVM")


lazy val `ui` = project.in(file("ui"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3"
    )
  )
