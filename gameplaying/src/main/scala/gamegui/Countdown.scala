package gamegui

import gui.{BitmapText, Frame, Point, ScriptKind}

class Countdown(point: Point, width: Int, height: Int) extends Frame {

  setPoint(point)
  setSize(width, height)

  private lazy val bitmapText: BitmapText = {
    val bt = createBitmapText("0", "32 Quicksand", "center")
    bt.setAllPoints()
    bt.hide()
    bt
  }

  /** Remaining time in ms */
  private var remainingTime: Double = 0

  private def updateTime(dt: Double): Unit = {
    remainingTime -= dt

    setText()

    if (remainingTime < 0) {
      bitmapText.hide()
      removeScript(ScriptKind.OnUpdate)
    }

  }

  private def setText(): Unit = {
    val text = math.ceil(remainingTime / 1000).toInt + "s"
    if (text != bitmapText.text) {
      bitmapText.setText(text)
    }
  }

  def startClock(time: Long): Unit = {
    remainingTime = time
    bitmapText.show()
    setText()

    setScript(ScriptKind.OnUpdate)((dt: Double) => {
      updateTime(dt)
    })
  }



}
