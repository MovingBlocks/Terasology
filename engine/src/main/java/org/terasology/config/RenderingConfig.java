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
import org.terasology.utilities.subscribables.AbstractSubscribable;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 */
public class RenderingConfig extends AbstractSubscribable {

    private PixelFormat pixelFormat;
    private int windowPosX;
    private int windowPosY;
    private int windowWidth;
    private int windowHeight;
    private boolean fullscreen;
    private boolean animatedMenu;
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

    public PerspectiveCameraSettings getCameraSettings() {
        return cameraSettings;
    }

    public PixelFormat getPixelFormat() {
        return pixelFormat;
    }

    public static final String PIXEL_FORMAT = "PixelFormat";
    public void setPixelFormat(PixelFormat pixelFormat) {
        PixelFormat oldFormat = this.pixelFormat;
        this.pixelFormat = pixelFormat;
        propertyChangeSupport.firePropertyChange(PIXEL_FORMAT, oldFormat, this.pixelFormat);
        // propertyChangeSupport fires only if oldObject != newObject.
        // This method could theoretically use a better equality check then. In practice
        // it's unlikely a new PixelFormat instance will ever be value-per-value identical
        // to the previous one, -not- needing a change event firing.
    }

    public int getWindowPosX() {
        return windowPosX;
    }

    public static final String WINDOW_POS_X = "WindowPosX";
    public void setWindowPosX(int windowPosX) {
        int oldValue = this.windowPosX;
        this.windowPosX = windowPosX;
        propertyChangeSupport.firePropertyChange(WINDOW_POS_X, oldValue, this.windowPosX);
    }

    public int getWindowPosY() {
        return windowPosY;
    }

    public static final String WINDOW_POS_Y = "WindowPosY";
    public void setWindowPosY(int windowPosY) {
        int oldValue = this.windowPosY;
        this.windowPosY = windowPosY;
        propertyChangeSupport.firePropertyChange(WINDOW_POS_Y, oldValue, this.windowPosY);
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public static final String WINDOW_WIDTH = "WindowWidth";
    public void setWindowWidth(int windowWidth) {
        int oldValue = this.windowWidth;
        this.windowWidth = windowWidth;
        propertyChangeSupport.firePropertyChange(WINDOW_WIDTH, oldValue, this.windowWidth);
    }

    public int getWindowHeight() { return windowHeight; }

    public static final String WINDOW_HEIGHT = "WindowHeight";
    public void setWindowHeight(int windowHeight) {
            int oldValue = this.windowHeight;
            this.windowHeight = windowHeight;
            propertyChangeSupport.firePropertyChange(WINDOW_HEIGHT, oldValue, this.windowHeight);
    }

    public DisplayMode getDisplayMode() {
        return new DisplayMode(windowWidth, windowHeight);
    }

    public boolean isFullscreen() { return fullscreen; }

    public static final String FULLSCREEN = "FullScreen";
    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        propertyChangeSupport.firePropertyChange(FULLSCREEN, !this.fullscreen, this.fullscreen);
    }

    public boolean isAnimatedMenu() {
        return animatedMenu;
    }

    public static final String ANIMATED_MENU = "AnimatedMenu";
    public void setAnimatedMenu(boolean animatedMenu) {
        this.animatedMenu = animatedMenu;
        propertyChangeSupport.firePropertyChange(ANIMATED_MENU, !this.animatedMenu, this.animatedMenu);
    }

    public ViewDistance getViewDistance() {
        return viewDistance;
    }

    public static final String VIEW_DISTANCE = "viewDistance";
    /**
     * Sets the view distance and notifies the property change listeners registered via
     * {@link RenderingConfig#subscribe(PropertyChangeListener)} that listen for the property {@link #VIEW_DISTANCE}.
     * @param viewDistance the new view distance
     */
    public void setViewDistance(ViewDistance viewDistance) {
        ViewDistance oldDistance = this.viewDistance;
        this.viewDistance = viewDistance;
        propertyChangeSupport.firePropertyChange(VIEW_DISTANCE, oldDistance, viewDistance);
    }

    public boolean isFlickeringLight() {
        return flickeringLight;
    }

    public static final String FLICKERING_LIGHT = "FlickeringLight";
    public void setFlickeringLight(boolean flickeringLight) {
        this.flickeringLight = flickeringLight;
        propertyChangeSupport.firePropertyChange(FLICKERING_LIGHT, !this.flickeringLight, this.flickeringLight);
    }

    public boolean isAnimateGrass() {
        return animateGrass;
    }

    public static final String ANIMATE_GRASS = "AnimateGrass";
    public void setAnimateGrass(boolean animateGrass) {
        this.animateGrass = animateGrass;
        propertyChangeSupport.firePropertyChange(ANIMATE_GRASS, !this.animateGrass, this.animateGrass);
    }

    public boolean isAnimateWater() {
        return animateWater;
    }

    public static final String ANIMATE_WATER = "AnimateWater";
    public void setAnimateWater(boolean animateWater) {
        this.animateWater = animateWater;
        propertyChangeSupport.firePropertyChange(ANIMATE_WATER, !this.animateWater, this.animateWater);
    }

    public boolean isDynamicShadows() {
        return dynamicShadows;
    }

    public static final String DYNAMIC_SHADOWS = "DynamicShadows";
    public void setDynamicShadows(boolean dynamicShadows) {
        this.dynamicShadows = dynamicShadows;
        propertyChangeSupport.firePropertyChange(DYNAMIC_SHADOWS, !this.dynamicShadows, this.dynamicShadows);
    }

    public float getFieldOfView() {
        return fieldOfView;
    }

    public static final String FIELD_OF_VIEW = "FieldOfView";
    public void setFieldOfView(float fieldOfView) {
        float oldFieldOfView = this.fieldOfView;
        this.fieldOfView = fieldOfView;
        propertyChangeSupport.firePropertyChange(FIELD_OF_VIEW, oldFieldOfView, this.fieldOfView);
    }

    public boolean isCameraBobbing() {
        return cameraBobbing;
    }

    public static final String CAMERA_BOBBING = "CameraBobbing";
    public void setCameraBobbing(boolean cameraBobbing) {
        this.cameraBobbing = cameraBobbing;
        propertyChangeSupport.firePropertyChange(CAMERA_BOBBING, !this.cameraBobbing, this.cameraBobbing);
    }

    public boolean isRenderPlacingBox() {
        return renderPlacingBox;
    }

    public static final String RENDER_PLACING_BOX = "RenderPlacingBox";
    public void setRenderPlacingBox(boolean renderPlacingBox) {
        this.renderPlacingBox = renderPlacingBox;
        propertyChangeSupport.firePropertyChange(RENDER_PLACING_BOX, !this.renderPlacingBox, this.renderPlacingBox);
    }

    public int getBlurRadius() {
        return Math.max(1, blurIntensity);
    }

    public int getBlurIntensity() {
        return blurIntensity;
    }

    public static final String BLUR_INTENSITY = "BlurIntensity";
    public void setBlurIntensity(int blurIntensity) {
        int oldIntensity = this.blurIntensity;
        this.blurIntensity = blurIntensity;
        propertyChangeSupport.firePropertyChange(BLUR_INTENSITY, oldIntensity, this.blurIntensity);
    }

    public boolean isReflectiveWater() {
        return reflectiveWater;
    }

    public static final String REFLECTIVE_WATER = "ReflectiveWater";
    public void setReflectiveWater(boolean reflectiveWater) {
        this.reflectiveWater = reflectiveWater;
        propertyChangeSupport.firePropertyChange(REFLECTIVE_WATER, !this.reflectiveWater, this.reflectiveWater);
    }

    public boolean isVignette() {
        return vignette;
    }

    public static final String VIGNETTE = "Vignette";
    public void setVignette(boolean vignette) {
        this.vignette = vignette;
        propertyChangeSupport.firePropertyChange(VIGNETTE, !this.vignette, this.vignette);
    }

    public boolean isMotionBlur() {
        return motionBlur && !isOculusVrSupport();
    }

    public static final String MOTION_BLUR = "MotionBlur";
    public void setMotionBlur(boolean motionBlur) {
        this.motionBlur = motionBlur;
        propertyChangeSupport.firePropertyChange(MOTION_BLUR, !this.motionBlur, this.motionBlur);
    }

    public boolean isSsao() {
        return ssao;
    }

    public static final String SSAO = "Ssao";
    public void setSsao(boolean ssao) {
        this.ssao = ssao;
        propertyChangeSupport.firePropertyChange(SSAO, !this.ssao, this.ssao);
    }

    public boolean isFilmGrain() {
        return filmGrain;
    }

    public static final String FILM_GRAIN = "FilmGrain";
    public void setFilmGrain(boolean filmGrain) {
        this.filmGrain = filmGrain;
        propertyChangeSupport.firePropertyChange(FILM_GRAIN, !this.filmGrain, this.filmGrain);
    }

    public boolean isOutline() {
        return outline;
    }

    public static final String OUTLINE = "Outline";
    public void setOutline(boolean outline) {
        this.outline = outline;
        propertyChangeSupport.firePropertyChange(OUTLINE, !this.outline, this.outline);
    }

    public boolean isLightShafts() {
        return lightShafts;
    }

    public static final String LIGHT_SHAFTS = "LightShafts";
    public void setLightShafts(boolean lightShafts) {
        this.lightShafts = lightShafts;
        propertyChangeSupport.firePropertyChange(LIGHT_SHAFTS, !this.lightShafts, this.lightShafts);
    }

    public boolean isEyeAdaptation() {
        return eyeAdaptation;
    }

    public static final String EYE_ADAPTATION = "EyeAdaptation";
    public void setEyeAdaptation(boolean eyeAdaptation) {
        this.eyeAdaptation = eyeAdaptation;
        propertyChangeSupport.firePropertyChange(EYE_ADAPTATION, !this.eyeAdaptation, this.eyeAdaptation);
    }

    public boolean isBloom() {
        return bloom;
    }

    public static final String BLOOM = "Bloom";
    public void setBloom(boolean bloom) {
        this.bloom = bloom;
        propertyChangeSupport.firePropertyChange(BLOOM, !this.bloom, this.bloom);
    }

    public boolean isOculusVrSupport() {
        return oculusVrSupport;
    }

    public static final String OCULUS_VR_SUPPORT = "OculusVrSupport";
    public void setOculusVrSupport(boolean oculusVrSupport) {
            this.oculusVrSupport = oculusVrSupport;
            propertyChangeSupport.firePropertyChange(OCULUS_VR_SUPPORT, !this.oculusVrSupport, this.oculusVrSupport);
    }    

    public int getMaxTextureAtlasResolution() {
        return maxTextureAtlasResolution;
    }

    public static final String MAX_TEXTURE_ATLAS_RESOLUTION = "MaxTextureAtlasResolution";
    public void setMaxTextureAtlasResolution(int maxTextureAtlasResolution) {
        int oldResolution = this.maxTextureAtlasResolution;
        this.maxTextureAtlasResolution = maxTextureAtlasResolution;
        propertyChangeSupport.firePropertyChange(MAX_TEXTURE_ATLAS_RESOLUTION, oldResolution, this.maxTextureAtlasResolution);
    }

    public int getMaxChunksUsedForShadowMapping() {
        return maxChunksUsedForShadowMapping;
    }

    public static final String MAX_CHUNKS_USED_FOR_SHADOW_MAPPING = "MaxChunksUsedForShadowMapping";
    public void setMaxChunksUsedForShadowMapping(int maxChunksUsedForShadowMapping) {
        int oldValue = this.maxChunksUsedForShadowMapping;
        this.maxChunksUsedForShadowMapping = maxChunksUsedForShadowMapping;
        propertyChangeSupport.firePropertyChange(MAX_CHUNKS_USED_FOR_SHADOW_MAPPING, oldValue, this.maxChunksUsedForShadowMapping);
    }

    public int getShadowMapResolution() {
        return shadowMapResolution;
    }

    public static final String SHADOW_MAP_RESOLUTION = "ShadowMapResolution";
    public void setShadowMapResolution(int shadowMapResolution) {
        int oldResolution = this.shadowMapResolution;
        this.shadowMapResolution = shadowMapResolution;
        propertyChangeSupport.firePropertyChange(SHADOW_MAP_RESOLUTION, oldResolution, this.shadowMapResolution);
    }
    public boolean isNormalMapping() {
        return normalMapping;
    }

    public static final String NORMAL_MAPPING = "NormalMapping";
    public void setNormalMapping(boolean normalMapping) {
        this.normalMapping = normalMapping;
        propertyChangeSupport.firePropertyChange(NORMAL_MAPPING, !this.normalMapping, this.normalMapping);
    }
    
    public boolean isParallaxMapping() {
        return parallaxMapping;
    }

    public static final String PARALLAX_MAPPING = "ParallaxMapping";
    public void setParallaxMapping(boolean parallaxMapping) {
        this.parallaxMapping = parallaxMapping;
        propertyChangeSupport.firePropertyChange(PARALLAX_MAPPING, !this.parallaxMapping, this.parallaxMapping);
    }

    public boolean isDynamicShadowsPcfFiltering() {
        return dynamicShadowsPcfFiltering;
    }

    public static final String DYNAMIC_SHADOWS_PCF_FILTERING = "DynamicShadowsPcfFiltering";
    public void setDynamicShadowsPcfFiltering(boolean dynamicShadowsPcfFiltering) {
        this.dynamicShadowsPcfFiltering = dynamicShadowsPcfFiltering;
        propertyChangeSupport.firePropertyChange(DYNAMIC_SHADOWS_PCF_FILTERING, !this.dynamicShadowsPcfFiltering, this.dynamicShadowsPcfFiltering);
    }

    public boolean isCloudShadows() {
        return cloudShadows;
    }

    public static final String CLOUD_SHADOWS = "CloudShadows";
    public void setCloudShadows(boolean cloudShadows) {
        this.cloudShadows = cloudShadows;
        propertyChangeSupport.firePropertyChange(CLOUD_SHADOWS, !this.cloudShadows, this.cloudShadows);
    }

    public boolean isLocalReflections() {
        return this.localReflections;
    }

    public static final String LOCAL_REFLECTIONS = "LocalReflections";
    public void setLocalReflections(boolean localReflections) {
        this.localReflections = localReflections;
        propertyChangeSupport.firePropertyChange(LOCAL_REFLECTIONS, !this.localReflections, this.localReflections);
    }

    public boolean isInscattering() {
        return this.inscattering;
    }

    public static final String INSCATTERING = "Inscattering";
    public void setInscattering(boolean inscattering) {
        this.inscattering = inscattering;
        propertyChangeSupport.firePropertyChange(INSCATTERING, !this.inscattering, this.inscattering);
    }

    public boolean isRenderNearest() {
        return renderNearest;
    }

    public static final String RENDER_NEAREST = "RenderNearest";
    public void setRenderNearest(boolean renderNearest) {
        this.renderNearest = renderNearest;
        propertyChangeSupport.firePropertyChange(RENDER_NEAREST, !this.renderNearest, this.renderNearest);
    }
    public int getParticleEffectLimit() {
        return particleEffectLimit;
    }

    public static final String PARTICLE_EFFECT_LIMIT = "ParticleEffectLimit";
    public void setParticleEffectLimit(int particleEffectLimit) {
        int oldLimit = this.particleEffectLimit;
        this.particleEffectLimit = particleEffectLimit;
        propertyChangeSupport.firePropertyChange(PARTICLE_EFFECT_LIMIT, oldLimit, this.particleEffectLimit);
    }

    public int getMeshLimit() {
        return meshLimit;
    }

    public static final String MESH_LIMIT = "MeshLimit";
    public void setMeshLimit(int meshLimit) {
        int oldLimit = this.meshLimit;
        this.meshLimit = meshLimit;
        propertyChangeSupport.firePropertyChange(MESH_LIMIT, oldLimit, this.meshLimit);
    }
    public boolean isVSync() {
        return this.vSync;
    }

    public static final String V_SYNC = "VSync";
    public void setVSync(boolean vSync) {
        this.vSync = vSync;
        propertyChangeSupport.firePropertyChange(V_SYNC, !this.vSync, this.vSync);
    }

    public RenderingDebugConfig getDebug() {
        return debug;
    }

    public int getFrameLimit() {
        return frameLimit;
    }

    public static final String FRAME_LIMIT = "FrameLimit";
    public void setFrameLimit(int frameLimit) {
        int oldLimit = this.frameLimit;
        this.frameLimit = frameLimit;
        propertyChangeSupport.firePropertyChange(FRAME_LIMIT, oldLimit, this.frameLimit);
    }

    public int getFboScale() {
        return fboScale;
    }

    public static final String FBO_SCALE = "FboScale";
    public void setFboScale(int fboScale) {
        int oldScale = this.fboScale;
        this.fboScale = fboScale;
        propertyChangeSupport.firePropertyChange(FBO_SCALE, oldScale, this.fboScale);
    }

    public boolean isClampLighting() {
        return clampLighting;
    }

    public static final String CLAMP_LIGHTING = "ClampLighting";
    public void setClampLighting(boolean clampLighting) {
        this.clampLighting = clampLighting;
        propertyChangeSupport.firePropertyChange(CLAMP_LIGHTING, !this.clampLighting, this.clampLighting);
    }

    public ScreenshotSize getScreenshotSize() {
        return screenshotSize;
    }

    public static final String SCREENSHOT_SIZE = "screenshotSize";
    public void setScreenshotSize(ScreenshotSize screenshotSize) {
        ScreenshotSize oldSize = this.screenshotSize;
        this.screenshotSize = screenshotSize;
        propertyChangeSupport.firePropertyChange(SCREENSHOT_SIZE, oldSize, this.screenshotSize);
        // propertyChangeSupport fires only if oldObject != newObject.
        // This method could theoretically use a better equality check then. In practice
        // it's unlikely a new ScreenshotSize instance will ever be value-per-value identical
        // to the previous one, not requiring the event to be fired.
    }

    public String getScreenshotFormat() {
        return screenshotFormat;
    }

    public static final String SCREENSHOT_FORMAT = "ScreenshotFormat";
    public void setScreenshotFormat(String screenshotFormat) {

        // propertyChangeSupport fires if oldFormat != newFormat which in the case of strings is always true.
        // For this case we therefore use the equals method to prevent identical strings triggering the
        // firing of the change event.
        if (!this.screenshotFormat.equals(screenshotFormat)) {
            String oldFormat = this.screenshotFormat;
            this.screenshotFormat = screenshotFormat;
            propertyChangeSupport.firePropertyChange(SCREENSHOT_FORMAT, oldFormat, this.screenshotFormat);
        }
    }

    public boolean isDumpShaders() {
        return dumpShaders;
    }

    public static final String DUMP_SHADERS = "DumpShaders";
    public void setDumpShaders(boolean dumpShaders) {
        this.dumpShaders = dumpShaders;
        propertyChangeSupport.firePropertyChange(DUMP_SHADERS, !this.dumpShaders, this.dumpShaders);
    }
    
    public boolean isVolumetricFog() {
        return volumetricFog;
    }

    public static final String VOLUMETRIC_FOG = "VolumetricFog";
    public void setVolumetricFog(boolean volumetricFog) {
        this.volumetricFog = volumetricFog;
        propertyChangeSupport.firePropertyChange(VOLUMETRIC_FOG, !this.volumetricFog, this.volumetricFog);
    }

}
