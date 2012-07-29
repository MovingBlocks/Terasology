/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.manager;

import com.google.protobuf.TextFormat;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.terasology.game.CoreRegistry;
import org.terasology.protobuf.Configuration;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector2f;
import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>,
 * @author Kai Kratz <kaikratz@googlemail.com>
 */
public final class Config {
    private Logger logger = Logger.getLogger(getClass().getName());
    private final static Config _instance = new Config();
    private Configuration.Setting.Builder _setting;

    public static Config getInstance() {
        return _instance;
    }

    private Config() {
        if (!loadLastConfig()) {
            loadDefaultConfig();
        }
    }

    private boolean loadLastConfig() {
        return loadConfig(new File(PathManager.getInstance().getWorldPath(), "last.cfg"));
    }

    private void loadDefaultConfig() {
        _setting = Configuration.Setting.newBuilder();
    }

    public boolean loadConfig(File file) {


        Configuration.Setting.Builder setting = Configuration.Setting.newBuilder();
        if (file.exists()) {
            logger.log(Level.INFO, "Using config file: " + file);
            try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                TextFormat.merge(isr, setting);
                isr.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not load config file " + file, e);
                return false;
            }
        }
        _setting = setting;
        return true;
    }

    public void saveConfig(File file) {
        try {
            logger.log(Level.INFO, "Using config file: " + file);
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            TextFormat.print(_setting.build(), osw);
            osw.close();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not write " + file, e);
        }
    }

    /**
     * Get / Set methods *
     */
    public String getWorldTitle() {
        return _setting.getWorldBuilder().getWorldTitle();
    }

    public void setWorldTitle(String _worldTitle) {
        _setting.getWorldBuilder().setWorldTitle(_worldTitle);
    }

    public float getForestGrassDensity() {
        return _setting.getWorldBuilder().getForestGrassDensity();
    }

    public void setForestGrassDensity(float _forrestGrassDensity) {
        _setting.getWorldBuilder().setForestGrassDensity(_forrestGrassDensity);
    }

    public float getPlainsGrassDensity() {
        return _setting.getWorldBuilder().getPlainsGrassDensity();
    }

    public void setPlainsGrassDensity(float plainsGrassDensity) {
        _setting.getWorldBuilder().setPlainsGrassDensity(plainsGrassDensity);
    }

    public float getSnowGrassDensity() {
        return _setting.getWorldBuilder().getSnowGrassDensity();
    }

    public void setSnowGrassDensity(float snowGrassDensity) {
        _setting.getWorldBuilder().setSnowGrassDensity(snowGrassDensity);
    }

    public float getMountainGrassDensity() {
        return _setting.getWorldBuilder().getMountainGrassDensity();
    }

    public void setMountainGrassDensity(float mountainsGrassDensity) {
        _setting.getWorldBuilder().setMountainGrassDensity(mountainsGrassDensity);
    }

    public float getDesertGrassDensity() {
        return _setting.getWorldBuilder().getDesertGrassDensity();
    }

    public void setDesertGrassDensity(float desertGrassDensity) {
        _setting.getWorldBuilder().setDesertGrassDensity(desertGrassDensity);
    }

    public long getDayNightLengthInMs() {
        return _setting.getWorldBuilder().getDayNightLengthInMs();
    }

    public void setDayNightLengthInMs(long dayNightLengthInMs) {
        _setting.getWorldBuilder().setDayNightLengthInMs(dayNightLengthInMs);
    }

    public long getInitialTimeOffsetInMs() {
        return _setting.getWorldBuilder().getInitialTimeOffsetInMs();
    }

    public void setInitialTimeOffsetInMs(long initialTimeOffsetInMs) {
        _setting.getWorldBuilder().setInitialTimeOffsetInMs(initialTimeOffsetInMs);
    }

    public Vector2f getSpawnOrigin() {
        float x = _setting.getWorldBuilder().getSpawnOriginBuilder().getX();
        float y = _setting.getWorldBuilder().getSpawnOriginBuilder().getX();
        return new Vector2f(x, y);
    }

    public void setSpawnOrigin(Vector2f spawnOrigin) {
        _setting.getWorldBuilder().getSpawnOriginBuilder().setX(spawnOrigin.x);
        _setting.getWorldBuilder().getSpawnOriginBuilder().setY(spawnOrigin.y);
    }

    public String getDefaultSeed() {
        return _setting.getWorldBuilder().getDefaultSeed();
    }

    public void setDefaultSeed(String defaultSeed) {
        _setting.getWorldBuilder().setDefaultSeed(defaultSeed);
    }

    public boolean isDebug() {
        return _setting.getDebugInfoBuilder().getDebug();
    }

    public void setDebug(boolean debug) {
        _setting.getDebugInfoBuilder().setDebug(debug);
    }

    public boolean isDebugCollision() {
        return _setting.getDebugInfoBuilder().getDebugCollision();
    }

    public void setDebugCollision(boolean debugCollision) {
        _setting.getDebugInfoBuilder().setDebugCollision(debugCollision);
    }

    public boolean isRenderChunkBoundingBoxes() {
        return _setting.getDebugInfoBuilder().getRenderChunkBoundingBoxes();
    }

    public void setRenderChunkBoundingBoxes(boolean renderChunkBoundingBoxes) {
        _setting.getDebugInfoBuilder().setRenderChunkBoundingBoxes(renderChunkBoundingBoxes);
    }

    public boolean isDemoFlight() {
        return _setting.getDebugInfoBuilder().getDemoFlight();
    }

    public void setDemoFlight(boolean demoFlight) {
        _setting.getDebugInfoBuilder().setDemoFlight(demoFlight);
    }

    public double getDemoFlightSpeed() {
        return _setting.getDebugInfoBuilder().getDemoFlightSpeed();
    }

    public void setDemoFlightSpeed(float demoFlightSpeed) {
        _setting.getDebugInfoBuilder().setDemoFlightSpeed(demoFlightSpeed);
    }

    public int getMaxParticles() {
        return _setting.getSystemBuilder().getMaxParticles();
    }

    public void setMaxParticles(int maxParticles) {
        _setting.getSystemBuilder().setMaxParticles(maxParticles);
    }

    public Vector2f getCloudResolution() {
        float x = _setting.getSystemBuilder().getCloudResolutionBuilder().getX();
        float y = _setting.getSystemBuilder().getCloudResolutionBuilder().getY();
        return new Vector2f(x, y);
    }

    public void setCloudResolution(Vector2f cloudResolution) {
        _setting.getSystemBuilder().getCloudResolutionBuilder().setX(cloudResolution.x);
        _setting.getSystemBuilder().getCloudResolutionBuilder().setX(cloudResolution.y);
    }

    public int getCloudUpdateInterval() {
        return _setting.getSystemBuilder().getCloudUpdateInterval();
    }

    public void setCloudUpdateInterval(int cloudUpdateInterval) {
        _setting.getSystemBuilder().setCloudUpdateInterval(cloudUpdateInterval);
    }

    public int getMaxThreads() {
        return _setting.getSystemBuilder().getMaxThreads();
    }

    public void setMaxThreads(int maxThreads) {
        _setting.getSystemBuilder().setMaxThreads(maxThreads);
    }

    public boolean isSaveChunks() {
        return _setting.getSystemBuilder().getSaveChunks();
    }

    public void setSaveChunks(boolean saveChunks) {
        _setting.getSystemBuilder().setSaveChunks(saveChunks);
    }

    public int getChunkCacheSize() {
        return _setting.getSystemBuilder().getChunkCacheSize();
    }

    public void setChunkCacheSize(int chunkCacheSize) {
        _setting.getSystemBuilder().setChunkCacheSize(chunkCacheSize);
    }

    public int getMaxChunkVBOs() {
        return _setting.getSystemBuilder().getMaxChunkVBOs();
    }

    public void setMaxChunkVBOs(int maxChunkVBOs) {
        _setting.getSystemBuilder().setMaxChunkVBOs(maxChunkVBOs);
    }

    public double getGamma() {
        return _setting.getSystemBuilder().getGamma();
    }

    public void setGamma(float gamma) {
        _setting.getSystemBuilder().setGamma(gamma);
    }

    public PixelFormat getPixelFormat() {
        int bits = _setting.getSystemBuilder().getPixelFormat();
        return new PixelFormat().withDepthBits(bits);
    }

    public void setPixelFormat(PixelFormat pixelFormat) {
        _setting.getSystemBuilder().setPixelFormat(pixelFormat.getBitsPerPixel());
    }

    public DisplayMode getDisplayMode() {
        int width = _setting.getSystemBuilder().getDisplayModeBuilder().getWidth();
        int height = _setting.getSystemBuilder().getDisplayModeBuilder().getHeight();
        return new DisplayMode(width, height);
    }

    public void setDisplayMode(DisplayMode displayMode) {
        _setting.getSystemBuilder().getDisplayModeBuilder().setWidth(displayMode.getWidth());
        _setting.getSystemBuilder().getDisplayModeBuilder().setHeight(displayMode.getHeight());
    }

    public boolean isFullscreen() {
        return _setting.getSystemBuilder().getFullscreen();
    }

    public void setFullscreen(boolean fullscreen) {
        _setting.getSystemBuilder().setFullscreen(fullscreen);
    }

    public int getViewingDistanceNear() {
        return _setting.getSystemBuilder().getViewingDistanceNear();
    }

    public void setViewingDistanceNear(int viewingDistanceNear) {
        _setting.getSystemBuilder().setViewingDistanceNear(viewingDistanceNear);
    }

    public int getViewingDistanceModerate() {
        return _setting.getSystemBuilder().getViewingDistanceModerate();
    }

    public void setViewingDistanceModerate(int viewingDistanceModerate) {
        _setting.getSystemBuilder().setViewingDistanceModerate(viewingDistanceModerate);
    }

    public int getViewingDistanceFar() {
        return _setting.getSystemBuilder().getViewingDistanceFar();
    }

    public void setViewingDistanceFar(int viewingDistanceFar) {
        _setting.getSystemBuilder().setViewingDistanceFar(viewingDistanceFar);
    }

    public int getViewingDistanceUltra() {
        return _setting.getSystemBuilder().getViewingDistanceUltra();
    }

    public void setViewingDistanceUltra(int viewingDistanceUltra) {
        _setting.getSystemBuilder().setViewingDistanceUltra(viewingDistanceUltra);
    }

    public boolean isFlickeringLight() {
        return _setting.getSystemBuilder().getFlickeringLight();
    }

    public void setFlickeringLight(boolean flickeringLight) {
        _setting.getSystemBuilder().setFlickeringLight(flickeringLight);
    }

    public boolean isEnablePostProcessingEffects() {
        return _setting.getSystemBuilder().getEnablePostProcessingEffects();
    }

    public void setEnablePostProcessingEffects(boolean enablePostProcessingEffects) {
        _setting.getSystemBuilder().setEnablePostProcessingEffects(enablePostProcessingEffects);
    }

    public boolean isAnimatedGrass() {
        return _setting.getSystemBuilder().getAnimatedGrass();
    }

    public void setAnimatedGrass(boolean animatedGrass) {
        _setting.getSystemBuilder().setAnimatedGrass(animatedGrass);
    }

    public int getVerticalChunkMeshSegments() {
        return _setting.getSystemBuilder().getVerticalChunkMeshSegments();
    }

    public void setVerticalChunkMeshSegments(int verticalChunkMeshSegments) {
        _setting.getSystemBuilder().setVerticalChunkMeshSegments(verticalChunkMeshSegments);
    }

    public double getMouseSens() {
        return _setting.getPlayerBuilder().getMouseSens();
    }

    public void setMouseSens(float mouseSens) {
        _setting.getPlayerBuilder().setMouseSens(mouseSens);
    }

    public float getFov() {
        return _setting.getPlayerBuilder().getFov();
    }

    public void setFov(float fov) {
        _setting.getPlayerBuilder().setFov(fov);
    }

    public boolean isCameraBobbing() {
        return _setting.getPlayerBuilder().getCameraBobbing();
    }

    public void setCameraBobbing(boolean cameraBobbing) {
        _setting.getPlayerBuilder().setCameraBobbing(cameraBobbing);
    }

    public boolean isRenderFirstPersonView() {
        return _setting.getPlayerBuilder().getRenderFirstPersonView();
    }

    public void setRenderFirstPersonView(boolean renderFirstPersonView) {
        _setting.getPlayerBuilder().setRenderFirstPersonView(renderFirstPersonView);
    }

    public boolean isPlacingBox() {
        return _setting.getPlayerBuilder().getPlacingBox();
    }

    public void setPlacingBox(boolean placingBox) {
        _setting.getPlayerBuilder().setPlacingBox(placingBox);
    }

    public int getBlurIntensity() {
        return _setting.getSystemBuilder().getBlurIntensity();
    }

    public boolean isComplexWater() {
        return _setting.getSystemBuilder().getReflectiveWater();
    }

    public void setComplexWater(boolean reflectwater) {
        _setting.getSystemBuilder().setReflectiveWater(reflectwater);
    }
    
    public int getMusicVolume() {
        return _setting.getSystemBuilder().getMusicVolume();
    }

    public void setMusicVolume(int volume) {
        _setting.getSystemBuilder().setMusicVolume(volume);
    }
    
    public int getSoundVolume() {
        return _setting.getSystemBuilder().getSoundVolume();
    }

    public void setSoundVolume(int volume) {
        _setting.getSystemBuilder().setSoundVolume(volume);
    }

    /* MODS */

    public List<String> getActiveMods() {
        return _setting.getActiveModList();
    }

    public void setActiveMods(List<String> activeMods) {
        _setting.getActiveModList().clear();
        _setting.getActiveModList().addAll(activeMods);
    }

    /* SPECIAL STUFF */
    public int getActiveViewingDistance() {
        if (_setting.getSystemBuilder().getActiveViewingDistanceId() == 1)
            return getViewingDistanceModerate();
        else if (_setting.getSystemBuilder().getActiveViewingDistanceId() == 2)
            return getViewingDistanceFar();
        else if (_setting.getSystemBuilder().getActiveViewingDistanceId() == 3)
            return getViewingDistanceUltra();

        return getViewingDistanceNear();
    }

    public int getActiveViewingDistanceId() {
        return _setting.getSystemBuilder().getActiveViewingDistanceId();
    }

    //todo remove this from the config
    public void setViewingDistanceById(int viewingDistance) {
        _setting.getSystemBuilder().setActiveViewingDistanceId(viewingDistance);

        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);

        // Make sure to update the chunks "around" the player
        if (worldRenderer != null)
            worldRenderer.changeViewDistance(getActiveViewingDistance());
    }

    public void setBlurIntensity(int blurlevel) {
        _setting.getSystemBuilder().setBlurIntensity(blurlevel);
        switch (blurlevel) {
            case 0:
                return; //off
            case 1:
                return; //some
            case 2:
                return; //normal
            case 3:
                return; // max
            default:
                return; // normal?
        }
    }
}



