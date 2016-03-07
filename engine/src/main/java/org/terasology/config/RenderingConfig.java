/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.config;

import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.terasology.rendering.cameras.PerspectiveCameraSettings;
import org.terasology.rendering.nui.layers.mainMenu.videoSettings.ScreenshotSize;
import org.terasology.rendering.world.viewDistance.ViewDistance;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 */
public class RenderingConfig {
    public static final String VIEW_DISTANCE = "viewDistance";

    private PixelFormat pixelFormat;
    private int windowPosX;
    private int windowPosY;
    private int windowWidth;
    private int windowHeight;
    private boolean fullscreen;
    private ViewDistance viewDistance;
    private boolean flickeringLight;
    private boolean animateGrass;
    private boolean animateWater;
    private float fieldOfView;
    private boolean cameraBobbing;
    private boolean renderPlacingBox;
    private int blurIntensity;
    private boolean reflectiveWater;
    private boolean vignette;
    private boolean motionBlur;
    private boolean ssao;
    private boolean filmGrain;
    private boolean outline;
    private boolean lightShafts;
    private boolean eyeAdaptation;
    private boolean bloom;
    private boolean dynamicShadows;
    private boolean oculusVrSupport;
    private int maxTextureAtlasResolution;
    private int maxChunksUsedForShadowMapping;
    private int shadowMapResolution;
    private boolean normalMapping;
    private boolean parallaxMapping;
    private boolean dynamicShadowsPcfFiltering;
    private boolean cloudShadows;
    private boolean renderNearest;
    private int particleEffectLimit;
    private int frameLimit;
    private int meshLimit;
    private boolean inscattering;
    private boolean localReflections;
    private boolean vSync;
    private boolean clampLighting;
    private int fboScale;
    private boolean dumpShaders;
    private boolean volumetricFog;
    private ScreenshotSize screenshotSize;
    private String screenshotFormat;
    private PerspectiveCameraSettings cameraSettings;

    private RenderingDebugConfig debug = new RenderingDebugConfig();

    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    public PerspectiveCameraSettings getCameraSettings() {
        return cameraSettings;
    }

    public PixelFormat getPixelFormat() {
        return pixelFormat;
    }

    public void setPixelFormat(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    public int getWindowPosX() {
        return windowPosX;
    }

    public void setWindowPosX(int posX) {
        this.windowPosX = posX;
    }

    public int getWindowPosY() {
        return windowPosY;
    }

    public void setWindowPosY(int posY) {
        this.windowPosY = posY;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public DisplayMode getDisplayMode() {
        return new DisplayMode(windowWidth, windowHeight);
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public ViewDistance getViewDistance() {
        return viewDistance;
    }

    /**
     * Sets the view distance and notifies the property change listeners registered via
     * {@link RenderingConfig#subscribe(PropertyChangeListener)} that listen for the property {@link #VIEW_DISTANCE}.
     * @param viewDistance the new view distance
     */
    public void setViewDistance(ViewDistance viewDistance) {
        ViewDistance oldValue = this.viewDistance;
        this.viewDistance = viewDistance;
        propertyChangeSupport.firePropertyChange(VIEW_DISTANCE, oldValue, viewDistance);
    }

    public boolean isFlickeringLight() {
        return flickeringLight;
    }

    public void setFlickeringLight(boolean flickeringLight) {
        this.flickeringLight = flickeringLight;
    }

    public boolean isAnimateGrass() {
        return animateGrass;
    }

    public void setAnimateGrass(boolean animateGrass) {
        this.animateGrass = animateGrass;
    }

    public boolean isAnimateWater() {
        return animateWater;
    }

    public void setAnimateWater(boolean animateWater) {
        this.animateWater = animateWater;
    }

    public boolean isDynamicShadows() {
        return dynamicShadows;
    }

    public void setDynamicShadows(boolean dynamicShadows) {
        this.dynamicShadows = dynamicShadows;
    }

    public float getFieldOfView() {
        return fieldOfView;
    }

    public void setFieldOfView(float fieldOfView) {
        this.fieldOfView = fieldOfView;
    }

    public boolean isCameraBobbing() {
        return cameraBobbing;
    }

    public void setCameraBobbing(boolean cameraBobbing) {
        this.cameraBobbing = cameraBobbing;
    }

    public boolean isRenderPlacingBox() {
        return renderPlacingBox;
    }

    public void setRenderPlacingBox(boolean renderPlacingBox) {
        this.renderPlacingBox = renderPlacingBox;
    }

    public int getBlurRadius() {
        return Math.max(1, blurIntensity);
    }

    public int getBlurIntensity() {
        return blurIntensity;
    }

    public void setBlurIntensity(int blurIntensity) {
        this.blurIntensity = blurIntensity;
    }

    public boolean isReflectiveWater() {
        return reflectiveWater;
    }

    public void setReflectiveWater(boolean reflectiveWater) {
        this.reflectiveWater = reflectiveWater;
    }

    public boolean isVignette() {
        return vignette;
    }

    public void setVignette(boolean vignette) {
        this.vignette = vignette;
    }

    public boolean isMotionBlur() {
        return motionBlur && !isOculusVrSupport();
    }

    public void setMotionBlur(boolean motionBlur) {
        this.motionBlur = motionBlur;
    }

    public boolean isSsao() {
        return ssao;
    }

    public void setSsao(boolean ssao) {
        this.ssao = ssao;
    }

    public boolean isFilmGrain() {
        return filmGrain;
    }

    public void setFilmGrain(boolean filmGrain) {
        this.filmGrain = filmGrain;
    }

    public boolean isOutline() {
        return outline;
    }

    public void setOutline(boolean outline) {
        this.outline = outline;
    }

    public boolean isLightShafts() {
        return lightShafts;
    }

    public void setLightShafts(boolean lightShafts) {
        this.lightShafts = lightShafts;
    }

    public boolean isEyeAdaptation() {
        return eyeAdaptation;
    }

    public void setEyeAdaptation(boolean eyeAdaptation) {
        this.eyeAdaptation = eyeAdaptation;
    }

    public boolean isBloom() {
        return bloom;
    }

    public void setBloom(boolean bloom) {
        this.bloom = bloom;
    }

    public boolean isOculusVrSupport() {
        return oculusVrSupport;
    }

    public void setOculusVrSupport(boolean oculusVrSupport) {
        this.oculusVrSupport = oculusVrSupport;
    }

    public int getMaxTextureAtlasResolution() {
        return maxTextureAtlasResolution;
    }

    public void setMaxTextureAtlasResolution(int maxTextureAtlasResolution) {
        this.maxTextureAtlasResolution = maxTextureAtlasResolution;
    }

    public int getMaxChunksUsedForShadowMapping() {
        return maxChunksUsedForShadowMapping;
    }

    public void setMaxChunksUsedForShadowMapping(int maxChunksUsedForShadowMapping) {
        this.maxChunksUsedForShadowMapping = maxChunksUsedForShadowMapping;
    }

    public int getShadowMapResolution() {
        return shadowMapResolution;
    }

    public void setShadowMapResolution(int shadowMapResolution) {
        this.shadowMapResolution = shadowMapResolution;
    }

    public boolean isNormalMapping() {
        return normalMapping;
    }

    public void setNormalMapping(boolean normalMapping) {
        this.normalMapping = normalMapping;
    }

    public boolean isParallaxMapping() {
        return parallaxMapping;
    }

    public void setParallaxMapping(boolean parallaxMapping) {
        this.parallaxMapping = parallaxMapping;
    }

    public boolean isDynamicShadowsPcfFiltering() {
        return dynamicShadowsPcfFiltering;
    }

    public void setDynamicShadowsPcfFiltering(boolean dynamicShadowsPcfFiltering) {
        this.dynamicShadowsPcfFiltering = dynamicShadowsPcfFiltering;
    }

    public boolean isCloudShadows() {
        return cloudShadows;
    }

    public void setCloudShadows(boolean cloudShadows) {
        this.cloudShadows = cloudShadows;
    }

    public boolean isLocalReflections() {
        return this.localReflections;
    }

    public void setLocalReflections(boolean localReflections) {
        this.localReflections = localReflections;
    }

    public boolean isInscattering() {
        return this.inscattering;
    }

    public void setInscattering(boolean inscattering) {
        this.inscattering = inscattering;
    }

    public boolean isRenderNearest() {
        return renderNearest;
    }

    public void setRenderNearest(boolean renderNearest) {
        this.renderNearest = renderNearest;
    }

    public int getParticleEffectLimit() {
        return particleEffectLimit;
    }

    public void setParticleEffectLimit(int particleEffectLimit) {
        this.particleEffectLimit = particleEffectLimit;
    }

    public int getMeshLimit() {
        return meshLimit;
    }

    public void setMeshLimit(int meshLimit) {
        this.meshLimit = meshLimit;
    }

    public boolean isVSync() {
        return this.vSync;
    }

    public void setVSync(boolean value) {
        this.vSync = value;
    }

    public RenderingDebugConfig getDebug() {
        return debug;
    }

    public int getFrameLimit() {
        return frameLimit;
    }

    public void setFrameLimit(int frameLimit) {
        this.frameLimit = frameLimit;
    }

    public int getFboScale() {
        return fboScale;
    }

    public void setFboScale(int fboScale) {
        this.fboScale = fboScale;
    }

    public boolean isClampLighting() {
        return clampLighting;
    }

    public void setClampLighting(boolean clampLighting) {
        this.clampLighting = clampLighting;
    }

    public ScreenshotSize getScreenshotSize() {
        return screenshotSize;
    }

    public void setScreenshotSize(ScreenshotSize screenshotSize) {
        this.screenshotSize = screenshotSize;
    }

    public String getScreenshotFormat() {
        return screenshotFormat;
    }

    public void setScreenshotFormat(String screenshotFormat) {
        this.screenshotFormat = screenshotFormat;
    }

    public boolean isDumpShaders() {
        return dumpShaders;
    }

    public void setDumpShaders(boolean dumpShaders) {
        this.dumpShaders = dumpShaders;
    }

    /**
     * Subscribe a listener that gets nofified when a property cahnges..
     */
    public void subscribe(PropertyChangeListener changeListener) {
        this.propertyChangeSupport.addPropertyChangeListener(changeListener);
    }

    public void unsubscribe(PropertyChangeListener changeListener) {
        this.propertyChangeSupport.removePropertyChangeListener(changeListener);
    }

    public boolean isVolumetricFog() {
        return volumetricFog;
    }

    public void setVolumetricFog(boolean volumetricFog) {
        this.volumetricFog = volumetricFog;
    }
}
