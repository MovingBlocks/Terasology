// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config;

import org.terasology.engine.core.subsystem.Resolution;
import org.terasology.context.annotation.API;
import org.terasology.engine.rendering.cameras.PerspectiveCameraSettings;
import org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings.DisplayModeSetting;
import org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings.ScreenshotSize;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.utilities.subscribables.AbstractSubscribable;

import java.beans.PropertyChangeListener;

@API
public class RenderingConfig extends AbstractSubscribable {

    public static final String PIXEL_FORMAT = "PixelFormat";
    public static final String WINDOW_POS_X = "WindowPosX";
    public static final String WINDOW_POS_Y = "WindowPosY";
    public static final String WINDOW_WIDTH = "WindowWidth";
    public static final String WINDOW_HEIGHT = "WindowHeight";
    public static final String DISPLAY_MODE_SETTING = "DisplayModeSetting";
    public static final String RESOLUTION = "Resolution";
    public static final String ANIMATED_MENU = "AnimatedMenu";
    public static final String VIEW_DISTANCE = "viewDistance";
    public static final String CHUNK_LODS = "chunkLods";
    public static final String CHUNK_THREADS = "chunkThreads";
    public static final String BILLBOARD_LIMIT = "billboardLimit";
    public static final String FLICKERING_LIGHT = "FlickeringLight";
    public static final String ANIMATE_GRASS = "AnimateGrass";
    public static final String ANIMATE_WATER = "AnimateWater";
    public static final String DYNAMIC_SHADOWS = "DynamicShadows";
    public static final String FIELD_OF_VIEW = "FieldOfView";
    public static final String CAMERA_BOBBING = "CameraBobbing";
    public static final String RENDER_PLACING_BOX = "RenderPlacingBox";
    public static final String BLUR_INTENSITY = "BlurIntensity";
    public static final String REFLECTIVE_WATER = "ReflectiveWater";
    public static final String VIGNETTE = "Vignette";
    public static final String MOTION_BLUR = "MotionBlur";
    public static final String SSAO = "Ssao";
    public static final String FILM_GRAIN = "FilmGrain";
    public static final String OUTLINE = "Outline";
    public static final String LIGHT_SHAFTS = "LightShafts";
    public static final String EYE_ADAPTATION = "EyeAdaptation";
    public static final String BLOOM = "Bloom";
    public static final String MAX_TEXTURE_ATLAS_RESOLUTION = "MaxTextureAtlasResolution";
    public static final String MAX_CHUNKS_USED_FOR_SHADOW_MAPPING = "MaxChunksUsedForShadowMapping";
    public static final String SHADOW_MAP_RESOLUTION = "ShadowMapResolution";
    public static final String NORMAL_MAPPING = "NormalMapping";
    public static final String PARALLAX_MAPPING = "ParallaxMapping";
    public static final String DYNAMIC_SHADOWS_PCF_FILTERING = "DynamicShadowsPcfFiltering";
    public static final String CLOUD_SHADOWS = "CloudShadows";
    public static final String LOCAL_REFLECTIONS = "LocalReflections";
    public static final String INSCATTERING = "Inscattering";
    public static final String RENDER_NEAREST = "RenderNearest";
    public static final String PARTICLE_EFFECT_LIMIT = "ParticleEffectLimit";
    public static final String MESH_LIMIT = "MeshLimit";
    public static final String V_SYNC = "VSync";
    public static final String FRAME_LIMIT = "FrameLimit";
    public static final String FBO_SCALE = "FboScale";
    public static final String UI_SCALE = "UiScale";
    public static final String CLAMP_LIGHTING = "ClampLighting";
    public static final String SCREENSHOT_SIZE = "screenshotSize";
    public static final String SCREENSHOT_FORMAT = "ScreenshotFormat";
    public static final String DUMP_SHADERS = "DumpShaders";
    public static final String VOLUMETRIC_FOG = "VolumetricFog";

    private int pixelFormat;
    private int windowPosX;
    private int windowPosY;
    private int windowWidth;
    private int windowHeight;
    private DisplayModeSetting displayModeSetting;
    private Resolution resolution;
    private boolean animatedMenu;
    private ViewDistance viewDistance;
    private float chunkLods;
    private int chunkThreads;
    private float billboardLimit;
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
    private int uiScale = 100;
    private boolean dumpShaders;
    private boolean volumetricFog;
    private ScreenshotSize screenshotSize;
    private String screenshotFormat;
    private PerspectiveCameraSettings cameraSettings;

    private RenderingDebugConfig debug = new RenderingDebugConfig();

    public PerspectiveCameraSettings getCameraSettings() {
        return cameraSettings;
    }

    public int getPixelFormat() {
        return pixelFormat;
    }

    public void setPixelFormat(int pixelFormat) {
        int oldFormat = this.pixelFormat;
        this.pixelFormat = pixelFormat;
        propertyChangeSupport.firePropertyChange(PIXEL_FORMAT, oldFormat, this.pixelFormat);
        // propertyChangeSupport fires only if oldObject != newObject.
        // This method could theoretically use a better equality check then. In practice
        // it's unlikely a new PixelFormat instance will ever be value-per-value identical
        // to the previous one.
    }

    public int getWindowPosX() {
        return windowPosX;
    }

    public void setWindowPosX(int windowPosX) {
        int oldValue = this.windowPosX;
        this.windowPosX = windowPosX;
        propertyChangeSupport.firePropertyChange(WINDOW_POS_X, oldValue, this.windowPosX);
    }

    public int getWindowPosY() {
        return windowPosY;
    }

    public void setWindowPosY(int windowPosY) {
        int oldValue = this.windowPosY;
        this.windowPosY = windowPosY;
        propertyChangeSupport.firePropertyChange(WINDOW_POS_Y, oldValue, this.windowPosY);
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        int oldValue = this.windowWidth;
        this.windowWidth = windowWidth;
        propertyChangeSupport.firePropertyChange(WINDOW_WIDTH, oldValue, this.windowWidth);
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
            int oldValue = this.windowHeight;
            this.windowHeight = windowHeight;
            propertyChangeSupport.firePropertyChange(WINDOW_HEIGHT, oldValue, this.windowHeight);
    }

    public void setDisplayModeSetting(DisplayModeSetting displayModeSetting) {
        switch (displayModeSetting) {
            case FULLSCREEN:
                setFullscreen(true);
                break;
            case WINDOWED_FULLSCREEN:
                setWindowedFullscreen(true);
                break;
            case WINDOWED:
                setFullscreen(false);
                break;
        }
    }

    public DisplayModeSetting getDisplayModeSetting() {
        return displayModeSetting;
    }

    public boolean isFullscreen() {
        return displayModeSetting.getCurrent() == DisplayModeSetting.FULLSCREEN;
    }

    public void setFullscreen(boolean fullscreen) {
        DisplayModeSetting oldValue = displayModeSetting;
        if (fullscreen) {
            displayModeSetting = DisplayModeSetting.FULLSCREEN;
        } else {
            displayModeSetting = DisplayModeSetting.WINDOWED;
        }
        displayModeSetting.setCurrent(true);
        propertyChangeSupport.firePropertyChange(DISPLAY_MODE_SETTING, oldValue, displayModeSetting);
    }

    public boolean isWindowedFullscreen() {
        return displayModeSetting.getCurrent() == DisplayModeSetting.WINDOWED_FULLSCREEN;
    }

    public void setWindowedFullscreen(boolean fullscreenWindowed) {
        DisplayModeSetting oldValue = displayModeSetting;
        if (fullscreenWindowed) {
            displayModeSetting = DisplayModeSetting.WINDOWED_FULLSCREEN;
            displayModeSetting.setCurrent(true);
            propertyChangeSupport.firePropertyChange(DISPLAY_MODE_SETTING, oldValue, displayModeSetting);
        } else {
            setFullscreen(true);
        }
    }

    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        Resolution oldValue = this.resolution;
        this.resolution = resolution;
        propertyChangeSupport.firePropertyChange(RESOLUTION, oldValue, resolution);
    }

    public boolean isAnimatedMenu() {
        return animatedMenu;
    }

    public void setAnimatedMenu(boolean animatedMenu) {
        boolean oldValue = this.animatedMenu;
        this.animatedMenu = animatedMenu;
        propertyChangeSupport.firePropertyChange(ANIMATED_MENU, oldValue, this.animatedMenu);
    }

    public ViewDistance getViewDistance() {
        return (viewDistance == null) ? ViewDistance.MODERATE : viewDistance;
    }

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

    public float getChunkLods() {
        return chunkLods;
    }

    public void setChunkLods(float chunkLods) {
        float oldLods = this.chunkLods;
        this.chunkLods = chunkLods;
        propertyChangeSupport.firePropertyChange(CHUNK_LODS, oldLods, chunkLods);
    }

    public int getChunkThreads() {
        return chunkThreads;
    }

    public void setChunkThreads(int chunkThreads) {
        float oldChunkThreads = this.chunkThreads;
        this.chunkThreads = chunkThreads;
        propertyChangeSupport.firePropertyChange(CHUNK_THREADS, oldChunkThreads, chunkThreads);
    }

    public float getBillboardLimit() {
        return billboardLimit;
    }

    public void setBillboardLimit(float billboardLimit) {
        float oldValue = this.billboardLimit;
        this.billboardLimit = billboardLimit;
        propertyChangeSupport.firePropertyChange(BILLBOARD_LIMIT, oldValue, this.billboardLimit);
    }

    public boolean isFlickeringLight() {
        return flickeringLight;
    }

    public void setFlickeringLight(boolean flickeringLight) {
        boolean oldValue = this.flickeringLight;
        this.flickeringLight = flickeringLight;
        propertyChangeSupport.firePropertyChange(FLICKERING_LIGHT, oldValue, this.flickeringLight);
    }

    public boolean isAnimateGrass() {
        return animateGrass;
    }

    public void setAnimateGrass(boolean animateGrass) {
        boolean oldValue = this.animateGrass;
        this.animateGrass = animateGrass;
        propertyChangeSupport.firePropertyChange(ANIMATE_GRASS, oldValue, this.animateGrass);
    }

    public boolean isAnimateWater() {
        return animateWater;
    }

    public void setAnimateWater(boolean animateWater) {
        boolean oldValue = this.animateWater;
        this.animateWater = animateWater;
        propertyChangeSupport.firePropertyChange(ANIMATE_WATER, oldValue, this.animateWater);
    }

    public boolean isDynamicShadows() {
        return dynamicShadows;
    }

    public void setDynamicShadows(boolean dynamicShadows) {
        boolean oldValue = this.dynamicShadows;
        this.dynamicShadows = dynamicShadows;
        propertyChangeSupport.firePropertyChange(DYNAMIC_SHADOWS, oldValue, this.dynamicShadows);
    }

    public float getFieldOfView() {
        return fieldOfView;
    }

    public void setFieldOfView(float fieldOfView) {
        float oldFieldOfView = this.fieldOfView;
        this.fieldOfView = fieldOfView;
        propertyChangeSupport.firePropertyChange(FIELD_OF_VIEW, oldFieldOfView, this.fieldOfView);
    }

    public boolean isCameraBobbing() {
        return cameraBobbing;
    }

    public void setCameraBobbing(boolean cameraBobbing) {
        boolean oldValue = this.cameraBobbing;
        this.cameraBobbing = cameraBobbing;
        propertyChangeSupport.firePropertyChange(CAMERA_BOBBING, oldValue, this.cameraBobbing);
    }

    public boolean isRenderPlacingBox() {
        return renderPlacingBox;
    }

    public void setRenderPlacingBox(boolean renderPlacingBox) {
        boolean oldValue = this.renderPlacingBox;
        this.renderPlacingBox = renderPlacingBox;
        propertyChangeSupport.firePropertyChange(RENDER_PLACING_BOX, oldValue, this.renderPlacingBox);
    }

    public int getBlurRadius() {
        return Math.max(1, blurIntensity);
    }

    public int getBlurIntensity() {
        return blurIntensity;
    }

    public void setBlurIntensity(int blurIntensity) {
        int oldIntensity = this.blurIntensity;
        this.blurIntensity = blurIntensity;
        propertyChangeSupport.firePropertyChange(BLUR_INTENSITY, oldIntensity, this.blurIntensity);
    }

    public boolean isReflectiveWater() {
        return reflectiveWater;
    }

    public void setReflectiveWater(boolean reflectiveWater) {
        boolean oldValue = this.reflectiveWater;
        this.reflectiveWater = reflectiveWater;
        propertyChangeSupport.firePropertyChange(REFLECTIVE_WATER, oldValue, this.reflectiveWater);
    }

    public boolean isVignette() {
        return vignette;
    }

    public void setVignette(boolean vignette) {
        boolean oldValue = this.vignette;
        this.vignette = vignette;
        propertyChangeSupport.firePropertyChange(VIGNETTE, oldValue, this.vignette);
    }

    public boolean isMotionBlur() {
        return motionBlur;
    }

    public void setMotionBlur(boolean motionBlur) {
        boolean oldValue = this.motionBlur;
        this.motionBlur = motionBlur;
        propertyChangeSupport.firePropertyChange(MOTION_BLUR, oldValue, this.motionBlur);
    }

    public boolean isSsao() {
        return ssao;
    }

    public void setSsao(boolean ssao) {
        boolean oldValue = this.ssao;
        this.ssao = ssao;
        propertyChangeSupport.firePropertyChange(SSAO, oldValue, this.ssao);
    }

    public boolean isFilmGrain() {
        return filmGrain;
    }

    public void setFilmGrain(boolean filmGrain) {
        boolean oldValue = this.filmGrain;
        this.filmGrain = filmGrain;
        propertyChangeSupport.firePropertyChange(FILM_GRAIN, oldValue, this.filmGrain);
    }

    public boolean isOutline() {
        return outline;
    }

    public void setOutline(boolean outline) {
        boolean oldValue = this.outline;
        this.outline = outline;
        propertyChangeSupport.firePropertyChange(OUTLINE, oldValue, this.outline);
    }

    public boolean isLightShafts() {
        return lightShafts;
    }

    public void setLightShafts(boolean lightShafts) {
        boolean oldValue = this.lightShafts;
        this.lightShafts = lightShafts;
        propertyChangeSupport.firePropertyChange(LIGHT_SHAFTS, oldValue, this.lightShafts);
    }

    public boolean isEyeAdaptation() {
        return eyeAdaptation;
    }

    public void setEyeAdaptation(boolean eyeAdaptation) {
        boolean oldValue = this.eyeAdaptation;
        this.eyeAdaptation = eyeAdaptation;
        propertyChangeSupport.firePropertyChange(EYE_ADAPTATION, oldValue, this.eyeAdaptation);
    }

    public boolean isBloom() {
        return bloom;
    }

    public void setBloom(boolean bloom) {
        boolean oldValue = this.bloom;
        this.bloom = bloom;
        propertyChangeSupport.firePropertyChange(BLOOM, oldValue, this.bloom);
    }

    public int getMaxTextureAtlasResolution() {
        return maxTextureAtlasResolution;
    }

    public void setMaxTextureAtlasResolution(int maxTextureAtlasResolution) {
        int oldResolution = this.maxTextureAtlasResolution;
        this.maxTextureAtlasResolution = maxTextureAtlasResolution;
        propertyChangeSupport.firePropertyChange(MAX_TEXTURE_ATLAS_RESOLUTION, oldResolution, this.maxTextureAtlasResolution);
    }

    public int getMaxChunksUsedForShadowMapping() {
        return maxChunksUsedForShadowMapping;
    }

    public void setMaxChunksUsedForShadowMapping(int maxChunksUsedForShadowMapping) {
        int oldValue = this.maxChunksUsedForShadowMapping;
        this.maxChunksUsedForShadowMapping = maxChunksUsedForShadowMapping;
        propertyChangeSupport.firePropertyChange(MAX_CHUNKS_USED_FOR_SHADOW_MAPPING, oldValue, this.maxChunksUsedForShadowMapping);
    }

    public int getShadowMapResolution() {
        return shadowMapResolution;
    }

    public void setShadowMapResolution(int shadowMapResolution) {
        int oldResolution = this.shadowMapResolution;
        this.shadowMapResolution = shadowMapResolution;
        propertyChangeSupport.firePropertyChange(SHADOW_MAP_RESOLUTION, oldResolution, this.shadowMapResolution);
    }
    public boolean isNormalMapping() {
        return normalMapping;
    }

    public void setNormalMapping(boolean normalMapping) {
        boolean oldValue = this.normalMapping;
        this.normalMapping = normalMapping;
        propertyChangeSupport.firePropertyChange(NORMAL_MAPPING, oldValue, this.normalMapping);
    }

    public boolean isParallaxMapping() {
        return parallaxMapping;
    }

    public void setParallaxMapping(boolean parallaxMapping) {
        boolean oldValue = this.parallaxMapping;
        this.parallaxMapping = parallaxMapping;
        propertyChangeSupport.firePropertyChange(PARALLAX_MAPPING, oldValue, this.parallaxMapping);
    }

    public boolean isDynamicShadowsPcfFiltering() {
        return dynamicShadowsPcfFiltering;
    }

    public void setDynamicShadowsPcfFiltering(boolean dynamicShadowsPcfFiltering) {
        boolean oldValue = this.dynamicShadowsPcfFiltering;
        this.dynamicShadowsPcfFiltering = dynamicShadowsPcfFiltering;
        propertyChangeSupport.firePropertyChange(DYNAMIC_SHADOWS_PCF_FILTERING, oldValue, this.dynamicShadowsPcfFiltering);
    }

    public boolean isCloudShadows() {
        return cloudShadows;
    }

    public void setCloudShadows(boolean cloudShadows) {
        boolean oldValue = this.cloudShadows;
        this.cloudShadows = cloudShadows;
        propertyChangeSupport.firePropertyChange(CLOUD_SHADOWS, oldValue, this.cloudShadows);
    }

    public boolean isLocalReflections() {
        return this.localReflections;
    }

    public void setLocalReflections(boolean localReflections) {
        boolean oldValue = this.localReflections;
        this.localReflections = localReflections;
        propertyChangeSupport.firePropertyChange(LOCAL_REFLECTIONS, oldValue, this.localReflections);
    }

    public boolean isInscattering() {
        return this.inscattering;
    }

    public void setInscattering(boolean inscattering) {
        boolean oldValue = this.inscattering;
        this.inscattering = inscattering;
        propertyChangeSupport.firePropertyChange(INSCATTERING, oldValue, this.inscattering);
    }

    public boolean isRenderNearest() {
        return renderNearest;
    }

    public void setRenderNearest(boolean renderNearest) {
        boolean oldValue = this.renderNearest;
        this.renderNearest = renderNearest;
        propertyChangeSupport.firePropertyChange(RENDER_NEAREST, oldValue, this.renderNearest);
    }
    public int getParticleEffectLimit() {
        return particleEffectLimit;
    }

    public void setParticleEffectLimit(int particleEffectLimit) {
        int oldLimit = this.particleEffectLimit;
        this.particleEffectLimit = particleEffectLimit;
        propertyChangeSupport.firePropertyChange(PARTICLE_EFFECT_LIMIT, oldLimit, this.particleEffectLimit);
    }

    public int getMeshLimit() {
        return meshLimit;
    }

    public void setMeshLimit(int meshLimit) {
        int oldLimit = this.meshLimit;
        this.meshLimit = meshLimit;
        propertyChangeSupport.firePropertyChange(MESH_LIMIT, oldLimit, this.meshLimit);
    }
    public boolean isVSync() {
        return this.vSync;
    }

    public void setVSync(boolean vsync) {
        boolean oldValue = this.vSync;
        this.vSync = vsync;
        propertyChangeSupport.firePropertyChange(V_SYNC, oldValue, this.vSync);
    }

    public RenderingDebugConfig getDebug() {
        return debug;
    }

    public int getFrameLimit() {
        return frameLimit;
    }

    public void setFrameLimit(int frameLimit) {
        int oldLimit = this.frameLimit;
        this.frameLimit = frameLimit;
        propertyChangeSupport.firePropertyChange(FRAME_LIMIT, oldLimit, this.frameLimit);
    }

    public int getFboScale() {
        return fboScale;
    }

    public void setFboScale(int fboScale) {
        int oldScale = this.fboScale;
        this.fboScale = fboScale;
        propertyChangeSupport.firePropertyChange(FBO_SCALE, oldScale, this.fboScale);
    }

    public int getUiScale() {
        if (uiScale == 0) {
            return 100;
        }
        return uiScale;
    }

    public void setUiScale(int uiScale) {
        int oldScale = this.uiScale;
        this.uiScale = uiScale;
        propertyChangeSupport.firePropertyChange(UI_SCALE, oldScale, this.uiScale);
    }

    public boolean isClampLighting() {
        return clampLighting;
    }

    public void setClampLighting(boolean clampLighting) {
        boolean oldValue = this.clampLighting;
        this.clampLighting = clampLighting;
        propertyChangeSupport.firePropertyChange(CLAMP_LIGHTING, oldValue, this.clampLighting);
    }

    public ScreenshotSize getScreenshotSize() {
        return screenshotSize;
    }

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

    public void setDumpShaders(boolean dumpShaders) {
        boolean oldValue = this.dumpShaders;
        this.dumpShaders = dumpShaders;
        propertyChangeSupport.firePropertyChange(DUMP_SHADERS, oldValue, this.dumpShaders);
    }

    public boolean isVolumetricFog() {
        return volumetricFog;
    }

    public void setVolumetricFog(boolean volumetricFog) {
        boolean oldValue = this.volumetricFog;
        this.volumetricFog = volumetricFog;
        propertyChangeSupport.firePropertyChange(VOLUMETRIC_FOG, oldValue, this.volumetricFog);
    }

}
