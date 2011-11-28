import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.PixelFormat
import javax.vecmath.Vector2f

System {

    gameTitle = "Blockmania Pre Alpha"

    // Maximum amount of chunk updates per iteration
    maxChunkUpdatesPerIteration = 2
    // Max amount of particles
    maxParticles = 128
    // Size of the dynamic cloud texture
    cloudResolution = new Vector2f(64, 64)
    // Cloud update interval in ms
    cloudUpdateInterval = (Integer) 1000
    // Defines the maximum amount of threads used for chunk generation
    maxThreads = Runtime.getRuntime().availableProcessors() <= 2 ? 1 : 2;
    // Enable/or disable the persisting of chunks
    saveChunks = true

    // Size of the chunk cache
    chunkCacheSize = 1024

    Debug {

        debug = false
        debugCollision = false

        chunkOutlines = false

        demoFlight = true
        godMode = true

    }
}

Graphics {

    pixelFormat = new PixelFormat().withDepthBits(24)
    displayMode = new DisplayMode(1280, 720)

    aspectRatio = 16.0d / 9.0d

    fullscreen = false;

    fov = 64.0d

    viewingDistanceX = 25
    viewingDistanceZ = 25

}

HUD {

    crosshair = true
    rotatingBlock = true
    placingBox = true

}

Lighting {

    occlusionIntensDefault = 1.0d / 7.0d
    occlusionIntensBillboards = occlusionIntensDefault / 3.0d

}

Controls {

    mouseSens = 0.075d

}

Chunk {

    dimensionX = 16
    dimensionY = 256
    dimensionZ = 16

}

Player {

    bobbing = true

    maxGravity = 0.7d
    maxGravitySwimming = 0.01d

    gravity = 0.006d
    gravitySwimming = gravity * 2d;

    friction = 0.08d

    walkingSpeed = 0.03d
    runningFactor = 1.8d
    jumpIntensity = 0.125d

}

World {

    defaultSeed = "nXhTnOmGgLsZmWhO"

    dayNightLengthInMs = new Long((60 * 1000) * 20) // 20 minutes in ms
    initialTimeOffsetInMs = new Long(60 * 1000) // 120 seconds in ms

    Biomes {

        Forest {

            grassDensity = 0.3d

        }

        Plains {

            grassDensity = 0.1d

        }

        Snow {

            grassDensity = 0.001d

        }

        Mountains {

            grassDensity = 0.2d

        }

        Desert {

            grassDensity = 0.001d

        }

    }

    Resources {

        probCoal = -2d
        probGold = -3d
        probSilver = -2.5d
        probRedstone = -3d
        probDiamond = -4d

    }
}
