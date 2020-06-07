package gamemode

import exceptions.NoSuchModeException

import scala.language.implicitConversions

sealed trait GameMode

case object StandardMode extends GameMode {
  override def toString: String = "Standard"
}

case object CaptureTheFlagMode extends GameMode {
  override def toString: String = "Capture the Flag"
}

object GameMode {

  val gameModes: List[GameMode] = List(
    StandardMode,
    CaptureTheFlagMode
  )

  val scoreboards: Map[GameMode, String] = Map(
    StandardMode -> "scoreboard.html",
    CaptureTheFlagMode -> "scoreboardCaptureTheFlag.html"
  )

  implicit def fromString(mode: String): GameMode = gameModes.find(_.toString == mode) match {
    case Some(gameMode) => gameMode
    case None           => throw new NoSuchModeException(s"Game mode `$mode` does not exist.")
  }

}
