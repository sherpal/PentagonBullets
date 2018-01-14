package renderer

import communication.PlayerClient
import custommath.Complex
import entities.Player
import exceptions.StorageDecodingError
import globalvariables._
import globalvariables.VariableStorage.retrieveValue
import networkcom.PlayerGameSettingsInfo
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.MouseEvent
import plots.{Plot, PlotElement}
import plots.plotelements.Line
import scoreboardui.UIPages
import sharednodejsapis.BrowserWindow
import ui._
import webglgraphics.Vec3

import scala.scalajs.js
import scala.scalajs.js.timers.setTimeout




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

              final case class LifeOverTime(playerName: String, color: Vec3, lives: List[(Double, Double)])

              final class LifePlot(
                                    width: Int, height: Int,
                                    livesInfo: Map[PlotElement, LifeOverTime]
                                  ) extends Plot {
                override def onMouseMoveCanvasCoords(x: Double, y: Double): Unit = {

                  val child = closestChildToCanvasCoords(x, y)
                  livesInfo.get(child) match {
                    case Some(info) =>
                      val closestPoint = child.closestPointToCanvasCoords(Complex(x, y), this)
                      val closestPointPlotCoords = canvasToPlotCoordinates(closestPoint.re, closestPoint.im)

                      val timeLifeInfo = info.lives.minBy(elem => math.abs(elem._1 - closestPointPlotCoords._1))

                      val seconds = timeLifeInfo._1 / 1000

                      clear()
                      drawAxes()
                      drawChildren()

                      if ((Complex(x, y) - closestPoint).modulus2 < 60) {
                        drawPoint(closestPoint.re, closestPoint.im, 5, info.color)
                        write(
                          s"${info.playerName}",
                          5, height - 40
                        )
                        write(
                          s"Time: ${seconds}s, Health: ${timeLifeInfo._2}",
                          5, height - 20
                        )
                      }

                    case None =>
                  }
                }

                setSize(width, height)

                setYAxis(-5, Player.maxLife + 5)
                setXAxis(0, livesInfo.values.map(_.lives.head._1).max)
                livesInfo.keys.foreach(addChild)

                dom.document.body.appendChild(canvasElement)
                canvasElement.style.border = "1px solid black"
                setBackgroundColor(1,1,1)
                clear()

                setTimeout(500) {
                  setBackgroundColor(1,1,1)
                  clear()
                  drawAxes()
                  drawChildren()
                }


              }

              val livesOverTime = stats.map(stat => LifeOverTime(
                stat.playerName,
                Vec3(stat.color.red, stat.color.green, stat.color.blue),
                stat.lifeOverTime.map(tLS => ((tLS.time - startTime).toDouble, tLS.life))
              ))
                .filter(_.lives.nonEmpty)

              if (livesOverTime.nonEmpty) {

                val livesInfo: Map[PlotElement, LifeOverTime] =
                  livesOverTime.map(lifeOverTime => {
                    val (xs, ys) = lifeOverTime.lives.toVector.unzip
                    val line = new Line(xs, ys, lifeOverTime.color)
                    line -> lifeOverTime
                  })
                  .toMap

                new LifePlot(1000, 300, livesInfo)
              }


              UIPages.playersStat.quitButton.onclick = (_: MouseEvent) => {
                dom.window.location.href = "../../gamemenus/mainscreen/index.html"
              }

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
