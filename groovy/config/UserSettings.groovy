import javax.vecmath.Vector2f
import org.lwjgl.opengl.PixelFormat
import org.lwjgl.opengl.DisplayMode

Game{
    Graphics{
        maxParticles = 256
        cloudResolution = new Vector2f(128, 128)
        cloudUpdateInterval = (Integer) 8000
        maxThreads = 2
        saveChunks = true
        chunkCacheSize = 2048
        maxChunkVBOs = 512
        gamma = 2.2d
        pixelFormat = new PixelFormat().withDepthBits(24)
        displayMode = new DisplayMode(1280, 720)
        fullscreen = false;
        viewingDistanceNear = 8
        viewingDistanceModerate = 16
        viewingDistanceFar = 26
        viewingDistanceUltra = 32
        enablePostProcessingEffects = true
        animatedWaterAndGrass = true
        verticalChunkMeshSegments = 1
    }
    Controls{
        mouseSens = 0.075d
    }
    Player {
        fov = 86.0d
        cameraBobbing = true
        renderFirstPersonView = true
    }
    HUD{
        placingBox = true
    }
}