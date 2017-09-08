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

import org.terasology.assets.ResourceUrn;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBO.Type;
import org.terasology.rendering.opengl.FBOConfig;

import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.DepthStencilTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;

public class CopyDepthNode extends AbstractNode {
    private static final ResourceUrn COPY_DEPTH_MATERIAL_URN = new ResourceUrn("engine:prog.copyDepth");
    public static final SimpleUri COPY_DEPTH_FBO_URI = new SimpleUri("engine:fbo.copyDepth");

    private FBO lastUpdatedGBuffer;
    private FBO outputFbo;
    private Material copyDepthMaterial;

    public CopyDepthNode(Context context)  {
        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        lastUpdatedGBuffer = displayResolutionDependentFBOs.getGBufferPair().getLastUpdatedFbo();
        outputFbo = requiresFBO(new FBOConfig(COPY_DEPTH_FBO_URI, FULL_SCALE, Type.NO_COLOR).useDepthBuffer(), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFbo(outputFbo));
        addDesiredStateChange(new SetViewportToSizeOf(outputFbo));

        addDesiredStateChange(new EnableMaterial(COPY_DEPTH_MATERIAL_URN));
        copyDepthMaterial = getMaterial(COPY_DEPTH_MATERIAL_URN);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, lastUpdatedGBuffer, DepthStencilTexture,
            displayResolutionDependentFBOs, COPY_DEPTH_MATERIAL_URN, "texSceneOpaqueDepth"));
    }

    @Override
    public void process()  {

        PerformanceMonitor.startActivity("rendering/copyDepth");
        renderFullscreenQuad();
        PerformanceMonitor.endActivity();
    }
}
