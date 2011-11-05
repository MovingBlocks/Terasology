import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.PixelFormat

System {

    gameTitle = "Blockmania Pre Alpha"

    maxThreads = Math.min(Runtime.getRuntime().availableProcessors() + 1, 6)

    sandboxed = false

    saveChunks = true

    Debug {

        debug = false;
        debugCollision = false

        chunkOutlines = false

        demoFlight = false

        godMode = false;

    }
}

Graphics {

    pixelFormat = new PixelFormat().withDepthBits(24)
    displayMode = new DisplayMode(1280, 720)

    aspectRatio = 16.0d / 9.0d

    fullscreen = false;

    vboUpdatesPerFrame = 2
    fov = 70.0d

    viewingDistanceX = 32
    viewingDistanceZ = 32

}

HUD {

    crosshair = true
    rotatingBlock = true
    placingBox = true

}

Lighting {

    occlusionIntensDefault = 1.0d / 8.0d
    occlusionIntensBillboards = occlusionIntensDefault / 4.0d

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

    defaultSeed = "friztztz"

    sunRiseSetDuration = 0.025d
    dayNightLength = new Long((60 * 1000) * 20) // 20 minutes in ms

    Resources {

        probCoal = -2d
        probGold = -3d
        probSilver = -2.5d
        probRedstone = -3d
        probDiamond = -4d

    }
}
