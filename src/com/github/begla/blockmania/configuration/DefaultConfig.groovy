// Temp cheat internal version of the file in the external Groovy dir

package com.github.begla.blockmania.configuration

import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.PixelFormat
import javax.vecmath.Vector2f

public class DefaultConfig {

    ConfigObject config = new ConfigObject()

    public DefaultConfig() {

        System.out.println("Yay")

        config.put("System.gameTitle", "Blckmania Pre Alpha")
        config.put("System.maxChunkUpdatesPerIteration", 1)
        config.put("System.maxParticles", 128)
        config.put("System.cloudResolution", new Vector2f(64, 64))
        config.put("System.cloudUpdateInterval", (Integer) 1000)
        config.put("System.maxThreads", Runtime.getRuntime().availableProcessors() <= 2 ? 1 : 2)
        config.put("System.saveChunks", true)
        config.put("System.chunkCacheSize", 1024)
        config.put("System.Debug.debug", false)
        config.put("System.Debug.debugCollision", false)
        config.put("System.Debug.chunkOutlines", false)
        config.put("System.Debug.demoFlight", false)
        config.put("System.Debug.godMode", false)

        System.out.println("Wee")

        config.put("Graphics.gamma", 2.2d)
        config.put("Graphics.animatedWaterAndGrass", true)
        config.put("Graphics.pixelFormat", new PixelFormat().withDepthBits(24))
        config.put("Graphics.displayMode", new DisplayMode(1280, 720))
        config.put("Graphics.aspectRatio", 16.0d / 9.0d)
        config.put("Graphics.fullscreen", false)
        config.put("Graphics.fov", 64.0d)
        config.put("Graphics.viewingDistanceNear", 8)
        config.put("Graphics.viewingDistanceModerate", 16)
        config.put("Graphics.viewingDistanceFar", 24)
        config.put("Graphics.viewingDistanceUltra", 28)

        config.put("HUD.crosshair", true)
        config.put("HUD.rotatingBlock", true)
        config.put("HUD.placingBox", true)

        config.put("Lighting.occlusionIntensDefault", 1.0d / 7.0d)
        config.put("Lighting.occlusionIntensBillboards", (1.0d / 7.0d) / 3.0d)

        config.put("Controls.mouseSens", 0.075d)

        config.put("Player.bobbing", true)
        config.put("Player.maxGravity", 0.7d)
        config.put("Player.maxGravitySwimming", 0.01d)
        config.put("Player.gravity", 0.006d)
        config.put("Player.gravitySwimming", 0.006d * 2d)
        config.put("Player.friction", 0.08d)
        config.put("Player.walkingSpeed", 0.03d)
        config.put("Player.runningFactor", 1.8d)
        config.put("Player.jumpIntensity", 0.125d)

        config.put("World.spawnOrigin", new Vector2f(-24429, 20547))
        config.put("World.defaultSeed", "nXhTnOmGgLsZmWhO")
        config.put("World.dayNightLengthInMs", new Long((60 * 1000) * 20))
        config.put("World.initialTimeOffsetInMs", new Long(60 * 1000))
        config.put("World.Biomes.Forest.grassDensity", 0.3d)
        config.put("World.Biomes.Plains.grassDensity", 0.1d)
        config.put("World.Biomes.Snow.grassDensity", 0.001d)
        config.put("World.Biomes.Mountains.grassDensity",0.2d)
        config.put("World.Biomes.Desert.grassDensity",0.001d)
        config.put("World.Resources.probCoal",-2d)
        config.put("World.Resources.probIron",-2.5d)
        config.put("World.Resources.probCopper",-3d)
        config.put("World.Resources.probGold",-3d)
        config.put("World.Resources.probDiamond",-4d)
    }

    public ConfigObject getConfig() {
        return config
    }
}