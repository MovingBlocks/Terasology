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
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.DisableDepthTest;
import org.terasology.rendering.dag.stateChanges.EnableBlending;
import org.terasology.rendering.dag.stateChanges.EnableFaceCulling;
import org.terasology.rendering.dag.stateChanges.SetFacesToCull;
import org.terasology.rendering.logic.LightComponent;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_NOTEQUAL;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glStencilFunc;

/**
 * TODO: Diagram of this node
 */
public class LightGeometryNode extends AbstractNode {
    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    @In
    private WorldRenderer worldRenderer;

    @In
    private EntityManager entityManager;
    private Material lightGeometryShader;

    @Override
    public void initialise() {
        lightGeometryShader = worldRenderer.getMaterial("engine:prog.lightGeometryPass");
        addDesiredStateChange(new BindFBO(READ_ONLY_GBUFFER));
        addDesiredStateChange(new DisableDepthTest());
        addDesiredStateChange(new EnableBlending());
        addDesiredStateChange(new EnableFaceCulling());
        addDesiredStateChange(new SetFacesToCull(GL_FRONT));
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

        // TODO: I removed cleanup and verified that final image was not changed.
        // TODO: It would be better to have this verified by other eyes as well. -tdgunes
        // LightGeometry requires a cleanup

        preRenderSetupLightGeometry();

        for (EntityRef entity : entityManager.getEntitiesWith(LightComponent.class, LocationComponent.class)) {
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
            LightComponent lightComponent = entity.getComponent(LightComponent.class);

            final Vector3f worldPosition = locationComponent.getWorldPosition();
            // TODO: find a more elegant way
            worldRenderer.renderLightComponent(lightComponent, worldPosition, lightGeometryShader, false);
        }

        PerformanceMonitor.endActivity();
    }

    // TODO: figure how lighting works and what this does
    private void preRenderSetupLightGeometry() {
        // Only write to the light buffer
        READ_ONLY_GBUFFER.setRenderBufferMask(false, false, true);

        // TODO: define glStencilFunc and glBlendFunc as StateChange.
        glStencilFunc(GL_NOTEQUAL, 0, 0xFF);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_COLOR);
    }

}
