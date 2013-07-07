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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.editor.properties.Property;
import org.terasology.game.CoreRegistry;
import org.terasology.rendering.assets.GLSLShaderProgramInstance;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.renderingProcesses.DefaultRenderingProcess;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3f;
import java.util.List;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the LightBufferPass shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersLightGeometryPass extends ShaderParametersBase {

    @Override
    public void applyParameters(GLSLShaderProgramInstance program) {
        super.applyParameters(program);

        DefaultRenderingProcess.FBO sceneOpaque = DefaultRenderingProcess.getInstance().getFBO("sceneOpaque");

        int texId = 0;
        if (sceneOpaque != null) {
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

        if (CoreRegistry.get(Config.class).getRendering().isDynamicShadows()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboDepthTexture("sceneShadowMap");
            program.setInt("texSceneShadowMap", texId++);

            Camera lightCamera = CoreRegistry.get(WorldRenderer.class).getLightCamera();
            Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();

            if (lightCamera != null && activeCamera != null) {
                program.setMatrix4("lightViewProjMatrix", lightCamera.getViewProjectionMatrix());
                program.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix());

                Vector3f activeCameraToLightSpace = new Vector3f();
                activeCameraToLightSpace.sub(activeCamera.getPosition(), lightCamera.getPosition());
                program.setFloat3("activeCameraToLightSpace", activeCameraToLightSpace.x, activeCameraToLightSpace.y, activeCameraToLightSpace.z);
            }

            if (CoreRegistry.get(Config.class).getRendering().isCloudShadows()) {
                Texture clouds = Assets.getTexture("engine:perlinNoiseTileable");

                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                glBindTexture(GL11.GL_TEXTURE_2D, clouds.getId());
                program.setInt("texSceneClouds", texId++);
            }
        }
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
    }
}
