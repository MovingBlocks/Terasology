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
package org.terasology.rendering.dag.nodes;

import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.AbstractNode;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.FINAL;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;

import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * TODO: Add diagram of this node
 */
public class FinalPostProcessingNode extends AbstractNode {

    @In
    private Config config;

    @In
    private ScreenGrabber screenGrabber;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private RenderingDebugConfig renderingDebugConfig;
    private RenderingConfig renderingConfig;
    private FBO.Dimensions fullScale;
    private Material finalPost;
    private Material debug;

    @Override
    public void initialise() {
        renderingConfig = config.getRendering();
        renderingDebugConfig = renderingConfig.getDebug();
        finalPost = worldRenderer.getMaterial("engine:prog.post"); // TODO: rename shader to finalPost
        debug = worldRenderer.getMaterial("engine:prog.debug");
        addDesiredStateChange(new BindFBO(FINAL));
    }

    /**
     * If each is enabled through the rendering settings, this method
     * adds depth-of-field blur, motion blur and film grain to the rendering
     * obtained so far. If OculusVR support is enabled, it composes (over two
     * calls) the images for each eye into a single image, and applies a distortion
     * pattern to each, to match the optics in the OculusVR headset.
     * <p>
     * Finally, depending on whether a screen shot is requested, it optionally
     * instructs the FrameBuffersManager to save it to a file.
     * <p>
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/finalPostProcessing");

        // Choose the proper material
        // TODO: split debug off into a separate node.
        if (!renderingDebugConfig.isEnabled()) {
            finalPost.enable();
        } else {
            debug.enable();
        }

        // Render
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        fullScale = READ_ONLY_GBUFFER.dimensions();
        renderFullscreenQuad(0, 0, fullScale.width(), fullScale.height());
        if (!screenGrabber.isNotTakingScreenshot()) {
            screenGrabber.saveScreenshot();
        }
        PerformanceMonitor.endActivity();
    }
}
