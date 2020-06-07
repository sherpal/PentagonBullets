package gamegui

import gui._

class GameClock extends Frame() {

  setSize(300, 25)
  setPoint(TopLeft)

  setScript(ScriptKind.OnUIParentResize)(() => {
    clearAllPoints()
    setPoint(TopLeft)
  })

  private var time: Double = 0

  private val fs: FontString = createFontString()
  fs.setAllPoints()
  fs.setJustifyH(JustifyLeft)

  private def setText(): Unit = {
    val t       = time.toInt
    val seconds = t % 60
    val minutes = (t - seconds) / 60
    val text    = s"$minutes m:${if (seconds < 10) "0" else ""}$seconds s"
    if (fs.text != text)
      fs.setText(text)
  }

  def startClock(): Unit =
    setScript(ScriptKind.OnUpdate)((dt: Double) => {
      time += dt / 1000
      setText()
    })

  def setTime(newTime: Double): Unit = {
    time = newTime
    setText()
  }

}
