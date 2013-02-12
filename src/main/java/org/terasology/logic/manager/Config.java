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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.game.CoreRegistry;
import org.terasology.protobuf.Configuration;
import org.terasology.rendering.world.WorldRenderer;

import com.google.protobuf.TextFormat;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>,
 * @author Kai Kratz <kaikratz@googlemail.com>
 */
public final class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
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
            logger.debug("Using config file: {}", file);
            try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                TextFormat.merge(isr, setting);
                isr.close();
            } catch (Exception e) {
                logger.error("Could not load config file {}", file, e);
                return false;
            }
        }
        _setting = setting;
        return true;
    }

    public void saveConfig(File file) {
        try {
            logger.debug("Using config file: {}", file);
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            TextFormat.print(_setting.build(), osw);
            osw.close();
        } catch (Exception e) {
            logger.error("Could not write {}", file, e);
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
    
    public List<String> getChunkGenerator() {
        return _setting.getWorldBuilder().getChunkGeneratorList();
    }

    public void setChunkGenerator(String[] list) {
    	_setting.getWorldBuilder().clearChunkGenerator();
        for (int i = 0; i < list.length; i++) {
        	_setting.getWorldBuilder().addChunkGenerator(list[i]);
		}
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

    public boolean isAnimatedGrass() {
        return _setting.getSystemBuilder().getAnimatedGrass();
    }

    public void setAnimatedGrass(boolean animatedGrass) {
        _setting.getSystemBuilder().setAnimatedGrass(animatedGrass);
    }

    public boolean isAnimatedWater() {
        return _setting.getSystemBuilder().getAnimatedWater();
    }

    public void setAnimatedWater(boolean animatedWater) {
        _setting.getSystemBuilder().setAnimatedWater(animatedWater);
    }

    public boolean isRefractiveWater() {
        return _setting.getSystemBuilder().getRefractiveWater();
    }

    public void setRefractiveWater(boolean refr) {
        _setting.getSystemBuilder().setRefractiveWater(refr);
    }

    public int getOceanOctaves() {
        return _setting.getSystemBuilder().getOceanOctaves();
    }

    public void setOceanOctaves(int octaves) {
        _setting.getSystemBuilder().setOceanOctaves(octaves);
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

    public int getBlurRadius() {
        return Math.max(getBlurIntensity(), 1);
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

    public void setVignette(boolean vignette) {
        _setting.getSystemBuilder().setVignette(vignette);
    }

    public boolean isVignette() {
        return _setting.getSystemBuilder().getVignette();
    }

    public void setMotionBlur(boolean motionBlur) {
        _setting.getSystemBuilder().setMotionBlur(motionBlur);
    }

    public boolean isMotionBlur() {
        return _setting.getSystemBuilder().getMotionBlur();
    }

    public void setSSAO(boolean ssao) {
        _setting.getSystemBuilder().setSsao(ssao);
    }

    public boolean isSSAO() {
        return _setting.getSystemBuilder().getSsao();
    }

    public void setLightShafts(boolean lightShafts) {
        _setting.getSystemBuilder().setLightShafts(lightShafts);
    }

    public boolean isLightShafts() {
        return _setting.getSystemBuilder().getLightShafts();
    }

    public void setOutline(boolean outline) {
        _setting.getSystemBuilder().setOutline(outline);
    }

    public boolean isOutline() {
        return _setting.getSystemBuilder().getOutline();
    }

    public void setFilmGrain(boolean filmGrain) {
        _setting.getSystemBuilder().setFilmGrain(filmGrain);
    }

    public boolean isFilmGrain() {
        return _setting.getSystemBuilder().getFilmGrain();
    }

    public void setEyeAdaption(boolean eyeAdaption) {
        _setting.getSystemBuilder().setEyeAdapation(eyeAdaption);
    }

    public boolean isEyeAdaption() {
        return _setting.getSystemBuilder().getEyeAdapation();
    }

    public void setBloom(boolean bloom) {
        _setting.getSystemBuilder().setBloom(bloom);
    }

    public boolean isBloom() {
        return _setting.getSystemBuilder().getBloom();
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

    public void setViewingDistanceById(int viewingDistance) {
        _setting.getSystemBuilder().setActiveViewingDistanceId(viewingDistance);

        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);

        int chunksToLoad = getActiveViewingDistance() * getActiveViewingDistance();
        setMaxChunkVBOs(chunksToLoad >= 512 ? 512 : chunksToLoad);

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



