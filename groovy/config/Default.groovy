import javax.vecmath.Vector2f
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.PixelFormat

System {

    gameTitle = "Blockmania Pre Alpha"

    // Max amount of particles
    maxParticles = 256

    // Size of the dynamic cloud texture
    cloudResolution = new Vector2f(64, 64)

    // Cloud update interval in ms
    cloudUpdateInterval = (Integer) 1000

    // Defines the maximum amount of threads used for chunk generation
    maxThreads = Runtime.getRuntime().availableProcessors() <= 2 ? 1 : 2

    // Enable/or disable the persisting of chunks
    saveChunks = true

    // Size of the chunk cache
    chunkCacheSize = 1024+512

    Debug {

        debug = false
        debugCollision = false

        renderChunkBoundingBoxes = false

        demoFlight = false
        godMode = false

    }
}

Graphics {

    gamma = 2.2d
    animatedWaterAndGrass = true

    mipMapping = true
    anisotropicFiltering = 1

    pixelFormat = new PixelFormat().withDepthBits(24)
    displayMode = new DisplayMode(1280, 720)

    aspectRatio = 16.0d / 9.0d

    fullscreen = false

    fov = 64.0d

    viewingDistanceNear = 8
    viewingDistanceModerate = 16
    viewingDistanceFar = 26
    viewingDistanceUltra = 32

    // Splits chunk meshes into multiple sub-meshes to support frustum and occlusion culling techniques
    verticalChunkMeshSegments = 2

    OcclusionCulling {

        // Occlusion culling is currently disabled by default
        enabled = false
        // The last 60 % of the visible chunks will be culled using occlusion queries
        distanceOffset = 0.4d
        // Minimum time gap between queries and checks for available results
        timeGap = 100l // ms

    }

}

Physics {
    // REQUIRES A LOT OF MEMORY DEPENDING
    // ON THE ACTIVE VIEWING DISTANCE AND SIZE OF THE CHUNK CACHE
    generatePhysicsMeshes = false
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

Player {

    bobbing = true

    maxGravity = 1.0d
    maxGravitySwimming = 0.01d

    gravity = 0.006d
    gravitySwimming = gravity * 2d;

    friction = 0.08d

    walkingSpeed = 0.03d
    runningFactor = 1.8d
    jumpIntensity = 0.125d

}

World {

    spawnOrigin = new Vector2f(-24429, 20547)

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
