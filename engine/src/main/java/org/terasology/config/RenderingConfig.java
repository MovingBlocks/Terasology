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
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.events.ChangeViewRangeRequest;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.cameras.PerspectiveCameraSettings;
import org.terasology.rendering.nui.layers.mainMenu.videoSettings.CameraSetting;
import org.terasology.rendering.world.ViewDistance;
import org.terasology.rendering.world.WorldRenderer;

/**
 * @author Immortius
 */
public class RenderingConfig {
    private PixelFormat pixelFormat = new PixelFormat().withDepthBits(24);
    private int windowPosX = -1;
    private int windowPosY = -1;
    private int windowWidth = 1152;
    private int windowHeight = 720;
    private boolean fullscreen;
    private ViewDistance viewDistance = ViewDistance.MODERATE;
    private boolean flickeringLight = true;
    private boolean animateGrass = true;
    private boolean animateWater;
    private float fieldOfView = 90;
    private boolean cameraBobbing = true;
    private boolean renderPlacingBox = true;
    private int blurIntensity = 2;
    private boolean reflectiveWater;
    private boolean vignette = true;
    private boolean motionBlur = true;
    private boolean ssao;
    private boolean filmGrain = true;
    private boolean outline = true;
    private boolean lightShafts = true;
    private boolean eyeAdaptation = true;
    private boolean bloom = true;
    private boolean dynamicShadows = true;
    private boolean oculusVrSupport;
    private int maxTextureAtlasResolution = 4096;
    private int maxChunksUsedForShadowMapping = 1024;
    private int shadowMapResolution = 1024;
    private boolean normalMapping;
    private boolean parallaxMapping;
    private boolean dynamicShadowsPcfFiltering;
    private boolean cloudShadows = true;
    private boolean renderNearest = true;
    private int particleEffectLimit = 10;
    private int meshLimit = 400;
    private boolean inscattering = true;
    private boolean localReflections;
    private boolean vSync;
    private PerspectiveCameraSettings cameraSettings = new PerspectiveCameraSettings(CameraSetting.NORMAL);

    private RenderingDebugConfig debug = new RenderingDebugConfig();

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

    public void setViewDistance(ViewDistance viewDistance) {
        this.viewDistance = viewDistance;

        // TODO: Remove this, switch to a property change listener
        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        LocalPlayer player = CoreRegistry.get(LocalPlayer.class);
        if (player != null) {
            player.getClientEntity().send(new ChangeViewRangeRequest(viewDistance));
        }
        if (worldRenderer != null) {
            worldRenderer.changeViewDistance(viewDistance);
        }
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

    @Override
    public String toString() {
        return Config.createGson().toJsonTree(this).toString();
    }
}
