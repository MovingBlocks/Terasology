/*
 * Copyright 2017 MovingBlocks
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
import org.terasology.config.RenderingConfig;
import org.terasology.rendering.opengl.AbstractFBOsManager;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;

import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.ScreenGrabber;

/**
 * TODO: Add javadocs
 * TODO: Better naming
 */
public class DisplayResolutionDependentFBOs extends AbstractFBOsManager {
    public static final ResourceUrn READONLY_GBUFFER = new ResourceUrn("engine:sceneOpaque");
    public static final ResourceUrn WRITEONLY_GBUFFER = new ResourceUrn("engine:sceneOpaquePingPong");
    public static final ResourceUrn FINAL_BUFFER = new ResourceUrn("engine:sceneFinal");

    private FBO.Dimensions fullScale;
    private RenderingConfig renderingConfig;
    private ScreenGrabber screenGrabber;

    public DisplayResolutionDependentFBOs(RenderingConfig renderingConfig, ScreenGrabber screenGrabber) {
        this.renderingConfig = renderingConfig;
        this.screenGrabber = screenGrabber;

        updateFullScale();
        generateDefaultFBOs();
    }

    private void generateDefaultFBOs() {
        generateWithDimensions(new FBOConfig(READONLY_GBUFFER, FULL_SCALE, FBO.Type.HDR)
                .useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer(), fullScale);
        generateWithDimensions(new FBOConfig(WRITEONLY_GBUFFER, FULL_SCALE, FBO.Type.HDR)
                .useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer(), fullScale);
        generateWithDimensions(new FBOConfig(FINAL_BUFFER, FULL_SCALE, FBO.Type.DEFAULT), fullScale);
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

        FBO sceneOpaqueFbo = get(READONLY_GBUFFER);
        if (sceneOpaqueFbo.dimensions().areDifferentFrom(fullScale)) {
            disposeAllFBOs();
            createFBOs();
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

    // TODO: Pairing FBOs for swapping functionality
    public void swapReadWriteBuffers() {
        FBO fbo = get(READONLY_GBUFFER);
        fboLookup.put(READONLY_GBUFFER, get(WRITEONLY_GBUFFER));
        fboLookup.put(WRITEONLY_GBUFFER, fbo);
        notifySubscribers();
    }
}
