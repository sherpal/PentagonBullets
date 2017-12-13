package renderermainprocesscom

import java.nio.ByteBuffer

import boopickle.CompositePickler
import boopickle.Default._
import sharednodejsapis.Buffer

import scala.scalajs.js.JSConverters._
import scala.scalajs.js.typedarray.TypedArrayBufferOps._

abstract sealed class Message

object Message {
  implicit val messagePickler: CompositePickler[Message] = compositePickler[Message]
    .addConcreteType[OpenTooltip]
    .addConcreteType[CloseTooltip]
    .addConcreteType[MoveTooltip]


  def decode(buffer: scala.scalajs.js.Array[Byte]): Message =
    Unpickle[Message](messagePickler).fromBytes(ByteBuffer.wrap(buffer.toArray))

  def encode(message: Message): scala.scalajs.js.Array[Byte] =
    Buffer.from(Pickle.intoBytes(message).arrayBuffer()).toJSArray.map(_.toByte)


}



sealed trait ManageTooltipMsg extends Message


final case class OpenTooltip(text: String, xMousePos: Int, yMousePos: Int) extends ManageTooltipMsg
final case class MoveTooltip(xMousePos: Int, yMousePos: Int) extends ManageTooltipMsg
final case class CloseTooltip() extends ManageTooltipMsg


