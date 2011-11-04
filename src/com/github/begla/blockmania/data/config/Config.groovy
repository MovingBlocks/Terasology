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
    aspectRatio = (float) (16.0f / 9.0f)
    fullscreen = false;

    vboUpdatesPerFrame = 2
    fov = (float) 70.0f

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
    jumpIntensity = 0.125d
    maxGravity = 0.7d
    maxGravitySwimming = 0.01d
    gravity = 0.006d
    gravitySwimming = gravity * 2;
    walkingSpeed = 0.03d
    runningFactor = 1.8d
    friction = 0.08d

}

World {

    defaultSeed = "friztztz"
    sunRiseSetDuration = 0.025d

    Resources {

        probCoal = -2d
        probGold = -3d
        probSilver = -2.5d
        probRedstone = -3d
        probDiamond = -4d;

    }
}
