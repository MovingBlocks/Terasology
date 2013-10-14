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
import org.terasology.engine.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.opengl.DefaultRenderingProcess;

import javax.vecmath.Vector4f;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Chunk shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersChunk extends ShaderParametersBase {
    @EditorRange(min = 0.0f, max = 2.0f)
    public float waveIntens = 2.0f;
    @EditorRange(min = 0.0f, max = 2.0f)
    public float waveIntensFalloff = 0.85f;
    @EditorRange(min = 0.0f, max = 2.0f)
    public float waveSize = 0.1f;
    @EditorRange(min = 0.0f, max = 2.0f)
    public float waveSizeFalloff = 1.25f;
    @EditorRange(min = 0.0f, max = 2.0f)
    public float waveSpeed = 0.1f;
    @EditorRange(min = 0.0f, max = 2.0f)
    public float waveSpeedFalloff = 0.95f;
    @EditorRange(min = 0.0f, max = 5.0f)
    public float waterOffsetY;

    @EditorRange(min = 0.0f, max = 2.0f)
    public float waveOverallScale = 1.0f;

    @EditorRange(min = 0.0f, max = 1.0f)
    float waterRefraction = 0.04f;
    @EditorRange(min = 0.0f, max = 0.1f)
    float waterFresnelBias = 0.01f;
    @EditorRange(min = 0.0f, max = 10.0f)
    float waterFresnelPow = 2.5f;
    @EditorRange(min = 1.0f, max = 100.0f)
    float waterNormalBias = 10.0f;
    @EditorRange(min = 0.0f, max = 1.0f)
    float waterTint = 0.24f;

    @EditorRange(min = 0.0f, max = 1024.0f)
    float waterSpecExp = 200.0f;

    @EditorRange(min = 0.0f, max = 0.5f)
    float parallaxBias = 0.05f;
    @EditorRange(min = 0.0f, max = 0.50f)
    float parallaxScale = 0.05f;

    public void applyParameters(Material program) {
        super.applyParameters(program);

        Texture terrain = Assets.getTexture("engine:terrain");
        Texture terrainNormal = Assets.getTexture("engine:terrainNormal");
        Texture terrainHeight = Assets.getTexture("engine:terrainHeight");

        Texture water = Assets.getTexture("engine:waterStill");
        Texture lava = Assets.getTexture("engine:lavaStill");
        Texture waterNormal = Assets.getTexture("engine:waterNormal");
        Texture waterNormalAlt = Assets.getTexture("engine:waterNormalAlt");
        Texture effects = Assets.getTexture("engine:effects");

        if (terrain == null || water == null || lava == null || waterNormal == null || effects == null) {
            return;
        }

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, terrain.getId());
        program.setInt("textureAtlas", texId++, true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, water.getId());
        program.setInt("textureWater", texId++, true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, lava.getId());
        program.setInt("textureLava", texId++, true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, waterNormal.getId());
        program.setInt("textureWaterNormal", texId++, true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, waterNormalAlt.getId());
        program.setInt("textureWaterNormalAlt", texId++, true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, effects.getId());
        program.setInt("textureEffects", texId++, true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("sceneReflected");
        program.setInt("textureWaterReflection", texId++, true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("sceneOpaque");
        program.setInt("texSceneOpaque", texId++, true);

        if (CoreRegistry.get(Config.class).getRendering().isNormalMapping()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            glBindTexture(GL11.GL_TEXTURE_2D, terrainNormal.getId());
            program.setInt("textureAtlasNormal", texId++, true);

            if (CoreRegistry.get(Config.class).getRendering().isParallaxMapping()) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                glBindTexture(GL11.GL_TEXTURE_2D, terrainHeight.getId());
                program.setInt("textureAtlasHeight", texId++, true);
            }
        }

        Vector4f lightingSettingsFrag = new Vector4f();
        lightingSettingsFrag.z = waterSpecExp;
        program.setFloat4("lightingSettingsFrag", lightingSettingsFrag, true);

        Vector4f waterSettingsFrag = new Vector4f();
        waterSettingsFrag.x = waterNormalBias;
        waterSettingsFrag.y = waterRefraction;
        waterSettingsFrag.z = waterFresnelBias;
        waterSettingsFrag.w = waterFresnelPow;
        program.setFloat4("waterSettingsFrag", waterSettingsFrag, true);

        Vector4f alternativeWaterSettingsFrag = new Vector4f();
        alternativeWaterSettingsFrag.x = waterTint;
        program.setFloat4("alternativeWaterSettingsFrag", alternativeWaterSettingsFrag, true);

        if (CoreRegistry.get(Config.class).getRendering().isAnimateWater()) {
            program.setFloat("waveIntensFalloff", waveIntensFalloff, true);
            program.setFloat("waveSizeFalloff", waveSizeFalloff, true);
            program.setFloat("waveSize", waveSize, true);
            program.setFloat("waveSpeedFalloff", waveSpeedFalloff, true);
            program.setFloat("waveSpeed", waveSpeed, true);
            program.setFloat("waveIntens", waveIntens, true);
            program.setFloat("waterOffsetY", waterOffsetY, true);
            program.setFloat("waveOverallScale", waveOverallScale, true);
        }

        if (CoreRegistry.get(Config.class).getRendering().isParallaxMapping()
                && CoreRegistry.get(Config.class).getRendering().isNormalMapping()) {
            program.setFloat4("parallaxProperties", parallaxBias, parallaxScale, 0.0f, 0.0f, true);
        }
    }

}
