import scala.util.matching.Regex

name := "PentagonBullets"

val copyGamePlaying = taskKey[File]("Return compiled gamePlaying file.")
val copyGameMenus = taskKey[File]("Return compiled gameMenus file.")
val copyTableServerMenus = taskKey[File]("Return compiled menusfortableserver file")
val copyMain = taskKey[File]("Return main file.")
val copyServer = taskKey[File]("Return server file.")
val copyOneTimeServer = taskKey[File]("Return one time server file")
val copyTooltip = taskKey[File]("Return tooltip file.")
val copyReplay = taskKey[File]("Return replay file.")
val compileTableServer = taskKey[File]("Compile table server.")
lazy val fastOptCompileCopy = taskKey[Unit]("Compile and copy paste all the projects in the right directories.")

val copyGamePlayingFullOpt = taskKey[File]("Return full optimized compiled gamePlaying file")
val copyGameMenusFullOpt = taskKey[File]("Return full optimized compiled gameMenus file")
val copyTableServerMenusFullOpt = taskKey[File]("Return compiled menusfortableserver full opt file")
val copyMainFullOpt = taskKey[File]("Return full optimized compiled main file")
val copyServerFullOpt = taskKey[File]("Return full optimized compiled server file")
val copyOneTimeServerFullOpt = taskKey[File]("Return full optimized compiled one time server file")
val copyTooltipFullOpt = taskKey[File]("Return full optimized compiled tooltip file")
val copyReplayFullOpt = taskKey[File]("Return full optimized compiled replay file")
val compileTableServerFullOpt = taskKey[File]("Compile table server.")
lazy val fullOptCompileCopy = taskKey[Unit]("FullCompile and copy paste all the projects in the relevant directories")

val commonSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.1",
  scalacOptions ++= Seq("-deprecation", "-feature", "-encoding", "utf-8")
)



def fileNameWithoutPath(fileName: String): String = """[a-z\.-]+$""".r.findFirstIn(fileName).get

fastOptCompileCopy := {

  (compileTableServer in `tableServer`).value
  println("[info] table server is compiled.")

  val gamePlayingDirectory = (copyGamePlaying in `gamePlaying`).value
  IO.delete(baseDirectory.value / "electron/gameplaying/js-files")
  IO.copyFile(
    gamePlayingDirectory,
    baseDirectory.value / ("electron/gameplaying/js-files/" + fileNameWithoutPath(gamePlayingDirectory.toString))
  )

  /**
   * Replay window copy and paste
   */
  val replayDirectory = (copyReplay in `replay`).value
  IO.copyFile(
    replayDirectory,
    baseDirectory.value / ("electron/gameplaying/js-files/" + fileNameWithoutPath(replayDirectory.toString))
  )



  val gameMenusDirectory = (copyGameMenus in `gameMenus`).value
  IO.delete(baseDirectory.value / "electron/gamemenus/js-files")
  IO.copyFile(
    gameMenusDirectory,
    baseDirectory.value / ("electron/gamemenus/js-files/" + fileNameWithoutPath(gameMenusDirectory.toString))
  )

  val tableMenusDirectory = (copyTableServerMenus in `menusForTableServer`).value
  IO.delete(baseDirectory.value / "electron/table-menus/js-files")
  IO.copyFile(
    tableMenusDirectory,
    baseDirectory.value / ("electron/table-menus/js-files/" + fileNameWithoutPath(tableMenusDirectory.toString))
  )


  val serverDirectory = (copyServer in `server`).value
  IO.delete(baseDirectory.value / "electron/server/js-files")
  IO.copyFile(
    serverDirectory,
    baseDirectory.value / ("electron/server/js-files/" + fileNameWithoutPath(serverDirectory.toString))
  )

  val oneTimeServerDirectory = (copyOneTimeServer in `oneTimeServer`).value
  IO.delete(baseDirectory.value / "electron/one-time-server/js-files")
  IO.copyFile(
    oneTimeServerDirectory,
    baseDirectory.value /
      ("electron/one-time-server/js-files/" + fileNameWithoutPath(oneTimeServerDirectory.toString))
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
    "source-html/table-menus.html",
    "electron/table-menus/html/table-menus.html",
    "require(\"../js-files/menusfortableserver-fastopt.js\")"
  )

  makeHtmlFile(
    "source-html/scoreboard.html",
    "electron/gameplaying/gameplaying/scoreboard.html",
    "require(\"../js-files/gameplaying-fastopt.js\")"
  )

  makeHtmlFile(
    "source-html/replay.html",
    "electron/gameplaying/gameplaying/replay.html",
    "require(\"../js-files/replay-fastopt.js\")"
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

  makeHtmlFile(
    "source-html/server.html",
    "electron/one-time-server/html/server.html",
    "require(\"../js-files/onetimeserver-fastopt.js\")"
  )

  println("[info] Files copied to relevant directories")
}

fullOptCompileCopy := {

  (compileTableServerFullOpt in `tableServer`).value
  println("[info] table server is compiled.")


  val gamePlayingDirectory = (copyGamePlayingFullOpt in `gamePlaying`).value
  IO.delete(baseDirectory.value / "electron/gameplaying/js-files")
  IO.copyFile(
    gamePlayingDirectory,
    baseDirectory.value / ("electron/gameplaying/js-files/" + fileNameWithoutPath(gamePlayingDirectory.toString))
  )

  /** Replay */
  val replayDirectory = (copyReplayFullOpt in `replay`).value
  IO.copyFile(
    replayDirectory,
    baseDirectory.value / ("electron/gameplaying/js-files/" + fileNameWithoutPath(replayDirectory.toString))
  )

  val gameMenusDirectory = (copyGameMenusFullOpt in `gameMenus`).value
  IO.delete(baseDirectory.value / "electron/gamemenus/js-files")
  IO.copyFile(
    gameMenusDirectory,
    baseDirectory.value / ("electron/gamemenus/js-files/" + fileNameWithoutPath(gameMenusDirectory.toString))
  )

  val tableMenusDirectory = (copyTableServerMenusFullOpt in `menusForTableServer`).value
  IO.delete(baseDirectory.value / "electron/table-menus/js-files")
  IO.copyFile(
    tableMenusDirectory,
    baseDirectory.value / ("electron/table-menus/js-files/" + fileNameWithoutPath(tableMenusDirectory.toString))
  )


  val serverDirectory = (copyServerFullOpt in `server`).value
  IO.delete(baseDirectory.value / "electron/server/js-files")
  IO.copyFile(
    serverDirectory,
    baseDirectory.value / ("electron/server/js-files/" + fileNameWithoutPath(serverDirectory.toString))
  )

  val oneTimeServerDirectory = (copyOneTimeServerFullOpt in `oneTimeServer`).value
  IO.delete(baseDirectory.value / "electron/one-time-server/js-files")
  IO.copyFile(
    oneTimeServerDirectory,
    baseDirectory.value /
      ("electron/one-time-server/js-files/" + fileNameWithoutPath(oneTimeServerDirectory.toString))
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
    "source-html/table-menus.html",
    "electron/table-menus/html/table-menus.html",
    "require(\"../js-files/menusfortableserver-opt.js\")"
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
    "source-html/replay.html",
    "electron/gameplaying/gameplaying/replay.html",
    "require(\"../js-files/replay-opt.js\")"
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

  makeHtmlFile(
    "source-html/server.html",
    "electron/one-time-server/html/server.html",
    "require(\"../js-files/onetimeserver-opt.js\")"
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

lazy val `replay` = project.in(file("replay"))
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
    copyReplay := {
      (fastOptJS in Compile).value.data
    },
    copyReplayFullOpt := {
      (fullOptJS in Compile).value.data
    }
  )
  .dependsOn(`gamePlaying`)

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

lazy val `menusForTableServer` = project.in(file("./menus-for-table-server"))
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
    copyTableServerMenus := {
      (fastOptJS in Compile).value.data
    },
    copyTableServerMenusFullOpt := {
      (fullOptJS in Compile).value.data
    }
  )
  .dependsOn(`shared`)
  .dependsOn(`messagesJS`)

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


lazy val `serverLogic` = project.in(file("server-logic"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3",
      "io.suzaku" %%% "boopickle" % "1.2.7-SNAPSHOT"
    ),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
.dependsOn(`gameLogicJS`)
.dependsOn(`messagesJS`)

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
  .dependsOn(`serverLogic`)
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


/**
 * This project will be a node.js module that keeps track of all the tables currently occupied by people.
 *
 * A table is a (virtual) place where people gather before starting a game.
 * The module should be put on a server accessible for every body.
 */
lazy val `tableServer` = project.in(file("tableserver"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3"
    ),
    compileTableServer := {
      (fastOptJS in Compile).value.data
    },
    compileTableServerFullOpt := {
      (fullOptJS in Compile).value.data
    }
  )
  .dependsOn(messagesJS)
  .dependsOn(gameLogicJS)


/**
 * This is a server created for one game.
 * An invisible browser window will pop, open a server, and will quit once the game is over.
 */
lazy val `oneTimeServer` = project.in(file("onetimeserver"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3"
    ),
    copyOneTimeServer := {
      (fastOptJS in Compile).value.data
    },
    copyOneTimeServerFullOpt := {
      (fullOptJS in Compile).value.data
    }
  )
  .dependsOn(`serverLogic`)
  .dependsOn(shared)

