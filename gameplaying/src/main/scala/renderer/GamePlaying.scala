package renderer

import communication.PlayerClient
import exceptions.StorageDecodingError
import globalvariables._
import globalvariables.VariableStorage.retrieveValue
import networkcom.PlayerGameSettingsInfo
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.MouseEvent
import scoreboardui.UIPages
import sharednodejsapis.BrowserWindow
import ui._

import scala.scalajs.js




/**
 * GamePlaying JSApp that happens when playing the game or the scores afterwards.
 *
 * Optimization possibilities:
 * - Mist drawing in EntityDrawer: make something that will not make the client draw a huge area.
 *
 *
 */
object GamePlaying {
  def main(args: Array[String]): Unit = {

    dom.document.title match {
      case "Pentagon Bullets" =>
        DataStorage.retrieveValue("gameData") match {
          case GameData(gameName, playerName, _, address, port, _, gameMode) =>
            val password: Int = retrieveValue("password").asInstanceOf[Int]

            val playersData: List[PlayerGameSettingsInfo] = (DataStorage.retrieveValue("playerGameSettings") match {
              case data: PlayerDataList =>
                data.data
              case _ =>
                throw new StorageDecodingError
            }).map(data => PlayerGameSettingsInfo.fromInfo(
              data.playerName, data.ready, data.team, data.id, data.abilities
            ))


            new PlayerClient(playerName, gameName, address, port, password, playersData, gameMode)

          case _ =>
            dom.window.alert("Fatal error: game data where not saved correctly.")
            dom.window.location.href = "../../gamemenus/mainscreen/index.html"

        }


      case "Score Board" =>

        // TODO: change all of this when Justin finishes it.

        try {


          DataStorage.retrieveValue("endOfGameData") match {
            case StandardModeEOGData(_) =>

              val (playerName, gameDuration, startTime, stats) = DataStorage.retrieveValue("statistics") match {
                case PlayerStats(pName, gD, ss, s) =>
                  (pName, gD, ss, s)
                case _ =>
                  throw new StorageDecodingError
              }

              UI.playerName.value = playerName

              UIPages.gameStat.quitButton.onclick = (_: dom.MouseEvent) => {
                UI.showConfirmBox(
                  "Quit Game",
                  "Are you sure you want to quit the wonderful Pentagon Bullets?",
                  (answer: Boolean) => {
                    if (answer) {
                      BrowserWindow.getFocusedWindow().close()
                    }
                  }
                )
              }

              UIStatManager.appendGame(new UIGame(
                js.Array[UIStatProperty](
                  new UIStatProperty("Duration", s"${gameDuration / 1000.0} s"),
                  new UIStatProperty("Winner", stats.head.playerName)
                )
              ))

              stats.zipWithIndex.foreach({ case (playerStat, idx) =>
                  UIStatManager.appendPlayer(new UIPlayer(
                    idx + 1, playerStat.playerName, js.Array[UIStatProperty](
                      new UIStatProperty("Ability", playerStat.ability),
                      playerStat.deathTime match {
                        case Some(deathTime) =>
                          new UIStatProperty("Alive during", s"${(deathTime - startTime) / 1000.0}s")
                        case None =>
                          new UIStatProperty("Alive", "")
                      },
                      {
                        val bulletNbr = playerStat.sentBullets.length
                        val lifeTime = playerStat.deathTime match {
                          case Some(deathTime) => deathTime - startTime
                          case None => gameDuration
                        }
                        new UIStatProperty(
                          "Fired Bullet Nbr",
                          s"$bulletNbr (${math.round(bulletNbr / (lifeTime / 1000.0) * 100) / 100.0} bullets/s)"
                        )
                      },
                      new UIStatProperty("Bullets hit players", playerStat.bulletHitPlayerNbr.toString),
                      new UIStatProperty(
                        "Efficiency",
                        s"${math.round(
                          playerStat.bulletHitPlayerNbr / playerStat.sentBullets.length.toDouble * 100.0 * 100.0) /
                          100.0}%"),
                      new UIStatProperty(
                        "First blood after",
                        if (playerStat.bulletHitsTimes.isEmpty) "Never"
                        else s"${(playerStat.bulletHitsTimes.min - startTime) / 1000.0} s"
                      ),
                      new UIStatProperty("Taken Damage", playerStat.damageTaken.toString),
                      new UIStatProperty("Taken Heal Units", playerStat.takenHealUnits.toString),
                      {
                        val burstWindow: Int = 5
                        val sentBulletTimesSorted = playerStat.sentBulletsTimes.sorted
                        val bestBurst = sentBulletTimesSorted.foldLeft((0, List[Long]()))({
                          case ((record, queuedTimes), time) =>
                            val newTimes = time +: queuedTimes.filter(time - _ <= burstWindow * 1000)
                            (math.max(record, newTimes.length), newTimes)
                        })._1
                        new UIStatProperty(
                          s"Best burst in ${burstWindow}s",
                          s"$bestBurst bullets (${bestBurst.toDouble / burstWindow} bullets/s)"
                        )
                      },
                      new UIStatProperty("Total movement", playerStat.totalMovement.toInt.toString)
                    )
                  ))
              })

              UIPages.playersStat.quitButton.onclick = (_: MouseEvent) => {
                dom.window.location.href = "../../gamemenus/mainscreen/index.html"
              }



//              val players: List[String] = playersFromLastToFirstDeath
//              val rows = players.zipWithIndex.map({case (name, position) =>
//                val tr = dom.document.createElement("tr").asInstanceOf[html.TableRow]
//                val positionTd = dom.document.createElement("td").asInstanceOf[html.TableCol]
//                positionTd.innerHTML = (position + 1).toString
//                val nameTd = dom.document.createElement("td").asInstanceOf[html.TableCol]
//                nameTd.innerHTML = name
//                tr.appendChild(positionTd)
//                tr.appendChild(nameTd)
//                tBody.appendChild(tr)
//
//                tr
//              })
//              rows.head.className = "success"
            case CaptureTheFlagModeEOGData(scores) =>
              val tBody = dom.document.getElementById("scoreBoard").asInstanceOf[html.TableDataCell]
              val rows = scores.toList.sortBy(_._2).reverse.map({case (teamNbr, points) =>
                val tr = dom.document.createElement("tr").asInstanceOf[html.TableRow]
                val positionTd = dom.document.createElement("td").asInstanceOf[html.TableCol]
                positionTd.innerHTML = points.toString
                val nameTd = dom.document.createElement("td").asInstanceOf[html.TableCol]
                nameTd.innerHTML = teamNbr.toString
                tr.appendChild(positionTd)
                tr.appendChild(nameTd)
                tBody.appendChild(tr)

                tr
              })
              rows.head.className = "success"

            case _ =>
              throw new StorageDecodingError
          }

        } catch {
          case e: Throwable =>
            e.printStackTrace()
            dom.window.alert("FATAL ERROR: player names were not saved! Sorry")
            //dom.window.location.href = "../../gamemenus/mainscreen/index.html"
        }
      case _ =>
        dom.window.alert("FATAL ERROR: Window title unknown!")
    }


  }

}
