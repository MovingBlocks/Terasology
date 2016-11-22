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

import org.lwjgl.opengl.GL13;
import org.terasology.assets.ResourceUrn;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

import static org.lwjgl.opengl.GL11.*;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.WRITE_ONLY_GBUFFER;
import static org.terasology.rendering.opengl.OpenGLUtils.bindDisplay;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.OpenGLUtils.setViewportToSizeOf;

/**
 * TODO
 */
public class ApplyDeferredLightingNode extends AbstractNode {
    private static final ResourceUrn REFRACTIVE_REFLECTIVE = new ResourceUrn("engine:sceneReflectiveRefractive");

    @In
    private WorldRenderer worldRenderer;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private Material lightBufferPass;
    private FBO sceneReflectiveRefractive;


    @Override
    public void initialise() {
        lightBufferPass = worldRenderer.getMaterial("engine:prog.lightBufferPass");
    }


    /**
     * Part of the deferred lighting technique, this method applies lighting through screen-space
     * calculations to the previously flat-lit world rendering stored in the primary FBO.   // TODO: rename sceneOpaque* FBOs to primaryA/B
     * <p>
     * See http://en.wikipedia.org/wiki/Deferred_shading as a starting point.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/applyDeferredLighting");

        int texId = 0;

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        READ_ONLY_GBUFFER.bindTexture();
        lightBufferPass.setInt("texSceneOpaque", texId++, true);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        READ_ONLY_GBUFFER.bindDepthTexture();
        lightBufferPass.setInt("texSceneOpaqueDepth", texId++, true);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        READ_ONLY_GBUFFER.bindNormalsTexture();
        lightBufferPass.setInt("texSceneOpaqueNormals", texId++, true);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        READ_ONLY_GBUFFER.bindLightBufferTexture();
        lightBufferPass.setInt("texSceneOpaqueLightBuffer", texId, true);

        WRITE_ONLY_GBUFFER.bind();
        WRITE_ONLY_GBUFFER.setRenderBufferMask(true, true, true);

        setViewportToSizeOf(WRITE_ONLY_GBUFFER);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

        renderFullscreenQuad();

        bindDisplay();     // TODO: verify this is necessary
        setViewportToSizeOf(READ_ONLY_GBUFFER); // TODO: verify this is necessary

        sceneReflectiveRefractive = displayResolutionDependentFBOs.get(REFRACTIVE_REFLECTIVE);
        displayResolutionDependentFBOs.swapReadWriteBuffers();
        READ_ONLY_GBUFFER.attachDepthBufferTo(sceneReflectiveRefractive);

        PerformanceMonitor.endActivity();
    }
}
