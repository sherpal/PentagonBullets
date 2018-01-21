package renderermainprocesscom

import java.nio.ByteBuffer

import boopickle.CompositePickler
import boopickle.Default._
import sharednodejsapis.{Buffer, IPCRenderer, WebContents}

import scala.scalajs.js.JSConverters._
import scala.scalajs.js.typedarray.TypedArrayBufferOps._

abstract sealed class Message

object Message {
  import StoreGameInfo._
  import GiveGameInfoBack._

  implicit val messagePickler: CompositePickler[Message] = compositePickler[Message]
    .addConcreteType[OpenTooltip]
    .addConcreteType[CloseTooltip]
    .addConcreteType[MoveTooltip]

    .addConcreteType[OpenDevTools]
    .addConcreteType[PlayersInfo]
    .addConcreteType[StoreAction]
    .addConcreteType[NewGame]
    .addConcreteType[PlayerInfo]
    .addConcreteType[GiveMeGameInfo]

    .addConcreteType[GeneralGameInfo]
    .addConcreteType[SendActionGroup]



  def decode(buffer: scala.scalajs.js.Array[Byte]): Message =
    Unpickle[Message](messagePickler).fromBytes(ByteBuffer.wrap(buffer.toArray))

  def encode(message: Message): scala.scalajs.js.Array[Byte] =
    Buffer.from(Pickle.intoBytes(message).arrayBuffer()).toJSArray.map(_.toByte)

  def sendMessageToMainProcess(message: Message): Unit = {
    IPCRenderer.send("main-renderer-message", encode(message))
  }

  def sendMessageToWebContents(webContents: WebContents, message: Message): Unit = {
    webContents.send("main-renderer-message", encode(message))
  }

}


final case class OpenDevTools() extends Message

sealed trait StoreGameInfo extends Message
object StoreGameInfo {
  final case class NewGame(gameName: String, initialGameStateTime: Long) extends StoreGameInfo
  final case class StoreAction(compressedAction: Vector[Byte]) extends StoreGameInfo
  final case class PlayersInfo(info: Vector[PlayerInfo]) extends StoreGameInfo
  final case class GiveMeGameInfo() extends StoreGameInfo

  final case class PlayerInfo(
                               id: Long,
                               name: String,
                               color: Vector[Double],
                               team: Int
                             ) extends Message
}

sealed trait GiveGameInfoBack extends Message
object GiveGameInfoBack {
  final case class GeneralGameInfo(
                                    gameName: String,
                                    startingGameTime: Long,
                                    playersInfo: StoreGameInfo.PlayersInfo,
                                    numberOfActions: Int
                                  ) extends GiveGameInfoBack

  final case class SendActionGroup(actions: List[Vector[Byte]]) extends GiveGameInfoBack
}


sealed trait ManageTooltipMsg extends Message


final case class OpenTooltip(text: String, xMousePos: Int, yMousePos: Int) extends ManageTooltipMsg
final case class MoveTooltip(xMousePos: Int, yMousePos: Int) extends ManageTooltipMsg
final case class CloseTooltip() extends ManageTooltipMsg


