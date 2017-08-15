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
import org.terasology.config.RenderingConfig;
import org.terasology.engine.SimpleUri;
import org.terasology.rendering.opengl.AbstractFBOsManager;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.SwappableFBO;

import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;

/**
 * TODO: Add javadocs
 * TODO: Better naming
 */
public class DisplayResolutionDependentFBOs extends AbstractFBOsManager {
    public static final SimpleUri FINAL_BUFFER = new SimpleUri("engine:fbo.finalBuffer");

    private SwappableFBO gBufferPair;

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
        FBO gBuffer1 = generateWithDimensions(new FBOConfig(new SimpleUri("engine:fbo.gBuffer1"), FULL_SCALE, FBO.Type.HDR)
                .useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer(), fullScale);
        FBO gBuffer2 = generateWithDimensions(new FBOConfig(new SimpleUri("engine:fbo.gBuffer2"), FULL_SCALE, FBO.Type.HDR)
                .useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer(), fullScale);
        generateWithDimensions(new FBOConfig(FINAL_BUFFER, FULL_SCALE, FBO.Type.DEFAULT), fullScale);

        gBufferPair = new SwappableFBO(gBuffer1, gBuffer2);
    }

    @Override
    public FBO request(FBOConfig fboConfig) {
        FBO fbo;
        SimpleUri fboName = fboConfig.getName();
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

        FBO readOnlyGBuffer = gBufferPair.getStaleFbo();
        if (readOnlyGBuffer.dimensions().areDifferentFrom(fullScale)) {
            regenerateFbos();
            notifySubscribers();
        }
    }

    private void regenerateFbos() {
        for (SimpleUri urn : fboConfigs.keySet()) {
            FBOConfig fboConfig = getFboConfig(urn);
            fboConfig.setDimensions(fullScale.multiplyBy(fboConfig.getScale()));
            FBO.recreate(get(urn), getFboConfig(urn));
        }
   }

    private void disposeAllFbos() {
        // TODO: This should be public, and should be called while disposing an object of this class, to prevent leaks.
        for (SimpleUri urn : fboConfigs.keySet()) {
            fboLookup.get(urn).dispose();
        }
        fboLookup.clear();
    }

    public SwappableFBO getGBufferPair() {
        return gBufferPair;
    }
}
