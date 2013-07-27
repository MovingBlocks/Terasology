/*
 * Copyright 2013 Moving Blocks
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
import org.terasology.engine.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.opengl.DefaultRenderingProcess;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 * Shader parameters for the Light Shaft shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersLightShaft extends ShaderParametersBase {

    private float density = 1.5f;
    private float exposure = 0.0075f;
    private float weight = 8.0f;
    private float decay = 0.9f;

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        DefaultRenderingProcess.FBO scene = DefaultRenderingProcess.getInstance().getFBO("sceneOpaque");

        int texId = 0;

        if (scene != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            scene.bindTexture();
            program.setInt("texScene", texId++, true);
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            scene.bindDepthTexture();
            program.setInt("texDepth", texId++, true);
        }

        program.setFloat("density", density, true);
        program.setFloat("exposure", exposure, true);
        program.setFloat("weight", weight, true);
        program.setFloat("decay", decay, true);

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

            program.setFloat("lightDirDotViewDir", activeCamera.getViewingDirection().dot(sunDirection), true);
            program.setFloat2("lightScreenPos", (sunPositionScreenSpace.x + 1.0f) / 2.0f, (sunPositionScreenSpace.y + 1.0f) / 2.0f, true);
        }
    }
}
