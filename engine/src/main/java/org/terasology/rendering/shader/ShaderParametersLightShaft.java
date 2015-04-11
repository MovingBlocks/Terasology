/*
 * Copyright 2013 MovingBlocks
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
import org.terasology.editor.EditorRange;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.LwjglRenderingProcess;
import org.terasology.rendering.world.WorldRenderer;

import static org.terasology.rendering.assets.material.Material.StorageQualifier.UNIFORM;

/**
 * Shader parameters for the Light Shaft shader program.
 *
 * @author Benjamin Glatzel
 */
public class ShaderParametersLightShaft extends ShaderParametersBase {

    @EditorRange(min = 0.0f, max = 10.0f)
    private float density = 1.0f;
    @EditorRange(min = 0.0f, max = 0.01f)
    private float exposure = 0.0075f;
    @EditorRange(min = 0.0f, max = 10.0f)
    private float weight = 8.0f;
    @EditorRange(min = 0.0f, max = 0.99f)
    private float decay = 0.95f;

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        FBO scene = LwjglRenderingProcess.getInstance().getFBO("sceneOpaque");

        int texId = 0;

        if (scene != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            scene.bindTexture();
            program.setInt(UNIFORM, "texScene", texId++, true);
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            scene.bindDepthTexture();
            program.setInt(UNIFORM, "texDepth", texId++, true);
        }

        program.setFloat(UNIFORM, "density", density, true);
        program.setFloat(UNIFORM, "exposure", exposure, true);
        program.setFloat(UNIFORM, "weight", weight, true);
        program.setFloat(UNIFORM, "decay", decay, true);

        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        BackdropProvider backdropProvider = CoreRegistry.get(BackdropProvider.class);

        if (worldRenderer != null) {
            Vector3f sunDirection = backdropProvider.getSunDirection(true);

            Camera activeCamera = worldRenderer.getActiveCamera();

            Vector4f sunPositionWorldSpace4 = new Vector4f(sunDirection.x * 10000.0f, sunDirection.y * 10000.0f, sunDirection.z * 10000.0f, 1.0f);
            Vector4f sunPositionScreenSpace = new Vector4f(sunPositionWorldSpace4);
            activeCamera.getViewProjectionMatrix().transform(sunPositionScreenSpace);

            sunPositionScreenSpace.x /= sunPositionScreenSpace.w;
            sunPositionScreenSpace.y /= sunPositionScreenSpace.w;
            sunPositionScreenSpace.z /= sunPositionScreenSpace.w;
            sunPositionScreenSpace.w = 1.0f;

            program.setFloat(UNIFORM, "lightDirDotViewDir", activeCamera.getViewingDirection().dot(sunDirection), true);
            program.setFloat2(UNIFORM, "lightScreenPos", (sunPositionScreenSpace.x + 1.0f) / 2.0f, (sunPositionScreenSpace.y + 1.0f) / 2.0f, true);
        }
    }
}
