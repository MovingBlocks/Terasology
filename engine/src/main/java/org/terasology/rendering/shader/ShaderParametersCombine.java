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
import org.terasology.config.Config;
import org.terasology.editor.EditorRange;
import org.terasology.math.geom.Vector4f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.LwjglRenderingProcess;
import org.terasology.rendering.world.WorldRenderer;

import static org.terasology.rendering.assets.material.Material.StorageQualifier.UNIFORM;

/**
 * Shader parameters for the Combine shader program.
 *
 * @author Benjamin Glatzel
 */
public class ShaderParametersCombine extends ShaderParametersBase {
    @EditorRange(min = 0.001f, max = 0.005f)
    private float outlineDepthThreshold = 0.001f;
    @EditorRange(min = 0.0f, max = 1.0f)
    private float outlineThickness = 0.65f;

    @EditorRange(min = 0.0f, max = 1.0f)
    private float skyInscatteringLength = 1.0f;
    @EditorRange(min = 0.0f, max = 1.0f)
    private float skyInscatteringStrength = 0.25f;
    @EditorRange(min = 0.0f, max = 1.0f)
    private float skyInscatteringThreshold = 0.8f;

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        int texId = 0;

        FBO sceneOpaque = LwjglRenderingProcess.getInstance().getFBO("sceneOpaque");

        if (sceneOpaque != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindTexture();
            program.setInt(UNIFORM, "texSceneOpaque", texId++, true);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindDepthTexture();
            program.setInt(UNIFORM, "texSceneOpaqueDepth", texId++, true);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindNormalsTexture();
            program.setInt(UNIFORM, "texSceneOpaqueNormals", texId++, true);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindLightBufferTexture();
            program.setInt(UNIFORM, "texSceneOpaqueLightBuffer", texId++, true);
        }

        FBO sceneReflectiveRefractive = LwjglRenderingProcess.getInstance().getFBO("sceneReflectiveRefractive");

        if (sceneReflectiveRefractive != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneReflectiveRefractive.bindTexture();
            program.setInt(UNIFORM, "texSceneReflectiveRefractive", texId++, true);
        }

        if (CoreRegistry.get(Config.class).getRendering().isLocalReflections()) {
            if (sceneReflectiveRefractive != null) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                sceneReflectiveRefractive.bindNormalsTexture();
                program.setInt(UNIFORM, "texSceneReflectiveRefractiveNormals", texId++, true);
            }

            Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
            if (activeCamera != null) {
                program.setMatrix4(UNIFORM, "invProjMatrix", activeCamera.getInverseProjectionMatrix(), true);
                program.setMatrix4(UNIFORM, "projMatrix", activeCamera.getProjectionMatrix(), true);
            }
        }

        if (CoreRegistry.get(Config.class).getRendering().isSsao()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            LwjglRenderingProcess.getInstance().bindFboTexture("ssaoBlurred");
            program.setInt(UNIFORM, "texSsao", texId++, true);
        }

        if (CoreRegistry.get(Config.class).getRendering().isOutline()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            LwjglRenderingProcess.getInstance().bindFboTexture("sobel");
            program.setInt(UNIFORM, "texEdges", texId++, true);

            program.setFloat(UNIFORM, "outlineDepthThreshold", outlineDepthThreshold, true);
            program.setFloat(UNIFORM, "outlineThickness", outlineThickness, true);
        }

        if (CoreRegistry.get(Config.class).getRendering().isInscattering()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            LwjglRenderingProcess.getInstance().bindFboTexture("sceneSkyBand1");
            program.setInt(UNIFORM, "texSceneSkyBand", texId++, true);

            Vector4f skyInscatteringSettingsFrag = new Vector4f();
            skyInscatteringSettingsFrag.y = skyInscatteringStrength;
            skyInscatteringSettingsFrag.z = skyInscatteringLength;
            skyInscatteringSettingsFrag.w = skyInscatteringThreshold;
            program.setFloat4(UNIFORM, "skyInscatteringSettingsFrag", skyInscatteringSettingsFrag, true);
        }
    }
}
