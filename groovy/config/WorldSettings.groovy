import javax.vecmath.Vector2f

World{
    Physics{
        maxGravity = 1.0d
        maxGravitySwimming = 0.04d
        gravity = 0.008d
        gravitySwimming = 0.008d * 4d
        friction = 0.15d
        walkingSpeed = 0.025d
        runningFactor = 1.5d
        jumpIntensity = 0.16d
    }
    Biomes {
        Forest.grassDensity = 0.3d
        Plains.grassDensity = 0.1d
        Snow.grassDensity = 0.001d
        Mountains.grassDensity = 0.2d
        Desert.grassDensity = 0.001d
    }
    DiurnalCycle{
        dayNightLengthInMs = new Long((60 * 1000) * 30) // 30 minutes in ms
        initialTimeOffsetInMs = new Long(60 * 1000) // 60 seconds in ms
    }
    Creation{
        spawnOrigin = new Vector2f(-24429, 20547)
        defaultSeed = "Pinkie Pie!"
    }
}