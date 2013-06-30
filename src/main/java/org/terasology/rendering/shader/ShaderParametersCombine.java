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
import org.terasology.config.Config;
import org.terasology.editor.properties.Property;
import org.terasology.game.CoreRegistry;
import org.terasology.rendering.assets.GLSLShaderProgramInstance;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.renderingProcesses.DefaultRenderingProcess;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.List;

/**
 * Shader parameters for the Combine shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersCombine extends ShaderParametersBase {

    private Property outlineDepthThreshold = new Property("outlineDepthThreshold", 0.001f, 0.001f, 0.005f);
    private Property outlineThickness = new Property("outlineThickness", 0.65f);

    Property skyInscatteringLength = new Property("skyInscatteringLength", 0.25f, 0.0f, 1.0f);
    Property skyInscatteringStrength = new Property("skyInscatteringStrength", 0.35f, 0.0f, 1.0f);
    Property skyInscatteringThreshold = new Property("skyInscatteringThreshold", 0.75f, 0.0f, 1.0f);

    Property shadowIntens = new Property("shadowIntens", 0.65f, 0.0f, 1.0f);
    Property shadowMapBias = new Property("shadowMapBias", 0.003f, 0.0f, 0.1f);

    Property volFogDensityAtViewer = new Property("volFogDensityAtViewer", 0.15f, 0.001f, 1.0f);
    Property volFogGlobalDensity = new Property("volFogGlobalDensity", 0.05f, 0.01f, 1.0f);
    Property volFogHeightFalloff = new Property("volFogHeightFalloff", 0.1f, 0.01f, 1.0f);

    @Override
    public void applyParameters(GLSLShaderProgramInstance program) {
        super.applyParameters(program);

        int texId = 0;

        DefaultRenderingProcess.FBO sceneOpaque = DefaultRenderingProcess.getInstance().getFBO("sceneOpaque");

        if (sceneOpaque != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindTexture();
            program.setInt("texSceneOpaque", texId++);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindDepthTexture();
            program.setInt("texSceneOpaqueDepth", texId++);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindNormalsTexture();
            program.setInt("texSceneOpaqueNormals", texId++);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindLightBufferTexture();
            program.setInt("texSceneOpaqueLightBuffer", texId++);
        }

        if (CoreRegistry.get(Config.class).getRendering().isDynamicShadows()
                || CoreRegistry.get(Config.class).getRendering().isVolumetricFog()) {

            Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
            if (activeCamera != null) {
                program.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix());
            }
        }

        if (CoreRegistry.get(Config.class).getRendering().isVolumetricFog()) {
            program.setFloat4("volumetricFogSettings", (Float) volFogDensityAtViewer.getValue(),
                    (Float) volFogGlobalDensity.getValue(), (Float) volFogHeightFalloff.getValue(), 0.0f);

            Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
            if (activeCamera != null) {
                Vector3f fogWorldPosition = new Vector3f(activeCamera.getPosition().x, 32.0f, activeCamera.getPosition().y);
                fogWorldPosition.sub(activeCamera.getPosition());

                program.setFloat3("fogWorldPosition", fogWorldPosition.x, fogWorldPosition.y, fogWorldPosition.z);
            }
        }

        if (CoreRegistry.get(Config.class).getRendering().isDynamicShadows()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboDepthTexture("sceneShadowMap");
            program.setInt("texSceneShadowMap", texId++);

            Camera lightCamera = CoreRegistry.get(WorldRenderer.class).getLightCamera();
            Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();

            if (lightCamera != null && activeCamera != null) {
                program.setMatrix4("lightViewProjMatrix", lightCamera.getViewProjectionMatrix());

                Vector3f activeCameraToLightSpace = new Vector3f();
                activeCameraToLightSpace.sub(activeCamera.getPosition(), lightCamera.getPosition());
                program.setFloat3("activeCameraToLightSpace", activeCameraToLightSpace.x, activeCameraToLightSpace.y, activeCameraToLightSpace.z);
            }

            Vector4f shadowSettingsFrag = new Vector4f();
            shadowSettingsFrag.x = (Float) shadowIntens.getValue();
            shadowSettingsFrag.y = (Float) shadowMapBias.getValue();

            program.setFloat4("shadowSettingsFrag", shadowSettingsFrag);
        }

        DefaultRenderingProcess.FBO sceneTransparent = DefaultRenderingProcess.getInstance().getFBO("sceneTransparent");

        if (sceneTransparent != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneTransparent.bindTexture();
            program.setInt("texSceneTransparent", texId++);
        }

        if (CoreRegistry.get(Config.class).getRendering().isSsao()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("ssaoBlurred1");
            program.setInt("texSsao", texId++);
        }

        if (CoreRegistry.get(Config.class).getRendering().isOutline()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("sobel");
            program.setInt("texEdges", texId++);

            program.setFloat("outlineDepthThreshold", (Float) outlineDepthThreshold.getValue());
            program.setFloat("outlineThickness", (Float) outlineThickness.getValue());
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("sceneSkyBand1");
        program.setInt("texSceneSkyBand", texId++);

        Vector4f skyInscatteringSettingsFrag = new Vector4f();
        skyInscatteringSettingsFrag.y = (Float) skyInscatteringStrength.getValue();
        skyInscatteringSettingsFrag.z = (Float) skyInscatteringLength.getValue();
        skyInscatteringSettingsFrag.w = (Float) skyInscatteringThreshold.getValue();
        program.setFloat4("skyInscatteringSettingsFrag", skyInscatteringSettingsFrag);
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(skyInscatteringLength);
        properties.add(skyInscatteringStrength);
        properties.add(skyInscatteringThreshold);
        properties.add(outlineThickness);
        properties.add(outlineDepthThreshold);
        properties.add(shadowIntens);
        properties.add(shadowMapBias);
        properties.add(volFogDensityAtViewer);
        properties.add(volFogGlobalDensity);
        properties.add(volFogHeightFalloff);
    }
}
