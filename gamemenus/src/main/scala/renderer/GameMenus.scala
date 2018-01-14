package renderer

import gamemenusui.{UIMenuPanels, UIPages}
import gamemode.GameMode
import org.scalajs.dom
import org.scalajs.dom.html
import plots.Plot
import renderermainprocesscom.{CloseTooltip, MoveTooltip, OpenTooltip}
import sharednodejsapis.{BrowserWindow, IPCRenderer}
import ui.UI
import uielements.ControlSettings


/**
 * GameMenus will manage all the menus.
 */
object GameMenus {
  def main(args: Array[String]): Unit = {

    ControlSettings // pre loading bindings so that it is on when showed

    Plot

    UIPages.mainMenu.quitButton.addEventListener[dom.MouseEvent]("click", (_: dom.MouseEvent) => {
      UI.showConfirmBox(
        "Quit Game",
        "Are you sure you want to quit the wonderful Pentagon Bullets?",
        (answer: Boolean) => {
          if (answer) {
            BrowserWindow.getFocusedWindow().close()
          }
        }
      )
    })

    UIMenuPanels.join.actionButton.addEventListener("mousemove", (event: dom.MouseEvent) => {
      IPCRenderer.send("main-renderer-message", renderermainprocesscom.Message.encode(MoveTooltip(
        event.screenX.toInt, event.screenY.toInt
      )))
    })

    UIMenuPanels.join.actionButton.addEventListener("mouseout", (_: dom.MouseEvent) => {
      IPCRenderer.send("main-renderer-message", renderermainprocesscom.Message.encode(CloseTooltip()))
    })

    if (scala.scalajs.LinkingInfo.developmentMode) {
      UIMenuPanels.join.actionButton.addEventListener("mouseenter", (event: dom.MouseEvent) => {
        IPCRenderer.send("main-renderer-message", renderermainprocesscom.Message.encode(OpenTooltip(
          "ceci est un test de tooltip", event.screenX.toInt, event.screenY.toInt
        )))
      })
    }

    GameMode.gameModes.foreach(mode => {
      val option = dom.document.createElement("option").asInstanceOf[html.Option]
      option.value = mode.toString

      dom.document.getElementsByName("mode")(0).asInstanceOf[html.Select].add(option)

      val modeName: html.Paragraph = dom.document.createElement("p").asInstanceOf[html.Paragraph]
      modeName.textContent = mode.toString
      option.appendChild(modeName)
    })


    HostGame
    JoinGame
    CreateServer


    // testing BitmapText
//    val canvas: html.Canvas = dom.document.getElementById("canvas").asInstanceOf[html.Canvas]
//    canvas.width = 300
//    canvas.height = 200
//
//    val webGLRenderer: WebGLRenderer = new WebGLRenderer(canvas.width, canvas.height, new RendererOptions {
//
//      override val view: js.UndefOr[html.Canvas] = canvas
//
//    })
//
//    val stage: PIXIContainer = new PIXIContainer()
//
//    val loader: PIXILoader = new PIXILoader
//    loader
//      .add("C:\\Users\\admintmp\\IdeaProjects\\PentagonBullets\\electron\\assets\\font\\quicksand_0.png")
//      .add("C:\\Users\\admintmp\\IdeaProjects\\PentagonBullets\\electron\\assets\\font\\quicksand.fnt")
//
//    loader.load((l: PIXILoader, resources: js.Dictionary[PIXIResource]) => {
//
//      println(resources.mkString("\n"))
//
//
//
////      val bitmapText: PIXIBitmapText = new PIXIBitmapText("test\n0123456789\nhello", new BitmapTextStyle {
////        override val font: js.UndefOr[String] = "32px Quicksand"
////
////        override val align: js.UndefOr[String] = "right"
////      })
////
////      stage.addChild(bitmapText)
////
////      webGLRenderer.render(stage)
//
//    })



  }
}
