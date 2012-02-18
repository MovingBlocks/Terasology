// Temp cheat internal version of the file in the external Groovy dir
// TODO: Clean up approach

package org.terasology.logic.manager

import javax.vecmath.Vector2f
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.PixelFormat

public class DefaultConfig {

    ConfigObject config = new ConfigObject()

    public DefaultConfig() {

        // The version tag
        config.put("System.versionTag", "Pre Alpha")
        // Maximum amount of rendered particles
        config.put("System.maxParticles", 256)
        // Resolution of the textures used to render the clouds
        config.put("System.cloudResolution", new Vector2f(128, 128))
        // Time between cloud updates
        config.put("System.cloudUpdateInterval", (Integer) 8000)
        // Maximum amount of concurrently running chunk update threads
        config.put("System.maxThreads", 2)
        // If set to true chunks get persisted to disk when removed from the chunk cache
        config.put("System.saveChunks", true)
        // Maximum amount of chunks stored in the chunk cache
        // ~2048 is a good default value when using the largest viewing distance of 32 chunks
        config.put("System.chunkCacheSize", 2048)
        // Maximum amount of chunk VBOs kept in video memory
        // 512 chunks is a good default value for GPUs with ~1024 MB video memory using the largest viewing distance
        config.put("System.maxChunkVBOs", 512)

        config.put("System.Debug.debug", false)
        config.put("System.Debug.debugCollision", false)
        config.put("System.Debug.renderChunkBoundingBoxes", false)
        config.put("System.Debug.demoFlight", false)
        config.put("System.Debug.demoFlightSpeed", 0.08d)
        config.put("System.Debug.godMode", false)

        config.put("Graphics.gamma", 2.2d)
        config.put("Graphics.pixelFormat", new PixelFormat().withDepthBits(24))
        config.put("Graphics.displayMode", new DisplayMode(1280, 720))
        config.put("Graphics.fullscreen", false)

        config.put("Graphics.viewingDistanceNear", 8)
        config.put("Graphics.viewingDistanceModerate", 16)
        config.put("Graphics.viewingDistanceFar", 26)
        config.put("Graphics.viewingDistanceUltra", 32)

        // Advanced effects – Disable them if you are having problems running the game!
//        config.put("Graphics.enablePostProcessingEffects", true)
        config.put("Graphics.animatedWaterAndGrass", true)

        // Splits chunk meshes into multiple sub-meshes to support frustum and occlusion culling techniques
        config.put("Graphics.verticalChunkMeshSegments", 1)

        config.put("HUD.placingBox", true)
        config.put("Player.renderFirstPersonView", true)

        config.put("Controls.mouseSens", 0.075d)

        config.put("Player.fov", 86.0d)
        config.put("Player.cameraBobbing", true)
        config.put("Player.maxGravity", 1.0d)
        config.put("Player.maxGravitySwimming", 0.04d)
        config.put("Player.gravity", 0.008d)
        config.put("Player.gravitySwimming", 0.008d * 4d)
        config.put("Player.friction", 0.15d)
        config.put("Player.walkingSpeed", 0.025d)
        config.put("Player.runningFactor", 1.5d)
        config.put("Player.jumpIntensity", 0.16d)

        config.put("World.spawnOrigin", new Vector2f(-24429, 20547))
        config.put("World.defaultSeed", "Blockmania42")
        config.put("World.dayNightLengthInMs", new Long((60 * 1000) * 30)) // 30 minutes in ms
        config.put("World.initialTimeOffsetInMs", new Long(60 * 1000)) // 60 seconds in ms

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