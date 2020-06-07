package graphics

import complex.Complex
import entities._
import gameengine.Engine
import gamegui.{AbilityButton, HealthBar}
import gameinfo.GameEvents
import gamestate.GameState
import gamestate.actions.{ChangeBulletRadius, FireLaser, SmashBulletGrows}
import graphics.gameanimations.Laser
import graphics.pixitexturemakers.{BarrierTextureMaker, GunTurretTextureMaker, LaserLauncherTextureMaker, TeamFlagTextureMaker}
import gui.Frame
import physics.{BoundingBox, Polygon}
import pixigraphics._
import webglgraphics.Vec3

import scala.collection.mutable
import scala.scalajs.js.JSConverters._




object EntityDrawer {

  import scala.language.implicitConversions
  private implicit def complexTranslation(z: custommath.Complex): complex.Complex =
    complex.Complex(z.re, z.im)
  private implicit def complexTranslation2(z: complex.Complex): custommath.Complex =
    custommath.Complex(z.re, z.im)

  private var playerColors: Map[Long, Int] = Map()

  def setPlayerColors(colors: Map[Long, (Double, Double, Double)]): Unit = {
    playerColors = colors.map { case (key, elem) => key -> Vec3(elem._1, elem._2, elem._3).toInt }
  }

  val abilityImagesTextures: Map[Int, PIXITexture] = AbilityButton.images.map({
    case (id, fileName) =>
      id -> PIXITexture.fromImage(fileName)
  })

  val healingZoneTexture: PIXITexture = PIXITexture.fromImage("../../assets/entities/healing_zone.png")
  val bulletAmplifierTexture: PIXITexture = PIXITexture.fromImage("../../assets/entities/bullet_amplifier.png")

  val stage: PIXIContainer = Engine.graphics.graphicsStage

  val obstacleStage: PIXIContainer = new PIXIContainer()
  val mistStage: PIXIContainer = new PIXIContainer()
  val healingZoneStage: PIXIContainer = new PIXIContainer()
  val damageZoneStage: PIXIContainer = new PIXIContainer()
  val bulletAmplifierStage: PIXIContainer = new PIXIContainer()
  val barrierStage: PIXIContainer = new PIXIContainer()
  val healingUnitStage: PIXIContainer = new PIXIContainer()
  val abilityGiverStage: PIXIContainer = new PIXIContainer()
  val playerStage: PIXIContainer = new PIXIContainer()
  val bulletStage: PIXIContainer = new PIXIContainer()
  val gunTurretStage: PIXIContainer = new PIXIContainer()
  val laserLauncherStage: PIXIContainer = new PIXIContainer()
  val teamFlagStage: PIXIContainer = new PIXIContainer()

  val laserLauncherAnimationStage: PIXIContainer = new PIXIContainer()

  List(
    mistStage, laserLauncherAnimationStage, healingZoneStage,
    damageZoneStage, bulletAmplifierStage, barrierStage, healingUnitStage,
    abilityGiverStage, playerStage, gunTurretStage,
    bulletStage, obstacleStage, laserLauncherStage, teamFlagStage, HealthBar.lifeBarContainer
  ).foreach(container => stage.addChild(container))

  val camera: Camera = new Camera(Engine.graphics.canvas)

  /**
   * World dimensions seen by a player.
   */
  val worldWidth: Int = 1500
  val worldHeight: Int = 800

  camera.worldWidth = worldWidth
  camera.worldHeight = worldHeight

  val cameraWidthToHeightRatio: Double = worldHeight.toDouble / worldWidth


  def newDisk(center: Complex, radius: Int, r: Double, g: Double, b: Double,
              alpha: Double = 1.0): Sprite = {
    val (x, y) = (2 * radius, 2 * radius)//canvas2d.changeCoordinates(center)

    val disk = new Sprite(Engine.graphics.webGLRenderer.generateTexture(
      new PIXIGraphics()
      .beginFill(Vec3(r, g, b).toInt, alpha)
      .drawCircle(x, y, radius)
      .endFill()
    ))

    disk
  }

  def newCircle(radius: Int, r: Double, g: Double, b: Double, alpha: Double = 1.0): Sprite = {
    val circle = new Sprite(Engine.graphics.webGLRenderer.generateTexture(
      new PIXIGraphics()
        .lineStyle(2, Vec3(r, g, b).toInt, alpha)
        .arc(2 * radius, 2 * radius, radius, 0, 6.283185307179586)
    ))



    circle
  }

  def newPlayer(vertices: Seq[Complex], plrColor: (Double, Double, Double),
                teamColor: (Double, Double, Double)): Sprite = {

    val innerVertices: Seq[Complex] = vertices.map(z => (z.modulus / 2) * Complex.rotation(z.arg))

    val localCoordsVertices = vertices
      .map(z => (z.re, -z.im))
      .flatMap(elem => Vector(elem._1, elem._2))
      .toJSArray

    val localCoordsInnerVertices = innerVertices
      .map(z => (z.re, -z.im))
      .flatMap(elem => Vector(elem._1, elem._2))
      .toJSArray

    val player = new Sprite(Engine.graphics.webGLRenderer.generateTexture(
      new PIXIGraphics()
        .beginFill(Vec3(teamColor._1, teamColor._2, teamColor._3).toInt)
        .drawPolygon(localCoordsVertices)
        .endFill()
        .beginFill(Vec3(plrColor._1, plrColor._2, plrColor._3).toInt)
        .drawPolygon(localCoordsInnerVertices)
        .endFill()
    ))

    player

  }

  def newPolygon(vertices: Seq[Complex], r: Double, g: Double, b: Double, alpha: Double = 1.0): Sprite = {
    val localCoordsVertices = vertices
      .map(z => (z.re, -z.im))
      .flatMap(elem => Vector(elem._1, elem._2))
      .toJSArray
    val polygon = new Sprite(Engine.graphics.webGLRenderer.generateTexture(
      new PIXIGraphics()
      .beginFill(Vec3(r, g, b).toInt, alpha)
      .drawPolygon(localCoordsVertices)
      .endFill()
    ))
    polygon
  }

  def newHealUnit(): Sprite = {
    val healCrossVertices = Vector(
      Complex(3,-1), Complex(3,1), Complex(1,1), Complex(1,3), Complex(-1,3),
      Complex(-1,1), Complex(-3,1), Complex(-3,-1), Complex(-1,-1), Complex(-1,-3),
      Complex(1,-3), Complex(1,-1)
    )
      .map(_ * HealUnit.radius / 5)
      .map(~_)
      .map(_ + Complex(2 * HealUnit.radius.toInt, 2 * HealUnit.radius.toInt))
      .flatMap(z => List(z.re, z.im))
    val circle = new Sprite(Engine.graphics.webGLRenderer.generateTexture(new PIXIGraphics()
        .beginFill(Vec3(1,1,1).toInt)
        .drawCircle(2 * HealUnit.radius.toInt, 2 * HealUnit.radius.toInt, HealUnit.radius.toInt)
        .endFill()
      .beginFill(Vec3(0,1,0).toInt)
      .drawPolygon(healCrossVertices.toJSArray)
      .endFill()
    ))

    circle
  }

  def newAbilityGiver(abilityId: Int): (Sprite, PIXIGraphics) = {
    val mask = new PIXIGraphics()
      .beginFill(0xFFFFFF, 1.0)
      .drawCircle(0, 0, AbilityGiver.radius)
      .endFill()

    val sprite = new Sprite(abilityImagesTextures(abilityId))

    sprite.mask = mask

    (sprite, mask)
  }



  val barrierSprites: mutable.Map[Long, Sprite] = mutable.Map()

  def drawBarriers(state: GameState, colors: Map[Long, (Double, Double, Double)]): Unit = {
    barrierSprites.toMap.filterNot(e => state.barriers.isDefinedAt(e._1)).foreach({ case (id, sprite) =>
        barrierSprites -= id
        barrierStage.removeChild(sprite)
    })

    state.barriers.foreach({ case (id, barrier) =>
      val sprite = barrierSprites.get(id) match {
        case Some(elem) =>
          elem
        case None =>
          val sprite = new Sprite(BarrierTextureMaker(colors(barrier.ownerId)))
          sprite.anchor.set(0.5, 0.5)

          barrierSprites += id -> sprite
          barrierStage.addChild(sprite)

          sprite.rotation = -barrier.rotation

          sprite
      }

      camera.viewportManager(sprite, barrier.pos, barrier.shape.boundingBox)
    })
  }


  def newBulletAmplifier(color: (Double, Double, Double)): Sprite = {

    val sprite = new Sprite(bulletAmplifierTexture)
    sprite.tint = Vec3(color._1, color._2, color._3).toInt
    sprite.alpha = 0.5
    sprite

  }

  val bulletAmplifierSprites: mutable.Map[Long, Sprite] = mutable.Map()

  def drawBulletAmplifiers(state: GameState, colors: Map[Long, (Double, Double, Double)]): Unit = {
    bulletAmplifierSprites.toMap.filterNot(e => state.bulletAmplifiers.isDefinedAt(e._1)).foreach({ case (id, sprite) =>
      bulletAmplifierSprites -= id
      bulletAmplifierStage.removeChild(sprite)
    })

    state.bulletAmplifiers.foreach({ case (id, bulletAmplifier) =>
      val sprite = bulletAmplifierSprites.get(id) match {
        case Some(elem) =>
          elem
        case None =>
          val elem = newBulletAmplifier(colors(bulletAmplifier.ownerId))
          bulletAmplifierSprites += (id -> elem)
          bulletAmplifierStage.addChild(elem)
          elem.anchor.set(0.5, 0.5)
          elem.rotation = -bulletAmplifier.rotation
          elem
      }
      camera.viewportManager(sprite, bulletAmplifier.pos, bulletAmplifier.shape.boundingBox)
    })
  }

  def newHealingZone(color: (Double, Double, Double)): Sprite = {

    val sprite = new Sprite(healingZoneTexture)
    sprite.tint = Vec3(color._1, color._2, color._3).toInt
    sprite

  }

  val healingZoneSprites: mutable.Map[Long, Sprite] = mutable.Map()

  def drawHealingZones(state: GameState, colors: Map[Long, (Double, Double, Double)]): Unit = {
    healingZoneSprites.toMap.foreach({ case (id, sprite) =>
      if (!state.healingZones.isDefinedAt(id)) {
        healingZoneSprites -= id
        healingZoneStage.removeChild(sprite)
      }
    })

    state.healingZones.foreach({ case (id, zone) =>
      val healingZoneSprite = healingZoneSprites.get(id) match {
        case Some(elem) =>
          elem
        case None =>
          val elem = newHealingZone(colors(zone.ownerId))
          healingZoneStage.addChild(elem)
          elem.anchor.set(0.5, 0.5)
          healingZoneSprites += (id -> elem)
          elem
      }
      camera.viewportManager(healingZoneSprite, zone.pos, zone.shape.boundingBox)
    })

  }

  val players: mutable.Map[Long, (Sprite, Sprite)] = mutable.Map()

  def drawPlayersPIXI(state: GameState, time: Long, colors: Map[Long, (Double, Double, Double)],
                      teamColors: Map[Int, (Double, Double, Double)]): Unit = {
    val playersAlive = state.players
    players.filterNot(elem => playersAlive.isDefinedAt(elem._1)).foreach(elem => {
      playerStage.removeChild(elem._2._1)
      playerStage.removeChild(elem._2._2)
      players -= elem._1
    })

    playersAlive.foreach({
      case (id, player) =>
        val (polygon, circle) = players.get(id) match {
          case Some(elem) =>
            elem
          case None =>
            val elem = (newPlayer(
              player.shape.vertices.map(z => complex.Complex(z.re, z.im)), colors(player.id), teamColors(player.team)
            ), newDisk(0, 2, 1, 1, 1))
            players += (id -> elem)

            playerStage.addChild(elem._1)
            playerStage.addChild(elem._2)

            elem._1.anchor.set(0.5, 0.5)
            elem._2.anchor.set(0.5, 0.5)

            elem
        }
        //val playerPosition = player.currentPosition(time - player.time, state.obstacles.values)
        val playerPosition = player.pos
        //val (x, y) = Engine.graphics.changeCoordinates(playerPosition)
        val rot = (Player.radius - 2) * custommath.Complex.rotation(player.rotation)
        //polygon.position.set(x, y)
        camera.viewportManager(polygon, playerPosition, player.shape.boundingBox)
        polygon.rotation = -player.rotation

        val directionDiskPos = playerPosition + rot
        val directionDiskRadius = math.max(circle.width / 2, 1)
        camera.viewportManager(circle, directionDiskPos, new BoundingBox(
          directionDiskRadius, directionDiskRadius, directionDiskRadius, directionDiskRadius
        ))
        //circle.position.set(x + rot.re, y - rot.im)
    })

  }

  val playerBuffs: mutable.Map[Long, Sprite] = mutable.Map()

  def drawPlayerBuffs(state: GameState, time: Long): Unit = {

    val shields = state.actionChangers.filter(_._2.isInstanceOf[Shield]).map(_.asInstanceOf[(Long, Shield)])

    playerBuffs
      .filterNot(elem => shields.isDefinedAt(elem._1))
      .foreach(elem => {
      playerStage.removeChild(elem._2)
      playerBuffs -= elem._1
    })

    shields.foreach({
      case (id, shield) =>
        val buff = playerBuffs.get(id) match {
          case Some(elem) =>
            elem
          case None =>
            val elem = newCircle(Player.radius.toInt + 2, 1, 1, 1)
            playerStage.addChild(elem)

            elem.anchor.set(0.5, 0.5)

            playerBuffs += (id -> elem)

            elem
        }

        state.players.get(shield.playerId) match {
          case Some(player) =>
            val playerPos = player.currentPosition(time - player.time, state.obstacles.values)
            camera.viewportManager(buff, playerPos, player.shape.boundingBox)
          case None =>
            buff.visible = false
        }
    })

  }


  val teamFlagSprites: mutable.Map[Int, Sprite] = mutable.Map()

  def drawTeamFlags(state: GameState, colors: Map[Int, (Double, Double, Double)]): Unit = {
    teamFlagSprites.filterNot(elem => state.flags.isDefinedAt(elem._1)).foreach(elem => {
      teamFlagStage.removeChild(elem._2)
      teamFlagSprites -= elem._1
    })

    state.flags.foreach({ case (teamNbr, flag) =>
      val elem = teamFlagSprites.get(teamNbr) match {
        case Some(e) =>
          e
        case None =>
          val flagSprite = new Sprite(TeamFlagTextureMaker(colors(teamNbr)))
          flagSprite.anchor.set(0, 1)
          teamFlagSprites += (teamNbr -> flagSprite)
          teamFlagStage.addChild(flagSprite)
          flagSprite
      }
      camera.viewportManager(elem, flag.currentPosition(state, 0), flag.shape.boundingBox)
    })
  }

  val gunTurretSprites: mutable.Map[Long, Sprite] = mutable.Map()

  def drawGunTurrets(state: GameState, colors: Map[Long, (Double, Double, Double)]): Unit = {
    gunTurretSprites.filterNot(elem => state.gunTurrets.isDefinedAt(elem._1)).foreach(elem => {
      gunTurretStage.removeChild(elem._2)
      gunTurretSprites -= elem._1
    })

    state.gunTurrets.foreach({ case (turretId, turret) =>
      val elem = gunTurretSprites.get(turretId) match {
        case Some(e) =>
          e
        case None =>
          val turretSprite = new Sprite(GunTurretTextureMaker(colors(turret.ownerId), turret.radius))
          turretSprite.anchor.set(0.5, 0.5)
          gunTurretSprites += (turretId -> turretSprite)
          gunTurretStage.addChild(turretSprite)
          turretSprite
      }
      elem.rotation = - turret.rotation
      camera.viewportManager(elem, turret.pos, turret.shape.boundingBox)
    })
  }

  val laserLauncherSprites: mutable.Map[Long, Sprite] = mutable.Map()

  def drawLaserLauncherSprites(state: GameState, colors: Map[Long, (Double, Double, Double)]): Unit = {
    laserLauncherSprites.filterNot(elem => state.laserLaunchers.isDefinedAt(elem._1)).foreach(elem => {
      laserLauncherStage.removeChild(elem._2)
      laserLauncherSprites -= elem._1
    })

    state.laserLaunchers.foreach({ case (laserLauncherId, laserLauncher) =>
        val elem = laserLauncherSprites.get(laserLauncherId) match {
          case Some(e) =>
            e
          case None =>
            val laserLauncherSprite = new Sprite(LaserLauncherTextureMaker(colors(laserLauncher.ownerId)))
            laserLauncherSprite.anchor.set(0.5, 0.5)
            laserLauncherSprites += (laserLauncherId -> laserLauncherSprite)
            laserLauncherStage.addChild(laserLauncherSprite)
            laserLauncherSprite
        }
        camera.viewportManager(elem, laserLauncher.pos, laserLauncher.shape.boundingBox)
    })


  }

  /**
   * drawBulletsPIXI is in charge of all the BulletLike entities, since they all draw the same way.
   */
  val bullets: mutable.Map[Long, Sprite] = mutable.Map()

  // TODO: store the created sprites in a queue and reuse them, instead of creating them all the time.

  def drawBulletsPIXI(state: GameState, time: Long, colors: Map[Long, (Double, Double, Double)]): Unit = {
    val bs = state.bullets ++ state.smashBullets
    bullets.filterNot(elem => bs.isDefinedAt(elem._1)).foreach(elem => {
      bulletStage.removeChild(elem._2)
      bullets -= elem._1
    })

    bs.foreach({
      case (id, bullet) =>
        val newElem = bullets.get(id) match {
          case Some(elem) =>
            elem
          case None =>
            val (r, g, b) = colors(bullet.ownerId)
            val elem = newDisk(Complex(0,0), bullet.radius.toInt, r, g, b)
            bulletStage.addChild(elem)

            elem.anchor.set(0.5, 0.5)

            bullets += (id -> elem)
            elem
        }
//        val (x, y) = Engine.graphics.changeCoordinates(bullet.currentPosition(time - bullet.time))
//        newElem.position.set(x, y)
        camera.viewportManager(newElem, bullet.currentPosition(time - bullet.time), bullet.shape.boundingBox)
    })
  }

  val mistsSprites: mutable.Map[Long, (PIXIGraphics, Double)] = mutable.Map()

  def drawMistsPIXI(mists: Map[Long, Mist]): Unit = {
    mistsSprites.filterNot(elem => mists.isDefinedAt(elem._1)).foreach(elem => {
      mistStage.removeChild(elem._2._1)
      mistsSprites -= elem._1
    })


    def addMistSprite(mist: Mist): (PIXIGraphics, Double) = {
      val localCoordsVertices = mist.shape.vertices
        .map(z => (z.re, -z.im))
        .flatMap(elem => Vector(elem._1, elem._2))
        .toJSArray
      val polygon = new PIXIGraphics()
        .beginFill(Vec3(0.8, 0.8, 0.8).toInt, 0.3)
        .drawPolygon(localCoordsVertices)
        .endFill()

      mistStage.addChild(polygon)

      (polygon, mist.sideLength)
    }

    mists.foreach({
      case (id, mist) =>
        val m = mistsSprites.get(id) match {
          case Some(elem) if elem._2 == mist.sideLength =>
            elem
          case Some(elem) =>
            mistStage.removeChild(elem._1)
            val newElem = addMistSprite(mist)
            mistsSprites += (id -> newElem)
            newElem
          case None =>
            val newElem = addMistSprite(mist)
            mistsSprites += (id -> newElem)
            newElem
        }
        camera.viewportManager(m._1, Complex(0, 0), Complex(0, 0), mist.shape.boundingBox)
    })
  }

  val damageZones: mutable.Map[Long, Sprite] = mutable.Map()

  def drawDamageZonesPIXI(zones: Map[Long, DamageZone]): Unit = {
    damageZones.filterNot(elem => zones.isDefinedAt(elem._1)).foreach(elem => {
      damageZoneStage.removeChild(elem._2)
      damageZones -= elem._1
    })

    zones.foreach({
      case (id, zone) =>
        val z = damageZones.get(id) match {
          case Some(elem) =>
            elem
          case None =>
            val elem = newDisk(zone.pos, DamageZone.maxRadius, 1, 0, 1)
            damageZoneStage.addChild(elem)
            elem.alpha = 0.3
            elem.anchor.set(0.5, 0.5)

            damageZones += (id -> elem)
            elem
        }
        camera.viewportManager(z, zone.pos, zone.pos, zone.shape.boundingBox)
        z.width = 2 * zone.radius * camera.scaleX
        z.height = 2 * zone.radius * camera.scaleY
    })
  }

  val healUnits: mutable.Map[Long, Sprite] = mutable.Map()

  def drawHealUnitsPIXI(hus: Map[Long, HealUnit]): Unit = {
    healUnits.filterNot(elem => hus.isDefinedAt(elem._1)).foreach(elem => {
      healingUnitStage.removeChild(elem._2)
//      elem._2.visible = false
      healUnits -= elem._1
    })

    hus.foreach({
      case (id, healUnit) =>
        camera.viewportManager(healUnits.get(id) match {
          case Some(elem) =>
            elem
          case None =>
            val elem = newHealUnit()
            healingUnitStage.addChild(elem)
//            val (x, y) = Engine.graphics.changeCoordinates(healUnit.pos)
//            elem.position.set(x - HealUnit.radius, y - HealUnit.radius)

            healUnits += (id -> elem)
            elem
        }, healUnit.pos + Complex(-HealUnit.radius, HealUnit.radius), healUnit.pos, healUnit.shape.boundingBox)
    })
  }

  val abilityGivers: mutable.Map[Long, (Sprite, PIXIGraphics)] = mutable.Map()

  def drawAbilityGivers(givers: Map[Long, AbilityGiver]): Unit = {
    abilityGivers.filterNot(elem => givers.isDefinedAt(elem._1)).foreach(elem => {
      abilityGiverStage.removeChild(elem._2._1)
      abilityGiverStage.removeChild(elem._2._2)
      abilityGivers -= elem._1
    })

    givers.foreach({
      case (id, abilityGiver) =>
        val (sprite, mask) = abilityGivers.get(id) match {
          case Some(elem) =>
            elem
          case None =>
            val elem = newAbilityGiver(abilityGiver.abilityId)
            abilityGiverStage.addChild(elem._1)
            abilityGiverStage.addChild(elem._2)
            elem._1.anchor.set(0.5, 0.5)

            abilityGivers += (id -> elem)

            elem
        }
        camera.viewportManager(sprite, abilityGiver.pos, abilityGiver.shape.boundingBox)
        camera.viewportManager(mask, abilityGiver.pos, abilityGiver.pos, abilityGiver.shape.boundingBox)
    })
  }

  val obstacles: mutable.Map[Long, Sprite] = mutable.Map()

  def drawObstaclesPIXI(obs: Map[Long, Obstacle]): Unit = {
    obstacles.filterNot(elem => obs.isDefinedAt(elem._1)).foreach(elem => {
      obstacleStage.removeChild(elem._2)
      obstacles -= elem._1
    })

    obs.foreach({
      case (id, obstacle) =>
        camera.viewportManager(obstacles.get(id) match {
          case Some(elem) =>
            elem
          case None =>
            val elem = newPolygon(
              obstacle.shape.asInstanceOf[Polygon].vertices.map(z => complex.Complex(z.re, z.im)), 1, 1, 1
            )
            obstacleStage.addChild(elem)
            elem.anchor.set(0.5, 0.5)

            obstacles += (id -> elem)
            elem
        }, obstacle.pos, obstacle.shape.boundingBox)
    })
  }


  val watchingFrame: Frame = new Frame()
  watchingFrame.registerEvent(GameEvents.OnChangeBulletRadius)((action: ChangeBulletRadius, _: GameState) => {
    // we remove the bulletSprite attached to that bullet.
    // It will be created directly automatically.
    bullets.get(action.bulletId) match {
      case Some(bulletSprite) =>
        bulletStage.removeChild(bulletSprite)
        bullets -= action.bulletId
      case None =>
    }
  })
  watchingFrame.registerEvent(GameEvents.OnSmashBulletGrows)((action: SmashBulletGrows, _: GameState) => {
    // we remove the bulletSprite attached to that bullet.
    // It will be created directly automatically.
    bullets.get(action.smashBulletId) match {
      case Some(bulletSprite) =>
        bulletStage.removeChild(bulletSprite)
        bullets -= action.smashBulletId
      case None =>
    }
  })
  watchingFrame.registerEvent(GameEvents.OnFireLaser)((action: FireLaser, _: GameState) => {
    new Laser(action.laserVertices, playerColors(action.ownerId), laserLauncherAnimationStage)
  })



  /**
   * Draw the GameState at the current time.
   *
   * @param state  state of the game
   * @param time   the current time of the server
   * @param colors a Map that sends entity ids to their color
   * @param bulletColors maps the id of a player to the color of its team leader
   */
  def drawState(cameraPos: custommath.Complex, state: GameState, time: Long,
                colors: Map[Long, (Double, Double, Double)],
                teamColors: Map[Int, (Double, Double, Double)],
                bulletColors: Map[Long, (Double, Double, Double)]): Unit = {

    camera.worldCenter = cameraPos

    drawMistsPIXI(state.mists)
    drawObstaclesPIXI(state.obstacles)
    drawHealingZones(state, colors)
    drawBulletAmplifiers(state, bulletColors)
    drawBarriers(state, colors)
    drawPlayersPIXI(state, time, colors, teamColors)
    drawBulletsPIXI(state, time, bulletColors)
    drawGunTurrets(state, bulletColors)
    drawLaserLauncherSprites(state, colors)
    drawDamageZonesPIXI(state.damageZones)
    drawAbilityGivers(state.abilityGivers)
    drawHealUnitsPIXI(state.healUnits)
    drawPlayerBuffs(state, time)

    drawTeamFlags(state, teamColors)

    HealthBar.updateBars()

    GameAnimation.animate(state, time, EntityDrawer.camera)

  }

}
