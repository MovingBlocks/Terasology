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

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.cameras.Camera;
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
    private static int lightSphereDisplayList = -1;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    @In
    private WorldRenderer worldRenderer;

    @In
    private EntityManager entityManager;

    private Material lightGeometryMaterial;
    private Camera playerCamera;

    @Override
    public void initialise() {
        playerCamera = worldRenderer.getActiveCamera();

        lightGeometryMaterial = getMaterial(LIGHT_GEOMETRY_MATERIAL);
        addDesiredStateChange(new EnableMaterial(LIGHT_GEOMETRY_MATERIAL.toString()));

        addDesiredStateChange(new EnableFaceCulling());
        addDesiredStateChange(new SetFacesToCull(GL_FRONT));

        addDesiredStateChange(new EnableBlending());
        addDesiredStateChange(new SetBlendFunction(GL_ONE, GL_ONE_MINUS_SRC_COLOR));

        addDesiredStateChange(new DisableDepthTest());

        initLightSphereDisplayList();
    }

    private void initLightSphereDisplayList() {
        lightSphereDisplayList = glGenLists(1);
        Sphere sphere = new Sphere();

        glNewList(lightSphereDisplayList, GL11.GL_COMPILE);
            sphere.draw(1, 8, 8);
        glEndList();
    }

    private boolean lightIsRenderable(LightComponent lightComponent, Vector3f lightPositionRelativeToCamera) {

        // if lightRenderingDistance is 0.0, the light is always considered, no matter the distance.
        boolean lightIsRenderable = lightComponent.lightRenderingDistance == 0.0f
                || lightPositionRelativeToCamera.lengthSquared() < (lightComponent.lightRenderingDistance * lightComponent.lightRenderingDistance);
        // above: rendering distance must be higher than distance from the camera or the light is ignored

        // No matter what, we ignore lights that are not in the camera frustrum
        lightIsRenderable &= playerCamera.getViewFrustum().intersects(lightPositionRelativeToCamera, lightComponent.lightAttenuationRange);
        // TODO: (above) what about lights just off-frame? They might light up in-frame surfaces.

        return lightIsRenderable;
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/lightGeometry");

        playerCamera.lookThrough(); // TODO: remove and replace with a state change

        READ_ONLY_GBUFFER.bind(); // TODO: remove and replace with a state change
        READ_ONLY_GBUFFER.setRenderBufferMask(false, false, true); // Only write to the light buffer

        for (EntityRef entity : entityManager.getEntitiesWith(LightComponent.class, LocationComponent.class)) {
            LightComponent lightComponent = entity.getComponent(LightComponent.class);

            if (lightComponent.lightType == LightComponent.LightType.POINT) {
                LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
                final Vector3f lightPositionInTeraCoords = locationComponent.getWorldPosition();

                Vector3f lightPositionRelativeToCamera = new Vector3f();
                lightPositionRelativeToCamera.sub(lightPositionInTeraCoords, playerCamera.getPosition());

                if (lightIsRenderable(lightComponent, lightPositionRelativeToCamera)) {
                    lightGeometryMaterial.activateFeature(ShaderProgramFeature.FEATURE_LIGHT_POINT);

                    lightGeometryMaterial.setCamera(playerCamera);

                    lightGeometryMaterial.setFloat3("lightColorDiffuse",
                            lightComponent.lightColorDiffuse.x, lightComponent.lightColorDiffuse.y, lightComponent.lightColorDiffuse.z, true);
                    lightGeometryMaterial.setFloat3("lightColorAmbient",
                            lightComponent.lightColorAmbient.x, lightComponent.lightColorAmbient.y, lightComponent.lightColorAmbient.z, true);
                    lightGeometryMaterial.setFloat3("lightProperties",
                            lightComponent.lightAmbientIntensity, lightComponent.lightDiffuseIntensity, lightComponent.lightSpecularPower, true);
                    lightGeometryMaterial.setFloat4("lightExtendedProperties",
                            lightComponent.lightAttenuationRange, lightComponent.lightAttenuationFalloff, 0.0f, 0.0f, true);

                    Vector3f lightPositionInViewSpace = new Vector3f(lightPositionRelativeToCamera);
                    playerCamera.getViewMatrix().transformPoint(lightPositionInViewSpace);
                    lightGeometryMaterial.setFloat3("lightViewPos", lightPositionInViewSpace.x, lightPositionInViewSpace.y, lightPositionInViewSpace.z, true);

                    Matrix4f modelMatrix = new Matrix4f();
                    modelMatrix.set(lightComponent.lightAttenuationRange); // scales the modelview matrix, effectively scales the light sphere
                    modelMatrix.setTranslation(lightPositionRelativeToCamera); // effectively moves the light sphere in the right position relative to camera
                    lightGeometryMaterial.setMatrix4("modelMatrix", modelMatrix, true);

                    glCallList(lightSphereDisplayList); // draws the light sphere

                    lightGeometryMaterial.deactivateFeature(ShaderProgramFeature.FEATURE_LIGHT_POINT);
                }
            }
        }

        READ_ONLY_GBUFFER.setRenderBufferMask(true, true, true); // TODO: eventually remove - used for safety for the time being

        PerformanceMonitor.endActivity();
    }

}
