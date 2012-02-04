// Temp cheat internal version of the file in the external Groovy dir
// TODO: Clean up approach

package org.terasology.logic.manager

import javax.vecmath.Vector2f
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.PixelFormat

public class DefaultConfig {

    ConfigObject config = new ConfigObject()

    public DefaultConfig() {
        config.put("System.versionTag", "Pre Alpha")
        config.put("System.maxParticles", 256)
        config.put("System.cloudResolution", new Vector2f(64, 64))
        config.put("System.cloudUpdateInterval", (Integer) 1000)
        config.put("System.maxThreads", 2)
        config.put("System.saveChunks", true)
        config.put("System.chunkCacheSize", 2048)
        config.put("System.Debug.debug", false)
        config.put("System.Debug.debugCollision", false)
        config.put("System.Debug.renderChunkBoundingBoxes", false)
        config.put("System.Debug.demoFlight", false)
        config.put("System.Debug.godMode", false)

        config.put("Graphics.gamma", 2.2d)
        config.put("Graphics.animatedWaterAndGrass", true)
        config.put("Graphics.pixelFormat", new PixelFormat().withDepthBits(24))
        config.put("Graphics.displayMode", new DisplayMode(1280, 720))
        config.put("Graphics.aspectRatio", 16.0d / 9.0d)
        config.put("Graphics.fullscreen", false)

        config.put("Graphics.viewingDistanceNear", 8)
        config.put("Graphics.viewingDistanceModerate", 16)
        config.put("Graphics.viewingDistanceFar", 26)
        config.put("Graphics.viewingDistanceUltra", 32)

        // Splits chunk meshes into multiple sub-meshes to support frustum and occlusion culling techniques
        config.put("Graphics.verticalChunkMeshSegments", 1)

        config.put("HUD.crosshair", true)
        config.put("HUD.placingBox", true)

        config.put("Controls.mouseSens", 0.075d)

        config.put("Player.fov", 80.0d)
        config.put("Player.cameraBobbing", true)
        config.put("Player.maxGravity", 1.0d)
        config.put("Player.maxGravitySwimming", 0.04d)
        config.put("Player.gravity", 0.008d)
        config.put("Player.gravitySwimming", 0.008d * 4d)
        config.put("Player.friction", 0.08d)
        config.put("Player.walkingSpeed", 0.03d)
        config.put("Player.runningFactor", 1.5d)
        config.put("Player.jumpIntensity", 0.16d)

        config.put("World.spawnOrigin", new Vector2f(-24429, 20547))
        config.put("World.defaultSeed", "Blockmania42")
        config.put("World.dayNightLengthInMs", new Long((60 * 1000) * 20)) // 20 minutes in ms
        config.put("World.initialTimeOffsetInMs", new Long(60 * 1000)) // 120 seconds in ms

        config.put("World.Biomes.Forest.grassDensity", 0.3d)
        config.put("World.Biomes.Plains.grassDensity", 0.1d)
        config.put("World.Biomes.Snow.grassDensity", 0.001d)
        config.put("World.Biomes.Mountains.grassDensity", 0.2d)
        config.put("World.Biomes.Desert.grassDensity", 0.001d)
    }

    public ConfigObject getConfig() {
        return config
    }
}