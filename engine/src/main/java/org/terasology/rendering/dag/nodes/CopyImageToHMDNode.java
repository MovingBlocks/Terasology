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

import jopenvr.JOpenVRLibrary;
import org.terasology.assets.ResourceUrn;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.openvrprovider.OpenVRProvider;
import org.lwjgl.opengl.GL11;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import static org.lwjgl.opengl.GL11.*;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.rendering.world.WorldRenderer.RenderingStage;

import static org.terasology.rendering.opengl.DefaultDynamicFBOs.FINAL;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;

public class CopyImageToHMDNode extends ConditionDependentNode {
    private static final ResourceUrn LEFT_EYE_FBO = new ResourceUrn("engine:leftEye");
    private static final ResourceUrn RIGHT_EYE_FBO = new ResourceUrn("engine:rightEye");
    // TODO: make these configurable options

    @In
    private OpenVRProvider vrProvider;

    @In
    private WorldRenderer worldRenderer;

    @In
    private Config config;

    @In
    private ScreenGrabber screenGrabber;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private RenderingConfig renderingConfig;
    private FBO leftEye;
    private FBO rightEye;

    @Override
    public void initialise() {
        renderingConfig = config.getRendering();
        requiresCondition(() -> (renderingConfig.isVrSupport()
                && vrProvider.isInitialized()));
        leftEye = requiresFBO(new FBOConfig(LEFT_EYE_FBO, FULL_SCALE,
                FBO.Type.DEFAULT).useDepthBuffer(), displayResolutionDependentFBOs);
        rightEye = requiresFBO(new FBOConfig(RIGHT_EYE_FBO, FULL_SCALE,
                FBO.Type.DEFAULT).useDepthBuffer(), displayResolutionDependentFBOs);
        if (vrProvider != null) {
            vrProvider.texType[0].handle = leftEye.colorBufferTextureId;
            vrProvider.texType[0].eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
            vrProvider.texType[0].eType = JOpenVRLibrary.EGraphicsAPIConvention.EGraphicsAPIConvention_API_OpenGL;
            vrProvider.texType[0].write();
            vrProvider.texType[1].handle = rightEye.colorBufferTextureId;
            vrProvider.texType[1].eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
            vrProvider.texType[1].eType = JOpenVRLibrary.EGraphicsAPIConvention.EGraphicsAPIConvention_API_OpenGL;
            vrProvider.texType[1].write();
        }
        addDesiredStateChange(new EnableMaterial("engine:prog.defaultTextured"));
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/copyImageToHMD");
        FINAL.bindTexture();
        renderFinalStereoImage(worldRenderer.getCurrentRenderStage());
        PerformanceMonitor.endActivity();
    }

    private void renderFinalStereoImage(RenderingStage renderingStage) {
        // TODO: verify if we can use glCopyTexSubImage2D instead of pass-through shaders,
        // TODO: in terms of code simplicity and performance.
        switch (renderingStage) {
            case LEFT_EYE:
                vrProvider.updateState();
                leftEye.bind();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                renderFullscreenQuad();
                break;

            case RIGHT_EYE:
                rightEye.bind();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                renderFullscreenQuad();
                vrProvider.submitFrame();
                GL11.glFinish();
                break;
        }
    }
}
