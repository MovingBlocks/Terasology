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
import org.terasology.config.SystemConfig;
import org.terasology.game.CoreRegistry;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.renderingProcesses.DefaultRenderingProcess;
import org.terasology.rendering.world.WorldRenderer;

/**
 * Shader parameters for the Debug shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersDebug extends ShaderParametersBase {

    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);

        Config config = CoreRegistry.get(Config.class);

        int texId = 0;

        if (config.getSystem().getDebugRenderingStage() == SystemConfig.DebugRenderingStages.DEBUG_STAGE_SHADOW_MAP.ordinal()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboDepthTexture("sceneShadowMap");
            program.setInt("texDebug", texId++);
        } else if (config.getSystem().getDebugRenderingStage() == SystemConfig.DebugRenderingStages.DEBUG_STAGE_OPAQUE_COLOR.ordinal()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("sceneOpaque");
            program.setInt("texDebug", texId++);
        } else if (config.getSystem().getDebugRenderingStage() == SystemConfig.DebugRenderingStages.DEBUG_STAGE_OPAQUE_NORMALS.ordinal()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboNormalsTexture("sceneOpaque");
            program.setInt("texDebug", texId++);
        } else if (config.getSystem().getDebugRenderingStage() == SystemConfig.DebugRenderingStages.DEBUG_STAGE_OPAQUE_DEPTH.ordinal()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboDepthTexture("sceneOpaque");
            program.setInt("texDebug", texId++);
        } else if (config.getSystem().getDebugRenderingStage() == SystemConfig.DebugRenderingStages.DEBUG_STAGE_OPAQUE_LIGHT_BUFFER.ordinal()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboLightBufferTexture("sceneOpaque");
            program.setInt("texDebug", texId++);
        } else if (config.getSystem().getDebugRenderingStage() == SystemConfig.DebugRenderingStages.DEBUG_STAGE_TRANSPARENT_COLOR.ordinal()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("sceneTransparent");
            program.setInt("texDebug", texId++);
        } else if (config.getSystem().getDebugRenderingStage() == SystemConfig.DebugRenderingStages.DEBUG_STAGE_SSAO.ordinal()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("ssaoBlurred1");
            program.setInt("texDebug", texId++);
        } else if (config.getSystem().getDebugRenderingStage() == SystemConfig.DebugRenderingStages.DEBUG_STAGE_SOBEL.ordinal()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("sobel");
            program.setInt("texDebug", texId++);
        }  else if (config.getSystem().getDebugRenderingStage() == SystemConfig.DebugRenderingStages.DEBUG_STAGE_RECONSTRUCTED_POSITION.ordinal()) {
            Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
            if (activeCamera != null) {
                program.setMatrix4("invProjMatrix", activeCamera.getInverseProjectionMatrix());
            }

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboDepthTexture("sceneOpaque");
            program.setInt("texDebug", texId++);
        } else if (config.getSystem().getDebugRenderingStage() == SystemConfig.DebugRenderingStages.DEBUG_STAGE_BLOOM.ordinal()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("sceneBloom1");
            program.setInt("texDebug", texId++);
        } else if (config.getSystem().getDebugRenderingStage() == SystemConfig.DebugRenderingStages.DEBUG_STAGE_HIGH_PASS.ordinal()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("sceneHighPass");
            program.setInt("texDebug", texId++);
        } else if (config.getSystem().getDebugRenderingStage() == SystemConfig.DebugRenderingStages.DEBUG_STAGE_SKY_BAND.ordinal()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("sceneSkyBand1");
            program.setInt("texDebug", texId++);
        }

        program.setInt("debugRenderingStage", CoreRegistry.get(Config.class).getSystem().getDebugRenderingStage());
    }
}
