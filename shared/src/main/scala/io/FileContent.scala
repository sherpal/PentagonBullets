package io

import boopickle.Default._
import boopickle.CompositePickler

sealed abstract class FileContent


object FileContent {
  implicit val fileContentPickler: CompositePickler[FileContent] = {
    compositePickler[FileContent]
      .addConcreteType[ConnectionToGameInfo]
      .addConcreteType[ControlBindings]
      .addConcreteType[ControlType.MouseType]
      .addConcreteType[ControlType.KeyboardType]
  }

}


final case class ConnectionToGameInfo(pseudo: String,
                                      gameName: String,
                                      address: String,
                                      port: Int) extends FileContent

sealed trait ControlType extends FileContent
object ControlType {
  final case class MouseType() extends ControlType
  final case class KeyboardType() extends ControlType
  // TODO: controller types? (XBox, PS, Switch?)
}

final case class ControlBindings(up: (ControlType, Int), down: (ControlType, Int),
                                 left: (ControlType, Int), right: (ControlType, Int),
                                 bulletShoot: (ControlType, Int),
                                 selectedAbility: (ControlType, Int),
                                 abilities: List[(ControlType, Int)],
                                 keyCodeToKey: Map[Int, String]) extends FileContent {

  def isUpPressed(controlType: ControlType, code: Int): Boolean = up._2 == code && up._1 == controlType

  def isDownPressed(controlType: ControlType, code: Int): Boolean = down._2 == code && down._1 == controlType

  def isLeftPressed(controlType: ControlType, code: Int): Boolean = left._2 == code && left._1 == controlType

  def isRightPressed(controlType: ControlType, code: Int): Boolean = right._2 == code && right._1 == controlType

  def isBulletShootPressed(controlType: ControlType, code: Int): Boolean =
    bulletShoot._2 == code && controlType == bulletShoot._1

  def isSelectedAbilityPressed(controlType: ControlType, code: Int): Boolean =
    selectedAbility._2 == code && selectedAbility._1 == controlType

  def isAbilityPressed(controlType: ControlType, code: Int): Int = abilities.indexOf((controlType, code))

  def isUsed(controlType: ControlType, code: Int): Boolean =
    up == (controlType, code) ||
    down == (controlType, code) ||
    left == (controlType, code) ||
    right == (controlType, code) ||
    bulletShoot == (controlType, code) ||
    selectedAbility == (controlType, code) ||
    abilities.contains((controlType, code))

  override def equals(that: Any): Boolean = that match {
    case ControlBindings(thatUp, thatDown, thatLeft, thatRight, thatBulletShoot, thatSA, thatAbilities, _) =>
      thatUp == up && thatDown == down && thatLeft == left && thatRight == right &&
      thatBulletShoot == bulletShoot && thatSA == selectedAbility && thatAbilities.forall(abilities contains _) &&
      abilities.forall(thatAbilities contains _)
    case _ => false
  }

  override def hashCode(): Int = super.hashCode()

}
