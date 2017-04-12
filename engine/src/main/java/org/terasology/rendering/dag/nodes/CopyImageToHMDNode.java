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
package org.terasology.rendering.dag.nodes;

import jopenvr.JOpenVRLibrary;
import org.terasology.assets.ResourceUrn;
import org.terasology.context.Context;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.FBOManagerSubscriber;
import org.terasology.rendering.openvrprovider.OpenVRProvider;
import org.lwjgl.opengl.GL11;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.monitoring.PerformanceMonitor;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.rendering.world.WorldRenderer.RenderingStage;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.FINAL_BUFFER;

public class CopyImageToHMDNode extends ConditionDependentNode implements FBOManagerSubscriber {
    private static final ResourceUrn LEFT_EYE_FBO = new ResourceUrn("engine:leftEye");
    private static final ResourceUrn RIGHT_EYE_FBO = new ResourceUrn("engine:rightEye");
    private static final ResourceUrn DEFAULT_TEXTURED_MATERIAL = new ResourceUrn("engine:prog.defaultTextured");
    // TODO: make these configurable options

    private OpenVRProvider vrProvider;
    private WorldRenderer worldRenderer;
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private FBO leftEyeFbo;
    private FBO rightEyeFbo;
    private FBO finalFbo;

    /**
     * Constructs an instance of this node. Specifically, initialize the vrProvider and pass the frame buffer
     * information for the vrProvider to use.
     */
    public CopyImageToHMDNode(Context context) {
        super(context);

        vrProvider = context.get(OpenVRProvider.class);
        requiresCondition(() -> (context.get(Config.class).getRendering().isVrSupport() && vrProvider.isInitialized()));

        if (this.isEnabled()) {
            worldRenderer = context.get(WorldRenderer.class);

            displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
            requiresFBO(new FBOConfig(LEFT_EYE_FBO, FULL_SCALE, FBO.Type.DEFAULT).useDepthBuffer(), displayResolutionDependentFBOs);
            requiresFBO(new FBOConfig(RIGHT_EYE_FBO, FULL_SCALE, FBO.Type.DEFAULT).useDepthBuffer(), displayResolutionDependentFBOs);
            update(); // Cheeky way to initialise finalFbo, leftEyeFbo, rightEyeFbo
            displayResolutionDependentFBOs.subscribe(this);

            vrProvider.texType[0].handle = leftEyeFbo.colorBufferTextureId;
            vrProvider.texType[0].eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
            vrProvider.texType[0].eType = JOpenVRLibrary.EGraphicsAPIConvention.EGraphicsAPIConvention_API_OpenGL;
            vrProvider.texType[0].write();
            vrProvider.texType[1].handle = rightEyeFbo.colorBufferTextureId;
            vrProvider.texType[1].eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
            vrProvider.texType[1].eType = JOpenVRLibrary.EGraphicsAPIConvention.EGraphicsAPIConvention_API_OpenGL;
            vrProvider.texType[1].write();

            addDesiredStateChange(new EnableMaterial(DEFAULT_TEXTURED_MATERIAL));
        }
    }

    /**
     * Actually perform the rendering-related tasks.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/copyImageToHMD");
        finalFbo.bindTexture();
        renderFinalStereoImage(worldRenderer.getCurrentRenderStage());
        PerformanceMonitor.endActivity();
    }

    private void renderFinalStereoImage(RenderingStage renderingStage) {
        // TODO: verify if we can use glCopyTexSubImage2D instead of pass-through shaders,
        // TODO: in terms of code simplicity and performance.
        switch (renderingStage) {
            case LEFT_EYE:
                vrProvider.updateState();
                leftEyeFbo.bind();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                renderFullscreenQuad();
                break;

            case RIGHT_EYE:
                rightEyeFbo.bind();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                renderFullscreenQuad();
                vrProvider.submitFrame();
                GL11.glFinish();
                break;
        }

        // Bind the default FBO. The DAG does not recognize that this node has
        // bound a different FBO, so as far as it is concerned, FBO 0 is still
        // bound. As a result, without the below line, the image is only copied
        // to the HMD - not to the screen as we would like. To get around this,
        // we bind the default FBO here at the end.  This is a bit brittle
        // because it assumes that FBO 0 is bound before this node is run.
        // TODO: break this node into two different nodes that use addDesiredStateChange(BindFBO...))
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }

    @Override
    public void update() {
        finalFbo = displayResolutionDependentFBOs.get(FINAL_BUFFER);
        leftEyeFbo = displayResolutionDependentFBOs.get(LEFT_EYE_FBO);
        rightEyeFbo = displayResolutionDependentFBOs.get(RIGHT_EYE_FBO);
    }
}
