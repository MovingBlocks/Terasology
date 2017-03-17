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
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
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
    private FBO.Dimensions fullScale;
    private RenderingConfig renderingConfig;
    private ScreenGrabber screenGrabber;

    public DisplayResolutionDependentFBOs(Context context) {
        renderingConfig = context.get(Config.class).getRendering();
        screenGrabber = context.get(ScreenGrabber.class);

        updateFullScale();
        generateDefaultFBOs();
    }

    private void generateDefaultFBOs() {
        generateWithDimensions(new FBOConfig(new ResourceUrn("engine:sceneOpaque"), FULL_SCALE, FBO.Type.HDR)
                .useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer(), fullScale);
        generateWithDimensions(new FBOConfig(new ResourceUrn("engine:sceneOpaquePingPong"), FULL_SCALE, FBO.Type.HDR)
                .useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer(), fullScale);
        generateWithDimensions(new FBOConfig(new ResourceUrn("engine:sceneFinal"), FULL_SCALE, FBO.Type.DEFAULT), fullScale);
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
        if (get(new ResourceUrn("engine:sceneOpaque")).dimensions().areDifferentFrom(fullScale)) {
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
        FBO fbo = get(new ResourceUrn("engine:sceneOpaque"));
        fboLookup.put(new ResourceUrn("engine:sceneOpaque"), get(new ResourceUrn("engine:sceneOpaquePingPong")));
        fboLookup.put(new ResourceUrn("engine:sceneOpaquePingPong"), fbo);
        notifySubscribers();
    }
}