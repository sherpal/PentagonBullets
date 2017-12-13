package gamegui

import custommath.Complex
import entities.{Body, Living, Player}
import gameengine.Engine
import graphics.EntityDrawer
import physics.BoundingBox
import pixigraphics.{PIXIContainer, PIXIGraphics, PIXITexture, Sprite}
import webglgraphics.Vec3

import scala.collection.mutable

trait HealthBar {

  def unit: Option[Body with Living]

  var maxLife: Double = Player.maxLife // TODO: change this when this piece of info is available from the Unit

  val worldWidth: Double = 2 * Player.radius - 5
  val worldHeight: Double = 5

  val boundingBox: BoundingBox = new BoundingBox(-worldWidth / 2, -worldHeight / 2, worldWidth / 2, worldHeight / 2)

  var offset: Complex = Complex(0, - Player.radius - 8)

  private val container: PIXIContainer = new PIXIContainer()

  private val background: Sprite = new Sprite(HealthBar.backgroundTexture)
  container.addChild(background)

  private val lifeSprite: Sprite = new Sprite(HealthBar.lifeTexture)
  container.addChild(lifeSprite)

  private val dangerLifeColor: Int = Vec3(1,0,0).toInt
  private val warningLifeColor: Int = Vec3(1, 163 / 255.0, 0).toInt
  private val safeLifeColor: Int = Vec3(0, 1, 0).toInt

  def addToWorld(): Unit = HealthBar.lifeBarContainer.addChild(container)

  def removeFromWorld(): Unit = HealthBar.lifeBarContainer.removeChild(container)

  def update(): Unit = {

    unit match {
      case Some(u) =>
        val worldPos = u.pos + offset

        val coef = u.lifeTotal / maxLife

        //val lifeWidth = worldWidth * coef

        lifeSprite.width = 100 * coef

        //val lifeCenter = worldPos - (worldWidth - lifeWidth) / 2
        EntityDrawer.camera.viewportManagerSized(
          container, worldPos + Complex(-worldWidth / 2, worldHeight / 2),
          worldWidth, worldHeight, worldPos, boundingBox
        )

        lifeSprite.tint = if (coef <= 0.2) dangerLifeColor else if (coef <= 0.5) warningLifeColor else safeLifeColor
      case _ =>
        container.visible = false
    }



  }

}


object HealthBar {

  private val backgroundTexture: PIXITexture = Engine.graphics.webGLRenderer.generateTexture(
    new PIXIGraphics()
      .beginFill(Vec3(0.8, 0.8, 0.8).toInt)
      .drawRect(0, 0, 100, 5)
      .endFill()
  )

  private val lifeTexture: PIXITexture = Engine.graphics.webGLRenderer.generateTexture(
    new PIXIGraphics()
      .beginFill(0xFFFFFF)
      .drawRect(0, 0, 100, 5)
      .endFill()
  )

  val lifeBarContainer: PIXIContainer = new PIXIContainer()

  private val allHealthBars: mutable.Set[HealthBar] = mutable.Set()

  def addBar(bar: HealthBar): Unit = {
    allHealthBars += bar
    bar.addToWorld()
  }

  def removeBar(bar: HealthBar): Unit = {
    allHealthBars -= bar
    bar.removeFromWorld()
  }

  def updateBars(): Unit =
    allHealthBars.foreach(_.update())

}