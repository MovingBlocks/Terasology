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

import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.openvrprovider.OpenVRProvider;
import org.terasology.rendering.openvrprovider.OpenVRStereoRenderer;
import org.lwjgl.opengl.GL11;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.AbstractNode;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.FINAL;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.rendering.world.WorldRenderer.RenderingStage;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

public class CopyToVRFrameBuffersNode extends AbstractNode {
    public static final ResourceUrn OC_UNDISTORTED = new ResourceUrn("engine:ocUndistorted");
    private OpenVRProvider vrProvider = null;
    private OpenVRStereoRenderer vrRenderer = null;

    @In
    private WorldRenderer worldRenderer;

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

    public void setOpenVRProvider(OpenVRProvider providerToSet)
    {
        this.vrProvider = providerToSet;
    }


    @Override
    public void initialise() {
        renderingConfig = config.getRendering();
        renderingDebugConfig = renderingConfig.getDebug();

        finalPost = worldRenderer.getMaterial("engine:prog.post");
        debug = worldRenderer.getMaterial("engine:prog.debug");
        addDesiredStateChange(new BindFBO(FINAL));
    }

    /**
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/copyToVRFrameBuffers");
        if (!renderingDebugConfig.isEnabled()) {
            finalPost.enable();
        } else {
            debug.enable();
        }

        if (!renderingConfig.isOculusVrSupport()) {
            // Do nothing. We shouldn't create this node if isOculusVrSupport() is not set, so this
            // code is unlikely to be reached.
        } else {
            renderFinalStereoImage(worldRenderer.getCurrentRenderStage());
        }

        PerformanceMonitor.endActivity();
    }

    private void renderFinalStereoImage(RenderingStage renderingStage) {
        if (this.vrRenderer == null) {
            this.vrProvider.init();
            this.vrRenderer = new OpenVRStereoRenderer(this.vrProvider, 1280, 720);
        }
        switch (renderingStage) {
            case LEFT_EYE:
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                vrProvider.updateState();
                org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT(
                        org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT, vrRenderer.getTextureHandleForEyeFramebuffer(0));
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                renderFullscreenQuad(0, 0, 1280, 720);

                break;

            case RIGHT_EYE:
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT(
                        org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT, vrRenderer.getTextureHandleForEyeFramebuffer(1));
                renderFullscreenQuad(0, 0, 1280, 720);
                vrProvider.submitFrame();
                GL11.glFinish();
                break;
            case MONO:
                break;
        }
    }
}
