/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.AssetManager;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.PostProcessingRenderer;
import org.terasology.logic.world.WorldProvider;
import org.terasology.rendering.assets.Texture;
import org.terasology.model.blocks.Block;
import org.terasology.rendering.world.WorldRenderer;

import static org.lwjgl.opengl.GL11.glBindTexture;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersPost implements IShaderParameters {

    Texture texture = AssetManager.loadTexture("engine:vignette");

    public void applyParameters(ShaderProgram program) {
        PostProcessingRenderer.FBO scene = PostProcessingRenderer.getInstance().getFBO("scene");

        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        PostProcessingRenderer.getInstance().getFBO("sceneBloom2").bindTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        PostProcessingRenderer.getInstance().getFBO("sceneBlur2").bindTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        scene.bindDepthTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        PostProcessingRenderer.getInstance().getFBO("sceneTonemapped").bindTexture();

        program.setInt("texScene", 0);
        program.setInt("texBloom", 1);
        program.setInt("texBlur", 2);
        program.setInt("texVignette", 3);
        program.setInt("texDepth", 4);
        
        program.setFloat("viewingDistance", Config.getInstance().getActiveViewingDistance() * 8.0f);

        if (CoreRegistry.get(LocalPlayer.class).isValid()) {
            Vector3d cameraPos = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
            Block block = CoreRegistry.get(WorldProvider.class).getBlock(new Vector3f(cameraPos));
            program.setInt("swimming", block.isLiquid() ? 1 : 0);
        }
    }

}
