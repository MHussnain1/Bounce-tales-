package com.example.data

object LevelsRepository {

  fun getLevel(number: Int): LevelDefinition {
    return when (number) {
      1 -> createLevel1()
      2 -> createLevel2()
      3 -> createLevel3()
      4 -> createLevel4()
      5 -> createLevel5()
      else -> createLevel1()
    }
  }

  val totalLevels = 5

  // Level 1: Emerald Meadow - Nostalgic Green Hills & Golden Rings Tutorial
  private fun createLevel1(): LevelDefinition {
    val platforms = listOf(
      // Floor segments
      SolidPlatform(0f, 600f, 700f, 200f, isGrass = true),
      SolidPlatform(800f, 600f, 800f, 200f, isGrass = true),
      SolidPlatform(1700f, 600f, 900f, 200f, isGrass = true),
      // Hill Steps & High Ledges
      SolidPlatform(400f, 480f, 200f, 40f, isGrass = true),
      SolidPlatform(1000f, 440f, 220f, 40f, isGrass = true),
      SolidPlatform(1300f, 320f, 250f, 40f, isGrass = true),
      SolidPlatform(1900f, 450f, 300f, 40f, isGrass = true),
      // Left boundary wall
      SolidPlatform(-40f, 0f, 40f, 800f, isStone = true),
      // Right boundary wall
      SolidPlatform(2600f, 0f, 40f, 800f, isStone = true)
    )

    val rings = listOf(
      Ring(1, 300f, 520f),
      Ring(2, 500f, 410f),
      Ring(3, 1100f, 370f),
      Ring(4, 1420f, 250f),
      Ring(5, 2050f, 380f)
    )

    val springs = listOf(
      SpringPad(x = 650f, y = 584f, width = 48f, bounceForce = -23f),
      SpringPad(x = 1250f, y = 584f, width = 48f, bounceForce = -25f)
    )

    val spikes = listOf(
      // Spike pit between platform 1 and 2
      SpikeHazard(x = 700f, y = 620f, width = 100f, height = 30f),
      // Spike pit between platform 2 and 3
      SpikeHazard(x = 1600f, y = 620f, width = 100f, height = 30f)
    )

    val powerUps = listOf(
      PowerUpEntity(1, 1050f, 400f, PowerUpType.SUPER_BOUNCE)
    )

    val checkpoints = listOf(
      Checkpoint(1, 950f, 560f),
      Checkpoint(2, 1800f, 560f)
    )

    return LevelDefinition(
      number = 1,
      title = "Emerald Meadow",
      subtitle = "Roll, Bounce & Collect Golden Rings",
      targetParTimeSeconds = 45,
      worldWidth = 2600f,
      worldHeight = 800f,
      spawnX = 100f,
      spawnY = 540f,
      rings = rings,
      powerUps = powerUps,
      platforms = platforms,
      movingPlatforms = emptyList(),
      breakableBlocks = emptyList(),
      pushableBoxes = emptyList(),
      switches = emptyList(),
      doors = emptyList(),
      springs = springs,
      waterZones = emptyList(),
      airVents = emptyList(),
      spikes = spikes,
      checkpoints = checkpoints,
      exitGate = ExitGate(x = 2400f, y = 530f)
    )
  }

  // Level 2: Aquatic Caverns - Water Physics & Balloon Float Power-Up
  private fun createLevel2(): LevelDefinition {
    val platforms = listOf(
      // Starting deck
      SolidPlatform(0f, 600f, 500f, 200f, isGrass = true),
      // Island in the lake
      SolidPlatform(800f, 550f, 250f, 250f, isGrass = true),
      // High cavern ledge
      SolidPlatform(1200f, 400f, 350f, 50f, isGrass = false, isStone = true),
      // Far bank
      SolidPlatform(1750f, 600f, 750f, 200f, isGrass = true),
      SolidPlatform(2000f, 460f, 300f, 40f, isGrass = true),
      // Ceiling spikes area ledge
      SolidPlatform(1350f, 240f, 280f, 30f, isStone = true),
      // Boundaries
      SolidPlatform(-40f, 0f, 40f, 900f, isStone = true),
      SolidPlatform(2500f, 0f, 40f, 900f, isStone = true)
    )

    val waterZones = listOf(
      // Lake 1: between spawn and island
      WaterZone(x = 500f, y = 550f, width = 300f, height = 250f),
      // Lake 2: between island and cavern ledge
      WaterZone(x = 1050f, y = 560f, width = 700f, height = 240f)
    )

    val powerUps = listOf(
      // Balloon / Float power-up placed before the second lake
      PowerUpEntity(1, 880f, 490f, PowerUpType.FLOAT_ORB),
      // Return to Normal orb near the exit
      PowerUpEntity(2, 1900f, 540f, PowerUpType.NORMAL_ORB)
    )

    val rings = listOf(
      Ring(1, 280f, 530f),
      Ring(2, 650f, 480f), // Floating above water 1
      Ring(3, 920f, 460f),
      Ring(4, 1370f, 320f), // High in cavern
      Ring(5, 1480f, 180f), // Highest float ring near ceiling
      Ring(6, 2150f, 390f)
    )

    val spikes = listOf(
      // Bed spikes in deep water (punishes sinking without balloon)
      SpikeHazard(x = 1100f, y = 740f, width = 400f, height = 30f),
      // Ceiling stalactites
      SpikeHazard(x = 1380f, y = 100f, width = 140f, height = 30f),
      // Hazard pit
      SpikeHazard(x = 2320f, y = 590f, width = 80f, height = 20f)
    )

    val movingPlatforms = listOf(
      MovingPlatform(1, 1600f, 480f, 120f, 24f, startX = 1600f, startY = 480f, endX = 1600f, endY = 320f, speed = 1.8f)
    )

    val springs = listOf(
      SpringPad(x = 420f, y = 584f, width = 44f, bounceForce = -22f)
    )

    val checkpoints = listOf(
      Checkpoint(1, 850f, 510f),
      Checkpoint(2, 1800f, 560f)
    )

    return LevelDefinition(
      number = 2,
      title = "Aquatic Caverns",
      subtitle = "Use Float Orb to Skim Water & Soar",
      targetParTimeSeconds = 65,
      worldWidth = 2500f,
      worldHeight = 900f,
      spawnX = 100f,
      spawnY = 540f,
      rings = rings,
      powerUps = powerUps,
      platforms = platforms,
      movingPlatforms = movingPlatforms,
      breakableBlocks = emptyList(),
      pushableBoxes = emptyList(),
      switches = emptyList(),
      doors = emptyList(),
      springs = springs,
      waterZones = waterZones,
      airVents = emptyList(),
      spikes = spikes,
      checkpoints = checkpoints,
      exitGate = ExitGate(x = 2380f, y = 530f)
    )
  }

  // Level 3: Granite Fortress - Stone Ball, Crushing Blocks & Pressure Switch Puzzles
  private fun createLevel3(): LevelDefinition {
    val platforms = listOf(
      SolidPlatform(0f, 600f, 600f, 200f, isStone = true),
      SolidPlatform(750f, 600f, 500f, 200f, isStone = true),
      SolidPlatform(1400f, 600f, 1200f, 200f, isStone = true),
      // Upper fortress chambers
      SolidPlatform(400f, 440f, 300f, 30f, isStone = true),
      SolidPlatform(900f, 420f, 320f, 30f, isStone = true),
      SolidPlatform(1300f, 450f, 260f, 30f, isStone = true),
      SolidPlatform(1700f, 360f, 350f, 30f, isStone = true),
      // Elevated puzzle platform for crate
      SolidPlatform(800f, 280f, 250f, 30f, isStone = true),
      // Boundary
      SolidPlatform(-40f, 0f, 40f, 900f, isStone = true),
      SolidPlatform(2600f, 0f, 40f, 900f, isStone = true)
    )

    // Breakable cracked blocks that need heavy stone ball to crush
    val breakableBlocks = listOf(
      BreakableBlock(1, 750f, 552f),
      BreakableBlock(2, 750f, 504f),
      BreakableBlock(3, 1400f, 552f),
      BreakableBlock(4, 1400f, 504f)
    )

    // Pushable box that can be rolled onto pressure switch
    val pushableBoxes = listOf(
      PushableBox(1, 880f, 230f, 48f, 48f, isHeavy = true)
    )

    val switches = listOf(
      PressureSwitch(1, x = 1100f, y = 586f, width = 50f, height = 14f, targetDoorId = 101, requiresHeavy = true)
    )

    val doors = listOf(
      DoorEntity(id = 101, x = 1600f, y = 460f, width = 30f, height = 140f)
    )

    val powerUps = listOf(
      // Stone Orb that makes ball heavy and destructive
      PowerUpEntity(1, 350f, 540f, PowerUpType.STONE_ORB),
      // Normal Orb to switch back for high bounce
      PowerUpEntity(2, 1250f, 390f, PowerUpType.NORMAL_ORB),
      PowerUpEntity(3, 1850f, 310f, PowerUpType.SUPER_BOUNCE)
    )

    val rings = listOf(
      Ring(1, 200f, 530f),
      Ring(2, 520f, 380f),
      Ring(3, 850f, 540f), // Behind breakable block
      Ring(4, 980f, 360f),
      Ring(5, 1450f, 390f),
      Ring(6, 1850f, 290f),
      Ring(7, 2300f, 530f)
    )

    val spikes = listOf(
      SpikeHazard(600f, 620f, 150f, 30f),
      SpikeHazard(1250f, 620f, 150f, 30f),
      // Moving ceiling crusher
      SpikeHazard(x = 1950f, y = 320f, width = 60f, height = 30f, isMoving = true, moveRangeX = 140f, moveSpeed = 2f)
    )

    val springs = listOf(
      SpringPad(x = 520f, y = 424f, width = 46f, bounceForce = -24f),
      SpringPad(x = 2100f, y = 584f, width = 46f, bounceForce = -26f)
    )

    val checkpoints = listOf(
      Checkpoint(1, 780f, 560f),
      Checkpoint(2, 1680f, 560f)
    )

    return LevelDefinition(
      number = 3,
      title = "Granite Fortress",
      subtitle = "Smash Cracked Barriers & Solve Switches",
      targetParTimeSeconds = 85,
      worldWidth = 2600f,
      worldHeight = 900f,
      spawnX = 100f,
      spawnY = 540f,
      rings = rings,
      powerUps = powerUps,
      platforms = platforms,
      movingPlatforms = emptyList(),
      breakableBlocks = breakableBlocks,
      pushableBoxes = pushableBoxes,
      switches = switches,
      doors = doors,
      springs = springs,
      waterZones = emptyList(),
      airVents = emptyList(),
      spikes = spikes,
      checkpoints = checkpoints,
      exitGate = ExitGate(x = 2480f, y = 530f)
    )
  }

  // Level 4: Skyward Spire - Updraft Air Vents, Moving Elevators & Precision
  private fun createLevel4(): LevelDefinition {
    val platforms = listOf(
      SolidPlatform(0f, 750f, 400f, 200f, isGrass = true),
      SolidPlatform(500f, 650f, 200f, 40f, isStone = true),
      SolidPlatform(800f, 520f, 250f, 40f, isStone = true),
      SolidPlatform(1300f, 400f, 300f, 40f, isStone = true),
      SolidPlatform(1700f, 300f, 250f, 40f, isStone = true),
      SolidPlatform(2200f, 220f, 400f, 600f, isStone = true),
      // Boundaries
      SolidPlatform(-40f, 0f, 40f, 950f, isStone = true),
      SolidPlatform(2600f, 0f, 40f, 950f, isStone = true)
    )

    val airVents = listOf(
      // Upward fan that lifts the ball sky-high
      AirVentZone(x = 1070f, y = 200f, width = 160f, height = 550f, updraftForce = -26f),
      AirVentZone(x = 1970f, y = 100f, width = 160f, height = 550f, updraftForce = -28f)
    )

    val movingPlatforms = listOf(
      MovingPlatform(1, 380f, 700f, 100f, 24f, startX = 380f, startY = 700f, endX = 480f, endY = 660f, speed = 2f),
      MovingPlatform(2, 1600f, 380f, 110f, 24f, startX = 1600f, startY = 380f, endX = 1750f, endY = 320f, speed = 2.2f)
    )

    val powerUps = listOf(
      PowerUpEntity(1, 280f, 700f, PowerUpType.FLOAT_ORB),
      PowerUpEntity(2, 1400f, 350f, PowerUpType.SUPER_BOUNCE)
    )

    val rings = listOf(
      Ring(1, 220f, 690f),
      Ring(2, 580f, 600f),
      Ring(3, 920f, 460f),
      Ring(4, 1150f, 320f), // Riding air vent 1
      Ring(5, 1420f, 340f),
      Ring(6, 1780f, 240f),
      Ring(7, 2050f, 160f), // Riding air vent 2
      Ring(8, 2350f, 160f)
    )

    val spikes = listOf(
      SpikeHazard(0f, 850f, 2600f, 50f), // Death pit beneath everything
      SpikeHazard(x = 880f, y = 470f, width = 80f, height = 24f, isMoving = true, moveRangeX = 100f, moveSpeed = 2.5f)
    )

    val springs = listOf(
      SpringPad(x = 820f, y = 504f, width = 44f, bounceForce = -25f)
    )

    val checkpoints = listOf(
      Checkpoint(1, 850f, 480f),
      Checkpoint(2, 1750f, 260f)
    )

    return LevelDefinition(
      number = 4,
      title = "Skyward Spire",
      subtitle = "Master the Wind Currents & Moving Ledges",
      targetParTimeSeconds = 90,
      worldWidth = 2600f,
      worldHeight = 950f,
      spawnX = 100f,
      spawnY = 700f,
      rings = rings,
      powerUps = powerUps,
      platforms = platforms,
      movingPlatforms = movingPlatforms,
      breakableBlocks = emptyList(),
      pushableBoxes = emptyList(),
      switches = emptyList(),
      doors = emptyList(),
      springs = springs,
      waterZones = emptyList(),
      airVents = airVents,
      spikes = spikes,
      checkpoints = checkpoints,
      exitGate = ExitGate(x = 2450f, y = 160f)
    )
  }

  // Level 5: Inferno Core - The Master Gauntlet!
  private fun createLevel5(): LevelDefinition {
    val platforms = listOf(
      SolidPlatform(0f, 650f, 400f, 200f, isStone = true),
      SolidPlatform(500f, 650f, 300f, 200f, isStone = true),
      SolidPlatform(950f, 520f, 300f, 40f, isStone = true),
      SolidPlatform(1400f, 420f, 350f, 40f, isStone = true),
      SolidPlatform(1900f, 550f, 300f, 40f, isStone = true),
      SolidPlatform(2350f, 600f, 450f, 200f, isStone = true),
      // Upper puzzle ceiling
      SolidPlatform(1400f, 250f, 250f, 30f, isStone = true),
      // Boundaries
      SolidPlatform(-40f, 0f, 40f, 950f, isStone = true),
      SolidPlatform(2800f, 0f, 40f, 950f, isStone = true)
    )

    val breakableBlocks = listOf(
      BreakableBlock(1, 950f, 472f),
      BreakableBlock(2, 950f, 424f),
      BreakableBlock(3, 1900f, 502f)
    )

    val waterZones = listOf(
      // Lava Hazard Pit in the center!
      WaterZone(x = 400f, y = 680f, width = 100f, height = 200f, isLava = true),
      WaterZone(x = 1250f, y = 680f, width = 150f, height = 200f, isLava = true)
    )

    val airVents = listOf(
      AirVentZone(x = 820f, y = 300f, width = 100f, height = 400f, updraftForce = -25f),
      AirVentZone(x = 1770f, y = 200f, width = 120f, height = 450f, updraftForce = -27f)
    )

    val powerUps = listOf(
      PowerUpEntity(1, 250f, 590f, PowerUpType.FLOAT_ORB),
      PowerUpEntity(2, 650f, 590f, PowerUpType.STONE_ORB),
      PowerUpEntity(3, 1500f, 370f, PowerUpType.NORMAL_ORB),
      PowerUpEntity(4, 2100f, 500f, PowerUpType.SUPER_BOUNCE)
    )

    val rings = listOf(
      Ring(1, 180f, 580f),
      Ring(2, 450f, 520f), // Above lava 1
      Ring(3, 720f, 590f),
      Ring(4, 1020f, 460f),
      Ring(5, 1320f, 360f), // Above lava 2
      Ring(6, 1500f, 200f), // High upper ledge
      Ring(7, 2020f, 470f),
      Ring(8, 2220f, 390f),
      Ring(9, 2550f, 530f)
    )

    val spikes = listOf(
      SpikeHazard(0f, 850f, 2800f, 50f),
      SpikeHazard(x = 1450f, y = 390f, width = 60f, height = 20f, isMoving = true, moveRangeX = 120f, moveSpeed = 3f)
    )

    val movingPlatforms = listOf(
      MovingPlatform(1, 1750f, 500f, 100f, 24f, startX = 1750f, startY = 500f, endX = 1900f, endY = 400f, speed = 2.5f)
    )

    val springs = listOf(
      SpringPad(x = 600f, y = 634f, width = 46f, bounceForce = -26f),
      SpringPad(x = 1600f, y = 404f, width = 46f, bounceForce = -28f)
    )

    val checkpoints = listOf(
      Checkpoint(1, 580f, 600f),
      Checkpoint(2, 1480f, 370f),
      Checkpoint(3, 2000f, 500f)
    )

    return LevelDefinition(
      number = 5,
      title = "Inferno Core",
      subtitle = "The Ultimate Bounce Master Gauntlet",
      targetParTimeSeconds = 120,
      worldWidth = 2800f,
      worldHeight = 950f,
      spawnX = 100f,
      spawnY = 590f,
      rings = rings,
      powerUps = powerUps,
      platforms = platforms,
      movingPlatforms = movingPlatforms,
      breakableBlocks = breakableBlocks,
      pushableBoxes = emptyList(),
      switches = emptyList(),
      doors = emptyList(),
      springs = springs,
      waterZones = waterZones,
      airVents = airVents,
      spikes = spikes,
      checkpoints = checkpoints,
      exitGate = ExitGate(x = 2650f, y = 530f)
    )
  }
}
