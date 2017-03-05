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

import org.lwjgl.opengl.Display;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
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
public class DisplayResolutionDependentFBOs extends AbstractFBOsManager {
    private FBO.Dimensions fullScale;
    private RenderingConfig renderingConfig;
    private ScreenGrabber screenGrabber;

    public DisplayResolutionDependentFBOs(Context context) {
        renderingConfig = context.get(Config.class).getRendering();
        screenGrabber = context.get(ScreenGrabber.class);
        fullScale = new FBO.Dimensions(Display.getWidth(), Display.getHeight());
        generateDefaultFBOs();
    }

    private void generateDefaultFBOs() {
        generateDefaultFBO(READ_ONLY_GBUFFER);
        generateDefaultFBO(WRITE_ONLY_GBUFFER);
        generateDefaultFBO(FINAL);
    }

    private void generateDefaultFBO(DefaultDynamicFBOs defaultDynamicFBO) {
        FBOConfig fboConfig = defaultDynamicFBO.getConfig();
        FBO fbo = generateWithDimensions(fboConfig, fullScale.multiplyBy(fboConfig.getScale()));
        defaultDynamicFBO.setFbo(fbo);
        defaultDynamicFBO.setFrameBufferManager(this);
    }

    @Override
    public FBO request(FBOConfig fboConfig) {
        FBO fbo;
        ResourceUrn fboName = fboConfig.getName();
        if (fboConfigs.containsKey(fboName)) {
            if (!fboConfig.equals(fboConfigs.get(fboName))) {
                throw new IllegalArgumentException("Requested FBO is already available with different configuration");
            }
            fbo = fboLookup.get(fboConfig.getName());
        } else {
            fbo = generateWithDimensions(fboConfig, fullScale.multiplyBy(fboConfig.getScale()));
        }
        retain(fboName);
        return fbo;
    }

    private void updateFullScale() {
        if (screenGrabber.isNotTakingScreenshot()) {
            fullScale = new FBO.Dimensions(Display.getWidth(), Display.getHeight());
        } else {
            fullScale = new FBO.Dimensions(
                    renderingConfig.getScreenshotSize().getWidth(Display.getWidth()),
                    renderingConfig.getScreenshotSize().getHeight(Display.getHeight())
            );
        }

        fullScale.multiplySelfBy(renderingConfig.getFboScale() / 100f);
    }

    /**
     * Invoked before real-rendering starts
     * TODO: how about completely removing this, and make Display observable and this FBM as an observer
     */
    public void update() {
        updateFullScale();
        if (READ_ONLY_GBUFFER.getFbo().dimensions().areDifferentFrom(fullScale)) {
            disposeAllFBOs();
            createFBOs();
            updateDefaultFBOs();
            notifySubscribers();
        }
    }

    private void disposeAllFBOs() {
        for (ResourceUrn urn : fboConfigs.keySet()) {
            fboLookup.get(urn).dispose();
        }
        fboLookup.clear();
    }

    private void createFBOs() {
        for (FBOConfig fboConfig : fboConfigs.values()) {
            generateWithDimensions(fboConfig, fullScale.multiplyBy(fboConfig.getScale()));
        }
    }

    private void updateDefaultFBOs() {
        READ_ONLY_GBUFFER.setFbo(fboLookup.get(READ_ONLY_GBUFFER.getName()));
        WRITE_ONLY_GBUFFER.setFbo(fboLookup.get(WRITE_ONLY_GBUFFER.getName()));
        FINAL.setFbo(fboLookup.get(FINAL.getName()));
    }

    // TODO: Pairing FBOs for swapping functionality
    public void swapReadWriteBuffers() {
        FBO fbo = READ_ONLY_GBUFFER.getFbo();
        READ_ONLY_GBUFFER.setFbo(WRITE_ONLY_GBUFFER.getFbo());
        WRITE_ONLY_GBUFFER.setFbo(fbo);
        fboLookup.put(READ_ONLY_GBUFFER.getName(), READ_ONLY_GBUFFER.getFbo());
        fboLookup.put(WRITE_ONLY_GBUFFER.getName(), WRITE_ONLY_GBUFFER.getFbo());
        notifySubscribers();
    }
}
