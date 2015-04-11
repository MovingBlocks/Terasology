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
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.opengl.LwjglRenderingProcess;
import org.terasology.rendering.world.WorldRenderer;

import static org.terasology.rendering.assets.material.Material.StorageQualifier.UNIFORM;

/**
 * Shader parameters for the Debug shader program.
 *
 * @author Benjamin Glatzel
 */
public class ShaderParametersDebug extends ShaderParametersBase {

    public void applyParameters(Material program) {
        super.applyParameters(program);

        Config config = CoreRegistry.get(Config.class);

        int texId = 0;

        switch (config.getRendering().getDebug().getStage()) {
            case SHADOW_MAP:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                LwjglRenderingProcess.getInstance().bindFboDepthTexture("sceneShadowMap");
                program.setInt(UNIFORM, "texDebug", texId++, true);
                break;
            case OPAQUE_COLOR:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                LwjglRenderingProcess.getInstance().bindFboTexture("sceneOpaque");
                program.setInt(UNIFORM, "texDebug", texId++, true);
                break;
            case OPAQUE_NORMALS:
            case OPAQUE_SUNLIGHT:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                LwjglRenderingProcess.getInstance().bindFboNormalsTexture("sceneOpaque");
                program.setInt(UNIFORM, "texDebug", texId++, true);
                break;
            case OPAQUE_DEPTH:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                LwjglRenderingProcess.getInstance().bindFboDepthTexture("sceneOpaque");
                program.setInt(UNIFORM, "texDebug", texId++, true);
                break;
            case OPAQUE_LIGHT_BUFFER:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                LwjglRenderingProcess.getInstance().bindFboLightBufferTexture("sceneOpaque");
                program.setInt(UNIFORM, "texDebug", texId++, true);
                break;
            case TRANSPARENT_COLOR:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                LwjglRenderingProcess.getInstance().bindFboTexture("sceneReflectiveRefractive");
                program.setInt(UNIFORM, "texDebug", texId++, true);
                break;
            case SSAO:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                LwjglRenderingProcess.getInstance().bindFboTexture("ssaoBlurred");
                program.setInt(UNIFORM, "texDebug", texId++, true);
                break;
            case SOBEL:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                LwjglRenderingProcess.getInstance().bindFboTexture("sobel");
                program.setInt(UNIFORM, "texDebug", texId++, true);
                break;
            case BAKED_OCCLUSION:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                LwjglRenderingProcess.getInstance().bindFboTexture("sceneOpaque");
                program.setInt(UNIFORM, "texDebug", texId++, true);
                break;
            case RECONSTRUCTED_POSITION:
                Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
                if (activeCamera != null) {
                    program.setMatrix4(UNIFORM, "invProjMatrix", activeCamera.getInverseProjectionMatrix(), true);
                }

                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                LwjglRenderingProcess.getInstance().bindFboDepthTexture("sceneOpaque");
                program.setInt(UNIFORM, "texDebug", texId++, true);
                break;
            case BLOOM:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                LwjglRenderingProcess.getInstance().bindFboTexture("sceneBloom2");
                program.setInt(UNIFORM, "texDebug", texId++, true);
                break;
            case HIGH_PASS:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                LwjglRenderingProcess.getInstance().bindFboTexture("sceneHighPass");
                program.setInt(UNIFORM, "texDebug", texId++, true);
                break;
            case SKY_BAND:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                LwjglRenderingProcess.getInstance().bindFboTexture("sceneSkyBand1");
                program.setInt(UNIFORM, "texDebug", texId++, true);
                break;
            case LIGHT_SHAFTS:
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                LwjglRenderingProcess.getInstance().bindFboTexture("lightShafts");
                program.setInt(UNIFORM, "texDebug", texId++, true);
                break;
            default:
                break;
        }

        program.setInt(UNIFORM, "debugRenderingStage", CoreRegistry.get(Config.class).getRendering().getDebug().getStage().getIndex());
    }
}
