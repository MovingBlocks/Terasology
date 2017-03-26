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
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOManagerSubscriber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.DepthStencilTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.LightAccumulationTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.NormalsTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.WRITEONLY_GBUFFER;

/**
 * The ApplyDeferredLightingNode takes advantage of the information stored by previous nodes
 * in various buffers, especially the light accumulation buffer and lights up the otherwise
 * flatly-lit 3d scene.
 *
 * This node is integral to the deferred lighting technique.
 */
public class ApplyDeferredLightingNode extends AbstractNode implements FBOManagerSubscriber {
    private static final ResourceUrn DEFERRED_LIGHTING_MATERIAL = new ResourceUrn("engine:prog.lightBufferPass");

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private FBO writeOnlyGBufferFbo;

    /**
     * Initializes an instance of this node.
     *
     * This method -must- be called once for this node to be fully operational.
     */
    @Override
    public void initialise() {
        addDesiredStateChange(new BindFBO(WRITEONLY_GBUFFER, displayResolutionDependentFBOs));
        update(); // Cheeky way to initialise writeOnlyGBufferFbo
        displayResolutionDependentFBOs.subscribe(this);

        addDesiredStateChange(new EnableMaterial(DEFERRED_LIGHTING_MATERIAL.toString()));

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFBO(
                textureSlot++, READONLY_GBUFFER, ColorTexture,
                    displayResolutionDependentFBOs, DEFERRED_LIGHTING_MATERIAL, "texSceneOpaque"));
        addDesiredStateChange(new SetInputTextureFromFBO(
                textureSlot++, READONLY_GBUFFER, DepthStencilTexture,
                    displayResolutionDependentFBOs, DEFERRED_LIGHTING_MATERIAL, "texSceneOpaqueDepth"));
        addDesiredStateChange(new SetInputTextureFromFBO(
                textureSlot++, READONLY_GBUFFER, NormalsTexture,
                    displayResolutionDependentFBOs, DEFERRED_LIGHTING_MATERIAL, "texSceneOpaqueNormals"));
        addDesiredStateChange(new SetInputTextureFromFBO(
                textureSlot,   READONLY_GBUFFER, LightAccumulationTexture,
                    displayResolutionDependentFBOs, DEFERRED_LIGHTING_MATERIAL, "texSceneOpaqueLightBuffer"));
    }

    /**
     * Part of the deferred lighting technique, this method applies lighting through screen-space
     * calculations to the previously flat-lit world rendering, stored in the engine:sceneOpaque.
     * <p>
     * See http://en.wikipedia.org/wiki/Deferred_shading for more information on the general subject.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/applyDeferredLighting");

        writeOnlyGBufferFbo.setRenderBufferMask(true, true, true);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: this is necessary - but why? Verify in the shader.

        renderFullscreenQuad();

        displayResolutionDependentFBOs.swapReadWriteBuffers();

        PerformanceMonitor.endActivity();
    }

    @Override
    public void update() {
        writeOnlyGBufferFbo = displayResolutionDependentFBOs.get(WRITEONLY_GBUFFER);
    }
}
