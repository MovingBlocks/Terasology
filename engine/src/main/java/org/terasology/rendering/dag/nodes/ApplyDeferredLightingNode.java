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

import org.terasology.assets.ResourceUrn;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTexture;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.WRITE_ONLY_GBUFFER;
import static org.terasology.rendering.opengl.OpenGLUtils.*;

/**
 * The ApplyDeferredLightingNode takes advantage of the information stored by previous nodes
 * in various buffers, especially the light accumulation buffer and lights up the otherwise
 * flatly-lit 3d scene.
 *
 * This node is integral to the deferred lighting technique.
 */
public class ApplyDeferredLightingNode extends AbstractNode {
    private static final ResourceUrn REFRACTIVE_REFLECTIVE = new ResourceUrn("engine:sceneReflectiveRefractive");
    private static final ResourceUrn DEFERRED_LIGHTING_MATERIAL = new ResourceUrn("engine:prog.lightBufferPass");

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    /**
     * Initializes an instance of this node.
     *
     * This method -must- be called once for this node to be fully operational.
     */
    @Override
    public void initialise() {

        addDesiredStateChange(new SetViewportToSizeOf(WRITE_ONLY_GBUFFER));
        addDesiredStateChange(new EnableMaterial(DEFERRED_LIGHTING_MATERIAL.toString()));

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTexture(
                textureSlot++, READ_ONLY_GBUFFER.getFbo().colorBufferTextureId,   DEFERRED_LIGHTING_MATERIAL, "texSceneOpaque"));
        addDesiredStateChange(new SetInputTexture(
                textureSlot++, READ_ONLY_GBUFFER.getFbo().depthStencilTextureId,  DEFERRED_LIGHTING_MATERIAL, "texSceneOpaqueDepth"));
        addDesiredStateChange(new SetInputTexture(
                textureSlot++, READ_ONLY_GBUFFER.getFbo().normalsBufferTextureId, DEFERRED_LIGHTING_MATERIAL, "texSceneOpaqueNormals"));
        addDesiredStateChange(new SetInputTexture(
                textureSlot,   READ_ONLY_GBUFFER.getFbo().lightBufferTextureId,   DEFERRED_LIGHTING_MATERIAL, "texSceneOpaqueLightBuffer"));
    }

    /**
     * Part of the deferred lighting technique, this method applies lighting through screen-space
     * calculations to the previously flat-lit world rendering, stored in the READ_ONLY_GBUFFER.
     * <p>
     * See http://en.wikipedia.org/wiki/Deferred_shading for more information on the general subject.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/applyDeferredLighting");

        WRITE_ONLY_GBUFFER.bind(); // TODO: remove and replace with a state change
        WRITE_ONLY_GBUFFER.setRenderBufferMask(true, true, true);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: this is necessary - but why? Verify in the shader.

        renderFullscreenQuad();

        displayResolutionDependentFBOs.swapReadWriteBuffers();
        READ_ONLY_GBUFFER.attachDepthBufferTo(displayResolutionDependentFBOs.get(REFRACTIVE_REFLECTIVE));

        PerformanceMonitor.endActivity();
    }
}
