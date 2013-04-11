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
import org.terasology.editor.properties.Property;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.DefaultRenderingProcess;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.List;

/**
 * Shader parameters for the Light Shaft shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersLightShaft extends ShaderParametersBase {

    private Property density = new Property("density", 1.5f, 0.0f, 10.0f);
    private Property exposure = new Property("exposure", 0.0075f, 0.0f, 0.01f);
    private Property weight = new Property("weight", 8.0f, 0.0f, 10.0f);
    private Property decay = new Property("decay", 0.9f, 0.0f, 0.99f);

    @Override
    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);

        DefaultRenderingProcess.FBO scene = DefaultRenderingProcess.getInstance().getFBO("sceneCombined");
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        scene.bindTexture();
        program.setInt("texScene", 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        scene.bindNormalsTexture();
        program.setInt("texNormals", 0);

        program.setFloat("density", (Float) density.getValue());
        program.setFloat("exposure", (Float) exposure.getValue());
        program.setFloat("weight", (Float) weight.getValue());
        program.setFloat("decay", (Float) decay.getValue());

        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        if (worldRenderer != null) {
            Vector3f sunDirection = worldRenderer.getSkysphere().getSunDirection(true);

            Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();

            Vector4f sunPositionWorldSpace4 = new Vector4f(sunDirection.x * 10000.0f, sunDirection.y * 10000.0f, sunDirection.z * 10000.0f, 1.0f);
            Vector4f sunPositionScreenSpace = new Vector4f();
            activeCamera.getViewProjectionMatrix().transform(sunPositionWorldSpace4, sunPositionScreenSpace);

            sunPositionScreenSpace.x /= sunPositionScreenSpace.w;
            sunPositionScreenSpace.y /= sunPositionScreenSpace.w;
            sunPositionScreenSpace.z /= sunPositionScreenSpace.w;
            sunPositionScreenSpace.w = 1.0f;

            program.setFloat("lightDirDotViewDir", activeCamera.getViewingDirection().dot(sunDirection));
            program.setFloat2("lightScreenPos", (sunPositionScreenSpace.x + 1.0f) / 2.0f, (sunPositionScreenSpace.y + 1.0f) / 2.0f);
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
