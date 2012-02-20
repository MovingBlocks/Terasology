import javax.vecmath.Vector2f
import org.lwjgl.opengl.PixelFormat
import org.lwjgl.opengl.DisplayMode

//TODO restructure options, create different enviroment configs, add example enviroment (custom config trough enviroments)

System {
    //Version
    versionTag = "Pre Alpha"
    // Maximum amount of rendered particles
    maxParticles = 256
    // Resolution of the textures used to render the clouds
    cloudResolution = new Vector2f(128, 128)
    // Time between cloud updates
    cloudUpdateInterval = (Integer) 8000
    // Maximum amount of concurrently running chunk update threads
    maxThreads = 2
    // If set to true chunks get persisted to disk when removed from the chunk cache
    saveChunks = true
    // Maximum amount of chunks stored in the chunk cache
    // ~2048 is a good default value when using the largest viewing distance of 32 chunks
    chunkCacheSize = 2048
    // Maximum amount of chunk VBOs kept in video memory
    // 512 chunks is a good default value for GPUs with ~1024 MB video memory using the largest viewing distance
    maxChunkVBOs = 512
    Debug {
        debug = false
        debugCollision = false
        renderChunkBoundingBoxes = false
        demoFlight = false
        demoFlightSpeed = 0.08d
        godMode = false
    }
}

Graphics {
    gamma = 2.2d
    pixelFormat = new PixelFormat().withDepthBits(24)
    displayMode = new DisplayMode(1280, 720)
    fullscreen = false

    viewingDistanceNear = 8
    viewingDistanceModerate = 16
    viewingDistanceFar = 26
    viewingDistanceUltra = 32

    // Advanced effects – Disable them if you are having problems running the game!
    enablePostProcessingEffects = true
    animatedWaterAndGrass = true

    // Splits chunk meshes into multiple sub-meshes to support frustum and occlusion culling techniques
    verticalChunkMeshSegments = 1
}

HUD{
    placingBox = true
}

Player {
    fov = 86.0d
    cameraBobbing = true
    maxGravity = 1.0d
    maxGravitySwimming = 0.04d
    gravity = 0.008d
    gravitySwimming = 0.008d * 4d
    friction = 0.15d
    walkingSpeed = 0.025d
    runningFactor = 1.5d
    jumpIntensity = 0.16d
    renderFirstPersonView = true
}

Controls{
    mouseSens = 0.075d
}

World{
    spawnOrigin = new Vector2f(-24429, 20547)
    defaultSeed = "Blockmania42"
    dayNightLengthInMs = new Long((60 * 1000) * 30) // 30 minutes in ms
    initialTimeOffsetInMs = new Long(60 * 1000) // 60 seconds in ms
    Biomes {
        Forest.grassDensity = 0.3d
        Plains.grassDensity = 0.1d
        Snow.grassDensity = 0.001d
        Mountains.grassDensity = 0.2d
        Desert.grassDensity = 0.001d
    }
}