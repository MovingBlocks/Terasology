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
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.DisableDepthTest;
import org.terasology.rendering.dag.stateChanges.EnableBlending;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetBlendFunction;
import org.terasology.rendering.logic.LightComponent;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import org.terasology.rendering.world.WorldRenderer;

/**
 * TODO: Break this node into several nodes
 * TODO: For doing that worldRenderer.renderLightComponent must be eliminated somehow
 */
public class DeferredMainLightNode extends AbstractNode {
    private static final ResourceUrn LIGHT_GEOMETRY_MATERIAL = new ResourceUrn("engine:prog.lightGeometryPass");

    @In
    private BackdropProvider backdropProvider;

    @In
    private WorldRenderer worldRenderer;

    // TODO: Review this? (What are we doing with a component not attached to an entity?)
    private LightComponent mainDirectionalLight = new LightComponent();

    private Camera playerCamera;

    private Material lightGeometryShader;

    @Override
    public void initialise() {
        playerCamera = worldRenderer.getActiveCamera();

        addDesiredStateChange(new EnableMaterial(LIGHT_GEOMETRY_MATERIAL.toString()));
        lightGeometryShader = getMaterial(LIGHT_GEOMETRY_MATERIAL);

        addDesiredStateChange(new DisableDepthTest());

        addDesiredStateChange(new EnableBlending());
        addDesiredStateChange(new SetBlendFunction(GL_ONE, GL_ONE_MINUS_SRC_COLOR));

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
        PerformanceMonitor.startActivity("rendering/mainlight");

        playerCamera.lookThrough(); // TODO: remove and replace with a state change

        READ_ONLY_GBUFFER.bind(); // TODO: remove and replace with a state change
        READ_ONLY_GBUFFER.setRenderBufferMask(false, false, true); // Only write to the light accumulation buffer

        Vector3f sunlightWorldPosition = new Vector3f(backdropProvider.getSunDirection(true));
        sunlightWorldPosition.scale(50000f);
        sunlightWorldPosition.add(playerCamera.getPosition());
        // TODO: find a more elegant way
        // TODO: iterating over RenderSystems for rendering multiple lights
        worldRenderer.renderLightComponent(mainDirectionalLight, sunlightWorldPosition, lightGeometryShader, false);

        READ_ONLY_GBUFFER.setRenderBufferMask(true, true, true);

        PerformanceMonitor.endActivity();
    }
}
