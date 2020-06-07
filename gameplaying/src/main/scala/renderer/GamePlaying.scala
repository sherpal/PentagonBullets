package renderer

import communication.PlayerClient
import custommath.Complex
import debug.DebugPackage
import entities.Player
import exceptions.StorageDecodingError
import globalvariables._
import globalvariables.VariableStorage.retrieveValue
import networkcom.tablemessages.Hello
import networkcom.{Disconnect, Peer, PlayerGameSettingsInfo}
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.MouseEvent
import plots.{Plot, PlotElement}
import plots.plotelements.{Line, Segment}
import replay.ReplayWindow
import scoreboardui.UIPages
import sharednodejsapis.BrowserWindow
import ui._
import webglgraphics.{Vec3, Vec4}

import scala.scalajs.js
import scala.scalajs.js.timers.setTimeout
import scala.Ordering.Double.TotalOrdering

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

    if (scala.scalajs.LinkingInfo.developmentMode) {
      new DebugPackage("../gameplaying/scoreboard.html")
    }

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
            }).map(
              data =>
                PlayerGameSettingsInfo.fromInfo(
                  data.playerName,
                  data.ready,
                  data.team,
                  data.id,
                  data.abilities,
                  (data.color(0), data.color(1), data.color(2))
                )
            )

            println(address + ":" + port)

            val playerClient = new PlayerClient(playerName, gameName, address, port, password, playersData, gameMode)

            scala.util.Try(DataStorage.retrieveValue("tableServerPeer")) match {
              case scala.util.Failure(e) =>
                e.printStackTrace()
              case scala.util.Success(PeerData(a, p)) =>
                playerClient.sendReliableTo(Hello(gameName), Peer(a, p))
                setTimeout(5000) {
                  playerClient.sendReliableTo(Disconnect(), Peer(a, p))
                }
              case _ =>
            }

          case _ =>
            dom.window.alert("Fatal error: game data where not saved correctly.")
            dom.window.location.href = "../../gamemenus/mainscreen/index.html"

        }

      case "Score Board" =>
        dom.document.getElementById("replay").asInstanceOf[html.Button].onclick = (_: dom.MouseEvent) => {
          new ReplayWindow
        }

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

              UIStatManager.appendGame(
                new UIGame(
                  js.Array[UIStatProperty](
                    new UIStatProperty("Duration", s"${gameDuration / 1000.0} s"),
                    new UIStatProperty("Winner", stats.head.playerName)
                  )
                )
              )

              stats.zipWithIndex.foreach({
                case (playerStat, idx) =>
                  UIStatManager.appendPlayer(
                    new UIPlayer(
                      idx + 1,
                      playerStat.playerName,
                      js.Array[UIStatProperty](
                        new UIStatProperty("Ability", playerStat.ability),
                        playerStat.deathTime match {
                          case Some(deathTime) =>
                            new UIStatProperty("Alive during", s"${(deathTime - startTime) / 1000.0}s")
                          case None =>
                            new UIStatProperty("Alive", "")
                        }, {
                          val bulletNbr = playerStat.sentBullets.length
                          val lifeTime = playerStat.deathTime match {
                            case Some(deathTime) => deathTime - startTime
                            case None            => gameDuration
                          }
                          new UIStatProperty(
                            "Fired Bullet Nbr",
                            s"$bulletNbr (${math.round(bulletNbr / (lifeTime / 1000.0) * 100) / 100.0} bullets/s)"
                          )
                        },
                        new UIStatProperty("Bullets hit players", playerStat.bulletHitPlayerNbr.toString),
                        new UIStatProperty(
                          "Efficiency",
                          s"${math.round(playerStat.bulletHitPlayerNbr / playerStat.sentBullets.length.toDouble * 100.0 * 100.0) /
                            100.0}%"
                        ),
                        new UIStatProperty(
                          "First blood after",
                          if (playerStat.bulletHitsTimes.isEmpty) "Never"
                          else s"${(playerStat.bulletHitsTimes.min - startTime) / 1000.0} s"
                        ),
                        new UIStatProperty("Taken Damage", playerStat.damageTaken.toString),
                        new UIStatProperty("Taken Heal Units", playerStat.takenHealUnits.toString), {
                          val burstWindow: Int      = 5
                          val sentBulletTimesSorted = playerStat.sentBulletsTimes.sorted
                          val bestBurst = sentBulletTimesSorted
                            .foldLeft((0, List[Long]()))({
                              case ((record, queuedTimes), time) =>
                                val newTimes = time +: queuedTimes.filter(time - _ <= burstWindow * 1000)
                                (math.max(record, newTimes.length), newTimes)
                            })
                            ._1
                          new UIStatProperty(
                            s"Best burst in ${burstWindow}s",
                            s"$bestBurst bullets (${bestBurst.toDouble / burstWindow} bullets/s)"
                          )
                        },
                        new UIStatProperty("Total movement", playerStat.totalMovement.toInt.toString)
                      )
                    )
                  )
              })

              final case class LifeOverTime(playerName: String, color: Vec3, lives: List[(Double, Double)])

              final class LifePlot(
                  width: Int,
                  height: Int,
                  livesInfo: Map[PlotElement, LifeOverTime]
              ) extends Plot {

                setSize(width, height)

                setYAxis(-5, Player.maxLife + 5)
                setXAxis(0, livesInfo.values.map(_.lives.head._1).max + 2000)
                livesInfo.keys.foreach(addChild)

                for (x <- 5000 until xAxis._2.toInt by 5000) {
                  addChild(
                    new Segment(
                      Complex(x, -2),
                      Complex(x, 2),
                      color = Vec4(0.5, 0.5, 0.5, 0.5)
                    )
                  )
                }

                val dashedVerticalLines: Map[PlotElement, Double] = (for (j <- 1 to 9) yield {
                  val x = j * xAxis._2 / 10

                  val segment = new Segment(
                    Complex(x, 0),
                    Complex(x, yAxis._2),
                    color  = Vec4(0.5, 0.5, 0.5, 0.5),
                    dashed = Some(Seq(5, 15))
                  )

                  addChild(segment)

                  (segment, x / 1000.0)
                }).toMap

                dom.document.body.appendChild(canvasElement)
                canvasElement.style.border = "1px solid black"
                setBackgroundColor(1, 1, 1)
                clear()

                setTimeout(500) {
                  setBackgroundColor(1, 1, 1)
                  clear()
                  drawAxes()
                  drawChildren()
                }

                override def onMouseMoveCanvasCoords(x: Double, y: Double): Unit = {
                  clear()
                  drawAxes()
                  drawChildren()

                  closestChildToCanvasCoords(x, y, (elem: PlotElement) => livesInfo.isDefinedAt(elem)) match {
                    case Some(child) =>
                      livesInfo.get(child) match {
                        case Some(info) =>
                          val closestPoint           = child.closestPointToCanvasCoords(Complex(x, y), this)
                          val closestPointPlotCoords = canvasToPlotCoordinates(closestPoint.re, closestPoint.im)

                          val timeLifeInfo = info.lives.minBy(elem => math.abs(elem._1 - closestPointPlotCoords._1))

                          val seconds = timeLifeInfo._1 / 1000

                          if ((Complex(x, y) - closestPoint).modulus2 < 1000) {
                            drawPoint(closestPoint.re, closestPoint.im, 5, info.color)
                            write(
                              s"${info.playerName}",
                              5,
                              height - 40,
                              color = info.color
                            )
                            write(
                              s"Time: ${seconds}s, Health: ${timeLifeInfo._2}",
                              5,
                              height - 20,
                              info.color
                            )
                          }

                        case None =>
                          println("not a life line")
                      }
                    case _ =>
                      println("there seems to be no life line, that's weird...")
                  }

                  closestChildToCanvasCoords(x, y, dashedVerticalLines.isDefinedAt) match {
                    case Some(child) =>
                      val closestPoint = child.closestPointToCanvasCoords(Complex(x, y), this)

                      if (closestPoint.re > width / 4 && (closestPoint - Complex(x, y)).modulus2 < 500) {
                        write(
                          f"${dashedVerticalLines(child)}%1.2fs",
                          closestPoint.re + 5,
                          height - 20,
                          color = Vec4(0.5, 0.5, 0.5, 1)
                        )
                      }
                    case None =>
                  }
                }
              }

              val livesOverTime = stats
                .map(
                  stat =>
                    LifeOverTime(
                      stat.playerName,
                      Vec3(stat.color.red, stat.color.green, stat.color.blue),
                      stat.lifeOverTime
                        .map(tLS => ((tLS.time - startTime).toDouble, tLS.life))
                        .foldLeft(List[(Double, Double)]()) {
                          case (accumulator, (time, life)) =>
                            if (accumulator.isEmpty)
                              List((time, life))
                            else if (accumulator.head._2 != life)
                              (time, life) +: ((time, accumulator.head._2) +: accumulator)
                            else
                              (time, life) +: accumulator
                        }
                        .reverse
                    )
                )
                .filter(_.lives.nonEmpty)

              if (livesOverTime.nonEmpty) {

                val livesInfo: Map[PlotElement, LifeOverTime] =
                  livesOverTime
                    .map(lifeOverTime => {
                      val (xs, ys) = lifeOverTime.lives.toVector.unzip
                      val line     = new Line(xs, ys, lifeOverTime.color)
                      line -> lifeOverTime
                    })
                    .toMap

                new LifePlot(1000, 300, livesInfo)
              }

              setTimeout(1000) {
                UIPages.playersStat.quitButton.onclick = (_: MouseEvent) => {
                  dom.window.location.href = "../../gamemenus/mainscreen/index.html"
                }
              }

            case CaptureTheFlagModeEOGData(scores) =>
              val tBody = dom.document.getElementById("scoreBoard").asInstanceOf[html.Table]
              val rows = scores.toList
                .sortBy(_._2)
                .reverse
                .map({
                  case (teamNbr, points) =>
                    val tr         = dom.document.createElement("tr").asInstanceOf[html.TableRow]
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
