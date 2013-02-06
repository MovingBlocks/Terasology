/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.shader;

import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Vector4f;
import org.terasology.editor.properties.Property;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.PostProcessingRenderer;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4d;
import java.util.List;

/**
 * Shader parameters for the Light Shaft shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersLightShaft extends ShaderParametersBase {

    private Property density = new Property("density", 0.8f, 0.0f, 1.0f);
    private Property exposure = new Property("exposure", 0.35f, 0.0f, 1.0f);
    private Property weight = new Property("weight", 0.21f, 0.0f, 1.0f);
    private Property decay = new Property("decay", 0.79f, 0.0f, 1.0f);

    @Override
    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);

        PostProcessingRenderer.FBO scene = PostProcessingRenderer.getInstance().getFBO("scene");
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        scene.bindTexture();
        program.setInt("texScene", 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        scene.bindDepthTexture();
        program.setInt("texDepth", 0);

        program.setFloat("density", (Float) density.getValue());
        program.setFloat("exposure", (Float) exposure.getValue());
        program.setFloat("weight", (Float) weight.getValue());
        program.setFloat("decay", (Float) decay.getValue());

        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        if (worldRenderer != null) {
            float sunAngle = worldRenderer.getSkysphere().getSunPosAngle();
            Vector3f sunNormalise = new Vector3f(0.0f, (float) java.lang.Math.cos(sunAngle), (float) java.lang.Math.sin(sunAngle));
            sunNormalise.normalize();

            program.setFloat3("lightVector", sunNormalise.x, sunNormalise.y, sunNormalise.z);
        }

        Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();

        if (activeCamera != null) {
            program.setMatrix4("viewProjMatrix", activeCamera.getViewProjectionMatrix());
        }
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(decay);
        properties.add(density);
        properties.add(weight);
        properties.add(exposure);
    }
}
