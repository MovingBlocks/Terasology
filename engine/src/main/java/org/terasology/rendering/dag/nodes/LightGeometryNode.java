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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.logic.LightComponent;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;
import org.terasology.rendering.world.WorldRenderer;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_NOTEQUAL;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glStencilFunc;
import static org.terasology.rendering.opengl.OpenGLUtils.bindDisplay;
import static org.terasology.rendering.opengl.OpenGLUtils.setRenderBufferMask;

/**
 * TODO: Diagram of this node
 */
public class LightGeometryNode extends AbstractNode {

    @In
    private FrameBuffersManager frameBuffersManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private EntityManager entityManager;

    private Material lightGeometryShader;
    private FBO sceneOpaque;

    @Override
    public void initialise() {
        lightGeometryShader = worldRenderer.getMaterial("engine:prog.lightGeometryPass");
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/lightGeometry");
        // DISABLED UNTIL WE CAN FIND WHY IT's BROKEN. SEE ISSUE #1486
        /*
        graphicState.preRenderSetupLightGeometryStencil();

        simple.enable();
        simple.setCamera(playerCamera);
        EntityManager entityManager = context.get(EntityManager.class);
        for (EntityRef entity : entityManager.getEntitiesWith(LightComponent.class, LocationComponent.class)) {
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
            LightComponent lightComponent = entity.getComponent(LightComponent.class);

            final Vector3f worldPosition = locationComponent.getWorldPosition();
            renderLightComponent(lightComponent, worldPosition, simple, true);
        }

        graphicState.postRenderCleanupLightGeometryStencil();
        */

        sceneOpaque = frameBuffersManager.getFBO("sceneOpaque");
        // LightGeometry requires a cleanup
        cleanupSceneOpaque();
        preRenderSetupLightGeometry();

        for (EntityRef entity : entityManager.getEntitiesWith(LightComponent.class, LocationComponent.class)) {
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
            LightComponent lightComponent = entity.getComponent(LightComponent.class);

            final Vector3f worldPosition = locationComponent.getWorldPosition();
            // TODO: find a more elegant way
            worldRenderer.renderLightComponent(lightComponent, worldPosition, lightGeometryShader, false);
        }
        postRenderCleanupLightGeometry();
        PerformanceMonitor.endActivity();

    }

    // TODO: figure how lighting works and what this does
    private void postRenderCleanupLightGeometry() {
        glDisable(GL_STENCIL_TEST);
        glCullFace(GL_BACK);

        bindDisplay();
    }

    // TODO: figure how lighting works and what this does
    private void preRenderSetupLightGeometry() {
        sceneOpaque.bind();

        // Only write to the light buffer
        setRenderBufferMask(sceneOpaque, false, false, true);

        glStencilFunc(GL_NOTEQUAL, 0, 0xFF);

        glDepthMask(true);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_COLOR);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);
    }

    /**
     * Resets the state after the rendering of the Opaque scene.
     */
    private void cleanupSceneOpaque() {
        sceneOpaque = frameBuffersManager.getFBO("sceneOpaque");
        setRenderBufferMask(sceneOpaque, true, true, true); // TODO: probably redundant - verify
        bindDisplay();
    }
}
