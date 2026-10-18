package com.example.data

enum class BallForm {
  NORMAL,  // Classic Red Rubber Ball: standard bounce, balanced speed
  FLOAT,   // Inflatable Balloon Ball: light, high bounce, floats on water, rides air currents
  STONE    // Heavy Granite Ball: crushes cracked blocks, heavy weight, sinks in water, activates heavy plates
}

enum class PowerUpType {
  FLOAT_ORB,     // Turn into Balloon Ball
  STONE_ORB,     // Turn into Heavy Stone Ball
  NORMAL_ORB,    // Return to Normal Red Rubber Ball
  SUPER_BOUNCE,  // Temporary extreme jump boost
  EXTRA_LIFE     // Bonus heart life
}

data class Ring(
  val id: Int,
  val x: Float,
  val y: Float,
  val radius: Float = 22f,
  var isCollected: Boolean = false,
  var sparklePhase: Float = 0f
)

data class PowerUpEntity(
  val id: Int,
  val x: Float,
  val y: Float,
  val type: PowerUpType,
  val radius: Float = 18f,
  var isCollected: Boolean = false
)

data class MovingPlatform(
  val id: Int,
  var x: Float,
  var y: Float,
  val width: Float,
  val height: Float,
  val startX: Float,
  val startY: Float,
  val endX: Float,
  val endY: Float,
  val speed: Float,
  var forward: Boolean = true
)

data class PushableBox(
  val id: Int,
  var x: Float,
  var y: Float,
  val width: Float = 44f,
  val height: Float = 44f,
  var vx: Float = 0f,
  var vy: Float = 0f,
  val isHeavy: Boolean = false
)

data class PressureSwitch(
  val id: Int,
  val x: Float,
  val y: Float,
  val width: Float = 40f,
  val height: Float = 14f,
  val targetDoorId: Int,
  val requiresHeavy: Boolean = false,
  var isPressed: Boolean = false
)

data class DoorEntity(
  val id: Int,
  val x: Float,
  val y: Float,
  val width: Float,
  val height: Float,
  var isOpen: Boolean = false,
  var openProgress: Float = 0f
)

data class Checkpoint(
  val id: Int,
  val x: Float,
  val y: Float,
  var isActivated: Boolean = false
)

data class ExitGate(
  val x: Float,
  val y: Float,
  val radius: Float = 32f,
  var isOpen: Boolean = false,
  var spinAngle: Float = 0f
)

data class WaterZone(
  val x: Float,
  val y: Float,
  val width: Float,
  val height: Float,
  val isLava: Boolean = false
)

data class AirVentZone(
  val x: Float,
  val y: Float,
  val width: Float,
  val height: Float,
  val updraftForce: Float = -24f
)

data class SpringPad(
  val x: Float,
  val y: Float,
  val width: Float = 42f,
  val height: Float = 16f,
  val bounceForce: Float = -24f,
  var compressionTimer: Float = 0f
)

data class SpikeHazard(
  val x: Float,
  val y: Float,
  val width: Float,
  val height: Float,
  val isMoving: Boolean = false,
  val moveRangeX: Float = 0f,
  val moveSpeed: Float = 0f,
  var currentOffsetX: Float = 0f,
  var moveDir: Float = 1f
)

data class BreakableBlock(
  val id: Int,
  val x: Float,
  val y: Float,
  val width: Float = 48f,
  val height: Float = 48f,
  var isDestroyed: Boolean = false
)

data class LevelDefinition(
  val number: Int,
  val title: String,
  val subtitle: String,
  val targetParTimeSeconds: Int,
  val worldWidth: Float,
  val worldHeight: Float,
  val spawnX: Float,
  val spawnY: Float,
  val rings: List<Ring>,
  val powerUps: List<PowerUpEntity>,
  val platforms: List<SolidPlatform>,
  val movingPlatforms: List<MovingPlatform>,
  val breakableBlocks: List<BreakableBlock>,
  val pushableBoxes: List<PushableBox>,
  val switches: List<PressureSwitch>,
  val doors: List<DoorEntity>,
  val springs: List<SpringPad>,
  val waterZones: List<WaterZone>,
  val airVents: List<AirVentZone>,
  val spikes: List<SpikeHazard>,
  val checkpoints: List<Checkpoint>,
  val exitGate: ExitGate
)

data class SolidPlatform(
  val x: Float,
  val y: Float,
  val width: Float,
  val height: Float,
  val isGrass: Boolean = true,
  val isStone: Boolean = false
)
