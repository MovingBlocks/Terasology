package org.terasology.config;

import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.terasology.game.CoreRegistry;
import org.terasology.rendering.world.WorldRenderer;

/**
 * @author Immortius
 */
public class RenderingConfig {
    private PixelFormat pixelFormat = new PixelFormat().withDepthBits(24);
    private int windowWidth = 1280;
    private int windowHeight = 720;
    private boolean fullscreen = false;
    private int activeViewDistanceMode = 0;
    private boolean flickeringLight = false;
    private boolean animateGrass = false;
    private boolean animateWater = false;
    private float fieldOfView = 90;
    private boolean cameraBobbing = true;
    private boolean renderPlacingBox = true;
    private int blurIntensity = 3;
    private boolean reflectiveWater = false;
    private boolean vignette = true;
    private boolean motionBlur = false;
    private boolean ssao = false;
    private boolean filmGrain = true;
    private boolean outline = true;
    private boolean lightShafts = false;
    private boolean eyeAdaptation = true;
    private boolean bloom = false;
    private boolean dynamicShadows = false;
    private boolean oculusVrSupport = false;
    private int maxTextureAtlasResolution = 4096;
    private int maxChunksUsedForShadowMapping = 1024;
    private int shadowMapResolution = 1024;
    private boolean normalMapping = false;
    private boolean parallaxMapping = false;
    private boolean dynamicShadowsPcfFiltering = false;
    private boolean volumetricFog = false;

    public int getBlurRadius() {
        return Math.max(1, blurIntensity);
    }

    public PixelFormat getPixelFormat() {
        return pixelFormat;
    }

    public void setPixelFormat(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
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

    public int getActiveViewDistanceMode() {
        return activeViewDistanceMode;
    }

    public void setActiveViewDistanceMode(int activeViewDistanceMode) {
        this.activeViewDistanceMode = activeViewDistanceMode;
        // TODO: Remove this, switch to a property change listener

        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        if (worldRenderer != null) {
            worldRenderer.changeViewDistance(getActiveViewingDistance());
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
        return motionBlur && !oculusVrSupport;
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

    public void setEyeAdaptation(boolean eyeAdapting) {
        this.eyeAdaptation = eyeAdapting;
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

    public int getActiveViewingDistance() {
        switch (activeViewDistanceMode) {
            case 1:
                return 16;
            case 2:
                return 32;
            case 3:
                return 64;
            default:
                return 8;
        }
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

    public boolean isVolumetricFog() {
        return volumetricFog;
    }

    public void setVolumetricFog(boolean volumetricFog) {
        this.volumetricFog = volumetricFog;
    }
}
