/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.terasology.game.Terasology;
import org.terasology.game.modes.StateSinglePlayer;

import javax.vecmath.Vector2f;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>,
 * @author Kai Kratz <kaikratz@googlemail.com>
 */
public final class Config {
    private final static Config _instance = new Config();
    private String _worldTitle = "New world";

    private double _forrestGrassDensity = 0.3d;
    private double _plainsGrassDensity = 0.2d;
    private double _snowGrassDensity = 0.001d;
    private double _mountainsGrassDensity = 0.2d;
    private double _desertGrassDensity = 0.001d;

    private long _dayNightLengthInMs = (60l * 1000l) * 30l; // 30 minutes in ms
    private long _initialTimeOffsetInMs = (60l * 1000l); // 60 seconds in ms

    private Vector2f _spawnOrigin = new Vector2f(-24429, 20547);
    private String _defaultSeed = "Blockmania42";

    private boolean _debug = false;
    private boolean _debugCollision = false;
    private boolean _renderChunkBoundingBoxes = false;

    private boolean _demoFlight = false;
    private double _demoFlightSpeed = 0.08d;
    private boolean _godMode = false;

    private int _maxParticles = 256;
    private Vector2f _cloudResolution = new Vector2f(128, 128);
    private int _cloudUpdateInterval = 8000;
    private int _maxThreads = 2;

    private boolean _saveChunks = true;
    private int _chunkCacheSize = 2048;
    private int _maxChunkVBOs = 512;
    private double _gamma = 2.2d;
    private PixelFormat _pixelFormat = new PixelFormat().withDepthBits(24);
    private DisplayMode _displayMode = new DisplayMode(1280, 720);
    private boolean _fullscreen = false;
    private float _fov = 86.0f;

    private int _activeViewingDistanceId = 0;
    private int _viewingDistanceNear = 8;
    private int _viewingDistanceModerate = 16;
    private int _viewingDistanceFar = 26;
    private int _viewingDistanceUltra = 32;

    private boolean _flickeringLight = false;
    private boolean _enablePostProcessingEffects = false;
    private boolean _animatedWaterAndGrass = false;
    private int _verticalChunkMeshSegments = 1;

    private double _mouseSens = 0.075d;

    private boolean _cameraBobbing = true;
    private boolean _renderFirstPersonView = true;
    private boolean _placingBox = true;

    public static Config getInstance() {
        return _instance;
    }

    public String getWorldTitle() {
        return _worldTitle;
    }

    public void setWorldTitle(String _worldTitle) {
        this._worldTitle = _worldTitle;
    }

    public double getForrestGrassDensity() {
        return _forrestGrassDensity;
    }

    public void setForrestGrassDensity(double _forrestGrassDensity) {
        this._forrestGrassDensity = _forrestGrassDensity;
    }

    public double getPlainsGrassDensity() {
        return _plainsGrassDensity;
    }

    public void setPlainsGrassDensity(double _plainsGrassDensity) {
        this._plainsGrassDensity = _plainsGrassDensity;
    }

    public double getSnowGrassDensity() {
        return _snowGrassDensity;
    }

    public void setSnowGrassDensity(double _snowGrassDensity) {
        this._snowGrassDensity = _snowGrassDensity;
    }

    public double getMountainsGrassDensity() {
        return _mountainsGrassDensity;
    }

    public void setMountainsGrassDensity(double _mountainsGrassDensity) {
        this._mountainsGrassDensity = _mountainsGrassDensity;
    }

    public double getDesertGrassDensity() {
        return _desertGrassDensity;
    }

    public void setDesertGrassDensity(double _desertGrassDensity) {
        this._desertGrassDensity = _desertGrassDensity;
    }

    public long getDayNightLengthInMs() {
        return _dayNightLengthInMs;
    }

    public void setDayNightLengthInMs(long _dayNightLengthInMs) {
        this._dayNightLengthInMs = _dayNightLengthInMs;
    }

    public long getInitialTimeOffsetInMs() {
        return _initialTimeOffsetInMs;
    }

    public void setInitialTimeOffsetInMs(long _initialTimeOffsetInMs) {
        this._initialTimeOffsetInMs = _initialTimeOffsetInMs;
    }

    public Vector2f getSpawnOrigin() {
        return _spawnOrigin;
    }

    public void setSpawnOrigin(Vector2f _spawnOrigin) {
        this._spawnOrigin = _spawnOrigin;
    }

    public String getDefaultSeed() {
        return _defaultSeed;
    }

    public void setDefaultSeed(String _defaultSeed) {
        this._defaultSeed = _defaultSeed;
    }

    public boolean isDebug() {
        return _debug;
    }

    public void setDebug(boolean _debug) {
        this._debug = _debug;
    }

    public boolean isDebugCollision() {
        return _debugCollision;
    }

    public void setDebugCollision(boolean _debugCollision) {
        this._debugCollision = _debugCollision;
    }

    public boolean isRenderChunkBoundingBoxes() {
        return _renderChunkBoundingBoxes;
    }

    public void setRenderChunkBoundingBoxes(boolean _renderChunkBoundingBoxes) {
        this._renderChunkBoundingBoxes = _renderChunkBoundingBoxes;
    }

    public boolean isDemoFlight() {
        return _demoFlight;
    }

    public void setDemoFlight(boolean _demoFlight) {
        this._demoFlight = _demoFlight;
    }

    public double getDemoFlightSpeed() {
        return _demoFlightSpeed;
    }

    public void setDemoFlightSpeed(double _demoFlightSpeed) {
        this._demoFlightSpeed = _demoFlightSpeed;
    }

    public boolean isGodMode() {
        return _godMode;
    }

    public void setGodMode(boolean _godMode) {
        this._godMode = _godMode;
    }

    public int getMaxParticles() {
        return _maxParticles;
    }

    public void setMaxParticles(int _maxParticles) {
        this._maxParticles = _maxParticles;
    }

    public Vector2f getCloudResolution() {
        return _cloudResolution;
    }

    public void setCloudResolution(Vector2f _cloudResolution) {
        this._cloudResolution = _cloudResolution;
    }

    public int getCloudUpdateInterval() {
        return _cloudUpdateInterval;
    }

    public void setCloudUpdateInterval(int _cloudUpdateInterval) {
        this._cloudUpdateInterval = _cloudUpdateInterval;
    }

    public int getMaxThreads() {
        return _maxThreads;
    }

    public void setMaxThreads(int _maxThreads) {
        this._maxThreads = _maxThreads;
    }

    public boolean isSaveChunks() {
        return _saveChunks;
    }

    public void setSaveChunks(boolean _saveChunks) {
        this._saveChunks = _saveChunks;
    }

    public int getChunkCacheSize() {
        return _chunkCacheSize;
    }

    public void setChunkCacheSize(int _chunkCacheSize) {
        this._chunkCacheSize = _chunkCacheSize;
    }

    public int getMaxChunkVBOs() {
        return _maxChunkVBOs;
    }

    public void setMaxChunkVBOs(int _maxChunkVBOs) {
        this._maxChunkVBOs = _maxChunkVBOs;
    }

    public double getGamma() {
        return _gamma;
    }

    public void setGamma(double _gamma) {
        this._gamma = _gamma;
    }

    public PixelFormat getPixelFormat() {
        return _pixelFormat;
    }

    public void setPixelFormat(PixelFormat _pixelFormat) {
        this._pixelFormat = _pixelFormat;
    }

    public DisplayMode getDisplayMode() {
        return _displayMode;
    }

    public void setDisplayMode(DisplayMode _displayMode) {
        this._displayMode = _displayMode;
    }

    public boolean isFullscreen() {
        return _fullscreen;
    }

    public void setFullscreen(boolean _fullscreen) {
        this._fullscreen = _fullscreen;
    }

    public int getViewingDistanceNear() {
        return _viewingDistanceNear;
    }

    public void setViewingDistanceNear(int _viewingDistanceNear) {
        this._viewingDistanceNear = _viewingDistanceNear;
    }

    public int getViewingDistanceModerate() {
        return _viewingDistanceModerate;
    }

    public void setViewingDistanceModerate(int _viewingDistanceModerate) {
        this._viewingDistanceModerate = _viewingDistanceModerate;
    }

    public int getViewingDistanceFar() {
        return _viewingDistanceFar;
    }

    public void setViewingDistanceFar(int _viewingDistanceFar) {
        this._viewingDistanceFar = _viewingDistanceFar;
    }

    public int getViewingDistanceUltra() {
        return _viewingDistanceUltra;
    }

    public void setViewingDistanceUltra(int _viewingDistanceUltra) {
        this._viewingDistanceUltra = _viewingDistanceUltra;
    }

    public boolean isFlickeringLight() {
        return _flickeringLight;
    }

    public void setFlickeringLight(boolean _flickeringLight) {
        this._flickeringLight = _flickeringLight;
    }

    public boolean isEnablePostProcessingEffects() {
        return _enablePostProcessingEffects;
    }

    public void setEnablePostProcessingEffects(boolean _enablePostProcessingEffects) {
        this._enablePostProcessingEffects = _enablePostProcessingEffects;
    }

    public boolean isAnimatedWaterAndGrass() {
        return _animatedWaterAndGrass;
    }

    public void setAnimatedWaterAndGrass(boolean _animatedWaterAndGrass) {
        this._animatedWaterAndGrass = _animatedWaterAndGrass;
    }

    public int getVerticalChunkMeshSegments() {
        return _verticalChunkMeshSegments;
    }

    public void setVerticalChunkMeshSegments(int _verticalChunkMeshSegments) {
        this._verticalChunkMeshSegments = _verticalChunkMeshSegments;
    }

    public double getMouseSens() {
        return _mouseSens;
    }

    public void setMouseSens(double _mouseSens) {
        this._mouseSens = _mouseSens;
    }

    public float getFov() {
        return _fov;
    }

    public void setFov(float _fov) {
        this._fov = _fov;
    }

    public boolean isCameraBobbing() {
        return _cameraBobbing;
    }

    public void setCameraBobbing(boolean _cameraBobbing) {
        this._cameraBobbing = _cameraBobbing;
    }

    public boolean isRenderFirstPersonView() {
        return _renderFirstPersonView;
    }

    public void setRenderFirstPersonView(boolean _renderFirstPersonView) {
        this._renderFirstPersonView = _renderFirstPersonView;
    }

    public boolean isPlacingBox() {
        return _placingBox;
    }

    public void setPlacingBox(boolean _placingBox) {
        this._placingBox = _placingBox;
    }

    private Config() {
        loadLastConfig();
    }

    private void loadLastConfig() {
    }

    public void loadConfig(String filename) {
    }

    /* SPECIAL STUFF */
    public int getActiveViewingDistance() {
        if (_activeViewingDistanceId == 1)
            return getViewingDistanceModerate();
        else if (_activeViewingDistanceId == 2)
            return getViewingDistanceFar();
        else if (_activeViewingDistanceId == 3)
            return getViewingDistanceUltra();

        return getViewingDistanceNear();
    }

    public int getActiveViewingDistanceId() {
        return _activeViewingDistanceId;
    }

    public void setViewingDistanceById(int _viewingDistance) {
        this._activeViewingDistanceId = _viewingDistance;

        // Make sure to update the chunks "around" the player
        if (Terasology.getInstance().getCurrentGameState() instanceof StateSinglePlayer)
            Terasology.getInstance().getActiveWorldRenderer().updateChunksInProximity(true);
    }

    public void setGraphicsQuality(int qualityLevel) {
        if (qualityLevel == 0) {
            setEnablePostProcessingEffects(false);
            setAnimatedWaterAndGrass(false);
            setFlickeringLight(false);
        } else if (qualityLevel == 1) {
            setEnablePostProcessingEffects(true);
            setAnimatedWaterAndGrass(false);
            setFlickeringLight(true);
        } else if (qualityLevel == 2) {
            setEnablePostProcessingEffects(true);
            setAnimatedWaterAndGrass(true);
            setFlickeringLight(true);
        }

        ShaderManager.getInstance().recompileAllShaders();
    }
}



