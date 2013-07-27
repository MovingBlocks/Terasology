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
import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.opengl.DefaultRenderingProcess;
import org.terasology.rendering.world.WorldRenderer;

/**
 * Shader parameters for the Debug shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersDebug extends ShaderParametersBase {

    public void applyParameters(Material program) {
        super.applyParameters(program);

        Config config = CoreRegistry.get(Config.class);

        int texId = 0;

        switch (config.getRendering().getDebug().getStage()) {
            case SHADOW_MAP:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                DefaultRenderingProcess.getInstance().bindFboDepthTexture("sceneShadowMap");
                program.setInt("texDebug", texId++, true);
                break;
            case OPAQUE_COLOR:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                DefaultRenderingProcess.getInstance().bindFboTexture("sceneOpaque");
                program.setInt("texDebug", texId++, true);
                break;
            case OPAQUE_NORMALS:
            case OPAQUE_SUNLIGHT:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                DefaultRenderingProcess.getInstance().bindFboNormalsTexture("sceneOpaque");
                program.setInt("texDebug", texId++, true);
                break;
            case OPAQUE_DEPTH:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                DefaultRenderingProcess.getInstance().bindFboDepthTexture("sceneOpaque");
                program.setInt("texDebug", texId++, true);
                break;
            case OPAQUE_LIGHT_BUFFER:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                DefaultRenderingProcess.getInstance().bindFboLightBufferTexture("sceneOpaque");
                program.setInt("texDebug", texId++, true);
                break;
            case TRANSPARENT_COLOR:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                DefaultRenderingProcess.getInstance().bindFboTexture("sceneTransparent");
                program.setInt("texDebug", texId++, true);
                break;
            case SSAO:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                DefaultRenderingProcess.getInstance().bindFboTexture("ssaoBlurred");
                program.setInt("texDebug", texId++, true);
                break;
            case SOBEL:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                DefaultRenderingProcess.getInstance().bindFboTexture("sobel");
                program.setInt("texDebug", texId++, true);
                break;
            case BAKED_OCCLUSION:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                DefaultRenderingProcess.getInstance().bindFboTexture("sceneOpaque");
                program.setInt("texDebug", texId++, true);
                break;
            case RECONSTRUCTED_POSITION:
                Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
                if (activeCamera != null) {
                    program.setMatrix4("invProjMatrix", activeCamera.getInverseProjectionMatrix(), true);
                }

                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                DefaultRenderingProcess.getInstance().bindFboDepthTexture("sceneOpaque");
                program.setInt("texDebug", texId++, true);
                break;
            case BLOOM:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                DefaultRenderingProcess.getInstance().bindFboTexture("sceneBloom2");
                program.setInt("texDebug", texId++, true);
                break;
            case HIGH_PASS:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                DefaultRenderingProcess.getInstance().bindFboTexture("sceneHighPass");
                program.setInt("texDebug", texId++, true);
                break;
            case SKY_BAND:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                DefaultRenderingProcess.getInstance().bindFboTexture("sceneSkyBand1");
                program.setInt("texDebug", texId++, true);
                break;
            case LIGHT_SHAFTS:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                DefaultRenderingProcess.getInstance().bindFboTexture("lightShafts");
                program.setInt("texDebug", texId++, true);
                break;
        }

        program.setInt("debugRenderingStage", CoreRegistry.get(Config.class).getRendering().getDebug().getStage().getIndex());
    }
}
