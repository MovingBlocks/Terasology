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
import org.terasology.rendering.renderingProcesses.DefaultRenderingProcess;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3f;
import java.util.List;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersPrePost extends ShaderParametersBase {

    Property aberrationOffsetX = new Property("aberrationOffsetX", 0.0f, 0.0f, 0.1f);
    Property aberrationOffsetY = new Property("aberrationOffsetY", 0.0f, 0.0f, 0.1f);

    Property bloomFactor = new Property("bloomFactor", 1.0f, 0.0f, 1.0f);

    @Override
    public void applyParameters(GLSLShaderProgramInstance program) {
        super.applyParameters(program);

        Vector3f tint = CoreRegistry.get(WorldRenderer.class).getTint();
        program.setFloat3("inLiquidTint", tint.x, tint.y, tint.z);

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("sceneOpaque");
        program.setInt("texScene", texId++);

        if (CoreRegistry.get(Config.class).getRendering().isBloom()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("sceneBloom1");
            program.setInt("texBloom", texId++);

            program.setFloat("bloomFactor", (Float) bloomFactor.getValue());
        }

        program.setFloat2("aberrationOffset", (Float) aberrationOffsetX.getValue(), (Float) aberrationOffsetY.getValue());

        if (CoreRegistry.get(Config.class).getRendering().isLightShafts()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("lightShafts");
            program.setInt("texLightShafts", texId++);
        }

        Texture vignetteTexture = Assets.getTexture("engine:vignette");

        if (vignetteTexture != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            glBindTexture(GL11.GL_TEXTURE_2D, vignetteTexture.getId());
            program.setInt("texVignette", texId++);
        }
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(aberrationOffsetX);
        properties.add(aberrationOffsetY);
        properties.add(bloomFactor);
    }
}
