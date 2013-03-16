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
    private int viewDistanceNear = 8;
    private int viewDistanceModerate = 16;
    private int viewDistanceFar = 32;
    private int viewDistanceUltra = 48;
    private int activeViewDistanceMode = 0;
    private int maxChunkVBOs = 512;
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

    public int getViewDistanceNear() {
        return viewDistanceNear;
    }

    public void setViewDistanceNear(int viewDistanceNear) {
        this.viewDistanceNear = viewDistanceNear;
    }

    public int getViewDistanceModerate() {
        return viewDistanceModerate;
    }

    public void setViewDistanceModerate(int viewDistanceModerate) {
        this.viewDistanceModerate = viewDistanceModerate;
    }

    public int getViewDistanceFar() {
        return viewDistanceFar;
    }

    public void setViewDistanceFar(int viewDistanceFar) {
        this.viewDistanceFar = viewDistanceFar;
    }

    public int getViewDistanceUltra() {
        return viewDistanceUltra;
    }

    public void setViewDistanceUltra(int viewDistanceUltra) {
        this.viewDistanceUltra = viewDistanceUltra;
    }

    public int getActiveViewDistanceMode() {
        return activeViewDistanceMode;
    }

    public void setActiveViewDistanceMode(int activeViewDistanceMode) {
        this.activeViewDistanceMode = activeViewDistanceMode;
        // TODO: Remove this, switch to a property change listener

        int chunksToLoad = getActiveViewingDistance() * getActiveViewingDistance();
        setMaxChunkVBOs(chunksToLoad >= 512 ? 512 : chunksToLoad);
        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        if (worldRenderer != null) {
            worldRenderer.changeViewDistance(getActiveViewingDistance());
        }
    }

    public int getMaxChunkVBOs() {
        return maxChunkVBOs;
    }

    public void setMaxChunkVBOs(int maxChunkVBOs) {
        this.maxChunkVBOs = maxChunkVBOs;
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
        return motionBlur;
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

    public int getActiveViewingDistance() {
        switch (activeViewDistanceMode) {
            case 1:
                return viewDistanceModerate;
            case 2:
                return viewDistanceFar;
            case 3:
                return viewDistanceUltra;
            default:
                return viewDistanceNear;
        }
    }
}
