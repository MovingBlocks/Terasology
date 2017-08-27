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

import org.terasology.context.Context;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.assets.ResourceUrn;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo;
import static org.lwjgl.opengl.GL11.glViewport;
import org.terasology.monitoring.PerformanceMonitor;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import org.lwjgl.opengl.Display;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.engine.SimpleUri;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.SwapGBuffers;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.world.WorldRenderer;

import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.DepthStencilTexture;

public class CopyDepthNode extends AbstractNode {
    private static final ResourceUrn COPY_DEPTH_MATERIAL_URN = new ResourceUrn("engine:prog.copyDepth");
    public static final SimpleUri COPY_DEPTH_FBO_URI = new SimpleUri("engine:fbo.sceneCopyDepth");

    private FBO lastUpdatedGBuffer;
    private FBO copyDepthFbo;

    private Material copyDepthMaterial;

    private SubmersibleCamera activeCamera;

    public CopyDepthNode(Context context)  {
        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        lastUpdatedGBuffer = displayResolutionDependentFBOs.getGBufferPair().getLastUpdatedFbo();
        copyDepthFbo = requiresFBO(new FBOConfig(COPY_DEPTH_FBO_URI, FULL_SCALE, FBO.Type.HDR).useDepthBuffer(), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFbo(copyDepthFbo));
        lastUpdatedGBuffer.attachDepthBufferTo(copyDepthFbo);

        activeCamera = context.get(WorldRenderer.class).getActiveCamera();

        addDesiredStateChange(new EnableMaterial(COPY_DEPTH_MATERIAL_URN));
        copyDepthMaterial = getMaterial(COPY_DEPTH_MATERIAL_URN);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, lastUpdatedGBuffer, ColorTexture,
            displayResolutionDependentFBOs, COPY_DEPTH_MATERIAL_URN, "texSceneOpaque"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, lastUpdatedGBuffer, DepthStencilTexture,
            displayResolutionDependentFBOs, COPY_DEPTH_MATERIAL_URN, "texSceneOpaqueDepth"));

        //only because water becomes invisible otherwise
        addDesiredStateChange(new SwapGBuffers(displayResolutionDependentFBOs.getGBufferPair()));
    }

    @Override
    public void process()  {

        PerformanceMonitor.startActivity("rendering/copyDepth");
        // The way things are set-up right now, we can have FBOs that are not the same size as the display (if scale != 100%).
        // However, when drawing the final image to the screen, we always want the viewport to match the size of display,
        // and not that of some FBO. Hence, we are manually setting the viewport via glViewport over here.

        copyDepthMaterial.setFloat("zNear",activeCamera.getzNear(),true);
        copyDepthMaterial.setFloat("zFar",activeCamera.getzFar(),true);

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        renderFullscreenQuad();
        PerformanceMonitor.endActivity();

    }
}
