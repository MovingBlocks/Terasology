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
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
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
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;

import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

// TODO: have this node and the shadowmap node handle multiple directional lights

/**
 * This class is integral to the deferred rendering process.
 * It renders the main light (sun/moon) as a directional light, a type of light emitting parallel rays as is
 * appropriate for astronomical light sources.
 *
 * This achieved by blending a single color into each pixel of the light accumulation buffer, the single
 * color being dependent only on the angle between the camera and the light direction.
 *
 * Eventually the content of the light accumulation buffer is combined with other buffers to correctly
 * light up the 3d scene.
 */
public class DeferredMainLightNode extends AbstractNode {
    private static final ResourceUrn LIGHT_GEOMETRY_MATERIAL = new ResourceUrn("engine:prog.lightGeometryPass");

    @In
    private BackdropProvider backdropProvider;

    @In
    private WorldRenderer worldRenderer;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private LightComponent mainLightComponent = new LightComponent();
    private Camera playerCamera;
    private Material lightGeometryMaterial;
    private FBO sceneOpaqueFbo;

    /**
     * Initializes an instance of this node.
     *
     * This method -must- be called once for this node to be fully operational.
     */
    @Override
    public void initialise() {
        sceneOpaqueFbo = displayResolutionDependentFBOs.get(READONLY_GBUFFER);

        playerCamera = worldRenderer.getActiveCamera();

        addDesiredStateChange(new EnableMaterial(LIGHT_GEOMETRY_MATERIAL.toString()));
        lightGeometryMaterial = getMaterial(LIGHT_GEOMETRY_MATERIAL);

        addDesiredStateChange(new DisableDepthTest());

        addDesiredStateChange(new EnableBlending());
        addDesiredStateChange(new SetBlendFunction(GL_ONE, GL_ONE_MINUS_SRC_COLOR));

        initMainDirectionalLight();
    }

    // TODO: one day the main light (sun/moon) should be just another light in the scene.
    private void initMainDirectionalLight() {
        mainLightComponent.lightType = LightComponent.LightType.DIRECTIONAL;
        mainLightComponent.lightAmbientIntensity = 0.75f;
        mainLightComponent.lightDiffuseIntensity = 0.75f;
        mainLightComponent.lightSpecularPower = 100f;
    }

    /**
     * Renders the main light (sun/moon) as a uniformly colored full-screen quad.
     * This gets blended into the existing data stored in the light accumulation buffer.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/mainLightGeometry");

        // Note: no need to set a camera here: the render takes place
        // with a default opengl camera and the quad is in front of it - I think.

        sceneOpaqueFbo.bind(); // TODO: remove and replace with a state change
        sceneOpaqueFbo.setRenderBufferMask(false, false, true); // Only write to the light accumulation buffer

        lightGeometryMaterial.activateFeature(ShaderProgramFeature.FEATURE_LIGHT_DIRECTIONAL);

        lightGeometryMaterial.setFloat3("lightColorDiffuse", mainLightComponent.lightColorDiffuse.x,
                mainLightComponent.lightColorDiffuse.y, mainLightComponent.lightColorDiffuse.z, true);
        lightGeometryMaterial.setFloat3("lightColorAmbient", mainLightComponent.lightColorAmbient.x,
                mainLightComponent.lightColorAmbient.y, mainLightComponent.lightColorAmbient.z, true);
        lightGeometryMaterial.setFloat3("lightProperties", mainLightComponent.lightAmbientIntensity,
                mainLightComponent.lightDiffuseIntensity, mainLightComponent.lightSpecularPower, true);

        Vector3f mainLightInViewSpace = new Vector3f(backdropProvider.getSunDirection(true));
        playerCamera.getViewMatrix().transformPoint(mainLightInViewSpace);
        lightGeometryMaterial.setFloat3("lightViewPos", mainLightInViewSpace.x, mainLightInViewSpace.y, mainLightInViewSpace.z, true);

        renderFullscreenQuad(); // renders the light.

        lightGeometryMaterial.deactivateFeature(ShaderProgramFeature.FEATURE_LIGHT_DIRECTIONAL);

        sceneOpaqueFbo.setRenderBufferMask(true, true, true);

        PerformanceMonitor.endActivity();
    }
}
