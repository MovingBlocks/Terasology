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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.DisableDepthTest;
import org.terasology.rendering.dag.stateChanges.EnableBlending;
import org.terasology.rendering.dag.stateChanges.EnableFaceCulling;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetBlendFunction;
import org.terasology.rendering.dag.stateChanges.SetFacesToCull;
import org.terasology.rendering.logic.LightComponent;

import static org.lwjgl.opengl.GL11.*;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

/**
 * TODO: Add desired state changes
 */
public class LightGeometryNode extends AbstractNode {

    private static final ResourceUrn LIGHT_GEOMETRY_MATERIAL = new ResourceUrn("engine:prog.lightGeometryPass");

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    @In
    private WorldRenderer worldRenderer;

    @In
    private EntityManager entityManager;

    private Material lightGeometryShader;

    @Override
    public void initialise() {

        lightGeometryShader = getMaterial(LIGHT_GEOMETRY_MATERIAL);
        addDesiredStateChange(new EnableMaterial(LIGHT_GEOMETRY_MATERIAL.toString()));

        addDesiredStateChange(new EnableFaceCulling());
        addDesiredStateChange(new SetFacesToCull(GL_FRONT));

        addDesiredStateChange(new EnableBlending());
        addDesiredStateChange(new SetBlendFunction(GL_ONE, GL_ONE_MINUS_SRC_COLOR));

        addDesiredStateChange(new DisableDepthTest());


    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/lightGeometry");

        READ_ONLY_GBUFFER.bind();
        READ_ONLY_GBUFFER.setRenderBufferMask(false, false, true); // Only write to the light buffer

        for (EntityRef entity : entityManager.getEntitiesWith(LightComponent.class, LocationComponent.class)) {
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
            LightComponent lightComponent = entity.getComponent(LightComponent.class);

            final Vector3f worldPosition = locationComponent.getWorldPosition();
            // TODO: find a more elegant way
            worldRenderer.renderLightComponent(lightComponent, worldPosition, lightGeometryShader, false);
        }

        READ_ONLY_GBUFFER.setRenderBufferMask(true, true, true); // TODO: eventually remove - used for safety for the time being

        PerformanceMonitor.endActivity();
    }

}
