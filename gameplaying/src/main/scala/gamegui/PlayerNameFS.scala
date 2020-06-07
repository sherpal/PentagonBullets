package gamegui

import custommath.Complex
import graphics.EntityDrawer
import gui._

import scala.collection.mutable

object PlayerNameFS {

  def newName(playerName: String, pos: Complex): FontString = {
    val fs = UIParent.createFontString()

    fs.setSize(200, 30)
    fs.setTextColor(1, 1, 1)
    fs.setText(playerName)

    val Complex(x, y) = EntityDrawer.camera.worldToMousePos(pos)

    fs.setPoint(Bottom, UIParent, Center, x, y)

    fontStrings += playerName -> (fs, pos)

    fs
  }

  private val watchingFrame: Frame = new Frame()
  watchingFrame.setScript(ScriptKind.OnUIParentResize)(() => {
    placeFontStrings()
  })

  private val fontStrings: mutable.Map[String, (FontString, Complex)] = mutable.Map()

  def hideFontStrings(): Unit = {
    watchingFrame.hide()
    fontStrings.values.map(_._1).foreach(_.hide())
  }

  def placeFontStrings(): Unit =
    fontStrings.values.foreach({
      case (fs, pos) =>
        fs.getPointCoords(Bottom) match {
          case Some(_) =>
            fs.clearAllPoints()

            val Complex(newX, newY) = EntityDrawer.camera.worldToMousePos(pos)

            fs.setPoint(Bottom, UIParent, Center, newX, newY)
          case None =>
        }
    })
}
