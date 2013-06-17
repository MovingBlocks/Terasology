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
import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.DefaultRenderingProcess;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Debug shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersDebug extends ShaderParametersBase {

    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboDepthTexture("sceneShadowMap");
        program.setInt("texSceneShadowMap", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("sceneOpaque");
        program.setInt("texSceneOpaqueColor", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboNormalsTexture("sceneOpaque");
        program.setInt("texSceneOpaqueNormals", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboDepthTexture("sceneOpaque");
        program.setInt("texSceneOpaqueDepth", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("sceneTransparent");
        program.setInt("texSceneTransparentColor", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboNormalsTexture("sceneTransparent");
        program.setInt("texSceneTransparentNormals", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboDepthTexture("sceneTransparent");
        program.setInt("texSceneTransparentDepth", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("ssaoBlurred1");
        program.setInt("texSSAO", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("sobel");
        program.setInt("texSobel", texId++);

        program.setInt("debugRenderingStage", CoreRegistry.get(Config.class).getSystem().getDebugRenderingStage());
    }
}
