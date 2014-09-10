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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.editor.EditorRange;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.opengl.DefaultRenderingProcess;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3f;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersPrePost extends ShaderParametersBase {

    @EditorRange(min = 0.0f, max = 0.1f)
    float aberrationOffsetX;
    @EditorRange(min = 0.0f, max = 0.1f)
    float aberrationOffsetY;

    @EditorRange(min = 0.0f, max = 1.0f)
    float bloomFactor = 0.5f;

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        Vector3f tint = CoreRegistry.get(WorldRenderer.class).getTint();
        program.setFloat3("inLiquidTint", tint.x, tint.y, tint.z, true);

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("sceneOpaque");
        program.setInt("texScene", texId++, true);

        if (CoreRegistry.get(Config.class).getRendering().isBloom()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("sceneBloom2");
            program.setInt("texBloom", texId++, true);

            program.setFloat("bloomFactor", bloomFactor, true);
        }

        program.setFloat2("aberrationOffset", aberrationOffsetX, aberrationOffsetY, true);

        if (CoreRegistry.get(Config.class).getRendering().isLightShafts()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("lightShafts");
            program.setInt("texLightShafts", texId++, true);
        }

        Texture vignetteTexture = Assets.getTexture("engine:vignette");

        if (vignetteTexture != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            glBindTexture(GL11.GL_TEXTURE_2D, vignetteTexture.getId());
            program.setInt("texVignette", texId++, true);
        }
    }
}
