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
package org.terasology.rendering.dag;

import org.lwjgl.opengl.GL13;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.logic.LightComponent;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;
import org.terasology.rendering.world.WorldRenderer;

import static org.lwjgl.opengl.GL11.*;
import static org.terasology.rendering.opengl.OpenGLUtils.*;

/**
 * TODO: Diagram of this node
 */
public class DirectionalLightsNode implements Node {

    @In
    private BackdropProvider backdropProvider;

    @In
    private WorldRenderer worldRenderer;

    @In
    private FrameBuffersManager frameBuffersManager;

    // TODO: Review this? (What are we doing with a component not attached to an entity?)
    private LightComponent mainDirectionalLight = new LightComponent();

    private Camera playerCamera;
    private FBO sceneOpaque;
    private Material lightGeometryShader;
    private Material lightBufferPass;

    @Override
    public void initialise() {
        playerCamera = worldRenderer.getActiveCamera();
        sceneOpaque = frameBuffersManager.getFBO("sceneOpaque");
        lightGeometryShader = worldRenderer.getMaterial("engine:prog.lightGeometryPass");
        lightBufferPass = worldRenderer.getMaterial("engine:prog.lightBufferPass");
        initMainDirectionalLight();
    }

    // TODO: one day the main light (sun/moon) should be just another light in the scene.
    private void initMainDirectionalLight() {
        mainDirectionalLight.lightType = LightComponent.LightType.DIRECTIONAL;
        mainDirectionalLight.lightAmbientIntensity = 0.75f;
        mainDirectionalLight.lightDiffuseIntensity = 0.75f;
        mainDirectionalLight.lightSpecularPower = 100f;
    }

    @Override
    public void process() {
        sceneOpaque.bind();

        Vector3f sunlightWorldPosition = new Vector3f(backdropProvider.getSunDirection(true));
        sunlightWorldPosition.scale(50000f);
        sunlightWorldPosition.add(playerCamera.getPosition());
        // TODO: find a more elegant way
        // TODO: iterating over RenderSystems for rendering multiple lights
        worldRenderer.renderLightComponent(mainDirectionalLight, sunlightWorldPosition, lightGeometryShader, false);

        glDisable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_DEPTH_TEST);

        setRenderBufferMask(sceneOpaque, true, true, true);
        bindDisplay();

        applyLightBufferPass();
        PerformanceMonitor.endActivity(); // Render Light Geometry
    }

    /**
     * Part of the deferred lighting technique, this method applies lighting through screen-space
     * calculations to the previously flat-lit world rendering stored in the primary FBO.   // TODO: rename sceneOpaque* FBOs to primaryA/B
     * <p>
     * See http://en.wikipedia.org/wiki/Deferred_shading as a starting point.
     */
    private void applyLightBufferPass() {

        int texId = 0;

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        sceneOpaque.bindTexture();
        lightBufferPass.setInt("texSceneOpaque", texId++, true);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        sceneOpaque.bindDepthTexture();
        lightBufferPass.setInt("texSceneOpaqueDepth", texId++, true);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        sceneOpaque.bindNormalsTexture();
        lightBufferPass.setInt("texSceneOpaqueNormals", texId++, true);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        sceneOpaque.bindLightBufferTexture();
        lightBufferPass.setInt("texSceneOpaqueLightBuffer", texId, true);

        FBO sceneOpaquePingPong = frameBuffersManager.getFBO("sceneOpaquePingPong");
        FBO sceneReflectiveRefractive = frameBuffersManager.getFBO("sceneReflectiveRefractive");

        sceneOpaquePingPong.bind();
        setRenderBufferMask(sceneOpaquePingPong, true, true, true);

        setViewportToSizeOf(sceneOpaquePingPong);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

        renderFullscreenQuad();

        bindDisplay();     // TODO: verify this is necessary
        setViewportToSizeOf(sceneOpaque);    // TODO: verify this is necessary

        frameBuffersManager.swapSceneOpaqueFBOs();
        sceneOpaque.attachDepthBufferTo(sceneReflectiveRefractive);
    }
}
