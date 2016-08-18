/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.opengl.fbms;

import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.rendering.oculusVr.OculusVrHelper;
import org.terasology.rendering.opengl.AbstractFBOsManager;
import org.terasology.rendering.opengl.DefaultDynamicFBOs;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.FINAL;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.WRITE_ONLY_GBUFFER;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.ScreenGrabber;

/**
 * TODO: Add javadocs
 * TODO: Better naming
 */
public class DynamicFBOsManager extends AbstractFBOsManager {
    private FBO.Dimensions fullScale;
    private RenderingConfig renderingConfig;
    private ScreenGrabber screenGrabber;

    public DynamicFBOsManager(Context context) {
        this.renderingConfig = context.get(Config.class).getRendering();

        fullScale = new FBO.Dimensions(Display.getWidth(), Display.getHeight());
        generateDefaultFBOs();
    }

    @Override
    public void request(FBOConfig fboConfig) {
        ResourceUrn fboName = fboConfig.getName();
        if (fboConfigs.containsKey(fboName)) {
            if (!fboConfig.equals(fboConfigs.get(fboName))) {
                throw new IllegalArgumentException("Requested FBO is already available with different configuration");
            }
        } else {
            generate(fboConfig, fullScale.multiplyBy(fboConfig.getScale()));
        }
        retain(fboName);
    }

    private void generateDefaultFBOs() {
        generateDefaultFBO(READ_ONLY_GBUFFER);
        generateDefaultFBO(WRITE_ONLY_GBUFFER);
        generateDefaultFBO(FINAL);
    }

    private void generateDefaultFBO(DefaultDynamicFBOs defaultDynamicFBO) {
        FBOConfig fboConfig = defaultDynamicFBO.getConfig();
        FBO fbo = generate(fboConfig, fullScale.multiplyBy(fboConfig.getScale()));
        defaultDynamicFBO.setFbo(fbo);
    }

    /**
     * Invoked before real-rendering starts
     * TODO: how about completely removing this, and make Display observable and this FBM as an observer
     */
    public void update() {
        updateFullScale();
        if (get(READ_ONLY_GBUFFER.getName()).dimensions().areDifferentFrom(fullScale)) {
            disposeAllFBOs();
            createFBOs();
            updateDefaultFBOs();
        }
    }

    private void updateDefaultFBOs() {
        READ_ONLY_GBUFFER.setFbo(fboLookup.get(READ_ONLY_GBUFFER.getName()));
        WRITE_ONLY_GBUFFER.setFbo(fboLookup.get(WRITE_ONLY_GBUFFER.getName()));
        FINAL.setFbo(fboLookup.get(FINAL.getName()));
    }

    private void disposeAllFBOs() {
        for (ResourceUrn urn : fboConfigs.keySet()) {
            fboLookup.get(urn).dispose();
            fboLookup.remove(urn);
        }
        fboLookup.clear();
    }

    private void createFBOs() {
        for (FBOConfig fboConfig : fboConfigs.values()) {
            generate(fboConfig, fullScale.multiplyBy(fboConfig.getScale()));
        }

        notifySubscribers();
    }


    /**
     * Returns the content of the color buffer of the FBO "sceneFinal", from GPU memory as a ByteBuffer.
     * If the FBO "sceneFinal" is unavailable, returns null.
     *
     * @return a ByteBuffer or null
     */
    public ByteBuffer getSceneFinalRawData() { 
        FBO fboSceneFinal = get(READ_ONLY_GBUFFER.getName());
        if (fboSceneFinal == null) {
            logger.error("FBO sceneFinal is unavailable: cannot return data from it.");
            return null;
        }

        ByteBuffer buffer = BufferUtils.createByteBuffer(fboSceneFinal.width() * fboSceneFinal.height() * 4);

        fboSceneFinal.bindTexture();
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        FBO.unbindTexture();

        return buffer;
    }

    private void updateFullScale() {
        if (screenGrabber.isNotTakingScreenshot()) {
            fullScale = new FBO.Dimensions(Display.getWidth(), Display.getHeight());
            if (renderingConfig.isOculusVrSupport()) {
                fullScale.multiplySelfBy(OculusVrHelper.getScaleFactor());
            }
        } else {
            fullScale = new FBO.Dimensions(
                    renderingConfig.getScreenshotSize().getWidth(Display.getWidth()),
                    renderingConfig.getScreenshotSize().getHeight(Display.getHeight())
            );
        }

        fullScale.multiplySelfBy(renderingConfig.getFboScale() / 100f);
    }

    public void setScreenGrabber(ScreenGrabber screenGrabber) {
        this.screenGrabber = screenGrabber;
    }

    public void swapReadWriteBuffers() {
        FBO fbo = READ_ONLY_GBUFFER.getFbo();
        READ_ONLY_GBUFFER.setFbo(WRITE_ONLY_GBUFFER.getFbo());
        WRITE_ONLY_GBUFFER.setFbo(fbo);
        fboLookup.put(READ_ONLY_GBUFFER.getName(), READ_ONLY_GBUFFER.getFbo());
        fboLookup.put(WRITE_ONLY_GBUFFER.getName(), WRITE_ONLY_GBUFFER.getFbo());
    }
}
