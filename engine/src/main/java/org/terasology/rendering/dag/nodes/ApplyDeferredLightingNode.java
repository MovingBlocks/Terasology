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
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo;
import org.terasology.rendering.dag.stateChanges.SwapGBuffers;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.SwappableFBO;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.DepthStencilTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.LightAccumulationTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.NormalsTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * The ApplyDeferredLightingNode takes advantage of the information stored by previous nodes
 * in various buffers, especially the light accumulation buffer and lights up the otherwise
 * flatly-lit 3d scene.
 *
 * This node is integral to the deferred lighting technique.
 */
public class ApplyDeferredLightingNode extends AbstractNode {
    private static final ResourceUrn DEFERRED_LIGHTING_MATERIAL_URN = new ResourceUrn("engine:prog.lightBufferPass");

    public ApplyDeferredLightingNode(Context context) {
        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        SwappableFBO gBufferPair = displayResolutionDependentFBOs.getGBufferPair();

        addDesiredStateChange(new SwapGBuffers(gBufferPair));

        addDesiredStateChange(new BindFbo(gBufferPair.getWriteOnlyFbo()));

        addDesiredStateChange(new EnableMaterial(DEFERRED_LIGHTING_MATERIAL_URN));

        FBO readOnlyGBuffer = displayResolutionDependentFBOs.getGBufferPair().getReadOnlyFbo();

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, readOnlyGBuffer, ColorTexture,
            displayResolutionDependentFBOs, DEFERRED_LIGHTING_MATERIAL_URN, "texSceneOpaque"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, readOnlyGBuffer, DepthStencilTexture,
            displayResolutionDependentFBOs, DEFERRED_LIGHTING_MATERIAL_URN, "texSceneOpaqueDepth"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, readOnlyGBuffer, NormalsTexture,
            displayResolutionDependentFBOs, DEFERRED_LIGHTING_MATERIAL_URN, "texSceneOpaqueNormals"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot,   readOnlyGBuffer, LightAccumulationTexture,
            displayResolutionDependentFBOs, DEFERRED_LIGHTING_MATERIAL_URN, "texSceneOpaqueLightBuffer"));
    }

    /**
     * Part of the deferred lighting technique, this method applies lighting through screen-space
     * calculations to the previously flat-lit world rendering, stored in the engine:writeOnlyGBuffer.
     * <p>
     * See http://en.wikipedia.org/wiki/Deferred_shading for more information on the general subject.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/applyDeferredLighting");

        // Actual Node Processing

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }
}
