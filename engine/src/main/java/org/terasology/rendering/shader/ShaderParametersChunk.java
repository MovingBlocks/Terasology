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
import org.terasology.utilities.Assets;
import org.terasology.config.Config;
import org.terasology.math.geom.Vector4f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FrameBuffersManager;

import java.util.Optional;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Chunk shader program.
 *
 */
public class ShaderParametersChunk extends ShaderParametersBase {
    @Range(min = 0.0f, max = 2.0f)
    public float waveIntens = 2.0f;
    @Range(min = 0.0f, max = 2.0f)
    public float waveIntensFalloff = 0.85f;
    @Range(min = 0.0f, max = 2.0f)
    public float waveSize = 0.1f;
    @Range(min = 0.0f, max = 2.0f)
    public float waveSizeFalloff = 1.25f;
    @Range(min = 0.0f, max = 2.0f)
    public float waveSpeed = 0.1f;
    @Range(min = 0.0f, max = 2.0f)
    public float waveSpeedFalloff = 0.95f;
    @Range(min = 0.0f, max = 5.0f)
    public float waterOffsetY;

    @Range(min = 0.0f, max = 2.0f)
    public float waveOverallScale = 1.0f;

    @Range(min = 0.0f, max = 1.0f)
    float waterRefraction = 0.04f;
    @Range(min = 0.0f, max = 0.1f)
    float waterFresnelBias = 0.01f;
    @Range(min = 0.0f, max = 10.0f)
    float waterFresnelPow = 2.5f;
    @Range(min = 1.0f, max = 100.0f)
    float waterNormalBias = 10.0f;
    @Range(min = 0.0f, max = 1.0f)
    float waterTint = 0.24f;

    @Range(min = 0.0f, max = 1024.0f)
    float waterSpecExp = 200.0f;

    @Range(min = 0.0f, max = 0.5f)
    float parallaxBias = 0.05f;
    @Range(min = 0.0f, max = 0.50f)
    float parallaxScale = 0.05f;

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        Optional<Texture> terrain = Assets.getTexture("engine:terrain");
        Optional<Texture> terrainNormal = Assets.getTexture("engine:terrainNormal");
        Optional<Texture> terrainHeight = Assets.getTexture("engine:terrainHeight");

        Optional<Texture> water = Assets.getTexture("engine:waterStill");
        Optional<Texture> lava = Assets.getTexture("engine:lavaStill");
        Optional<Texture> waterNormal = Assets.getTexture("engine:waterNormal");
        Optional<Texture> waterNormalAlt = Assets.getTexture("engine:waterNormalAlt");
        Optional<Texture> effects = Assets.getTexture("engine:effects");

        if (!terrain.isPresent() || !water.isPresent() || !lava.isPresent() || !waterNormal.isPresent() || !effects.isPresent()) {
            return;
        }

        FrameBuffersManager buffersManager = CoreRegistry.get(FrameBuffersManager.class);

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, terrain.get().getId());
        program.setInt("textureAtlas", texId++, true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, water.get().getId());
        program.setInt("textureWater", texId++, true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, lava.get().getId());
        program.setInt("textureLava", texId++, true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, waterNormal.get().getId());
        program.setInt("textureWaterNormal", texId++, true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, waterNormalAlt.get().getId());
        program.setInt("textureWaterNormalAlt", texId++, true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, effects.get().getId());
        program.setInt("textureEffects", texId++, true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        buffersManager.bindFboColorTexture("sceneReflected");
        program.setInt("textureWaterReflection", texId++, true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        buffersManager.bindFboColorTexture("sceneOpaque");
        program.setInt("texSceneOpaque", texId++, true);

        if (CoreRegistry.get(Config.class).getRendering().isNormalMapping()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            glBindTexture(GL11.GL_TEXTURE_2D, terrainNormal.get().getId());
            program.setInt("textureAtlasNormal", texId++, true);

            if (CoreRegistry.get(Config.class).getRendering().isParallaxMapping()) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                glBindTexture(GL11.GL_TEXTURE_2D, terrainHeight.get().getId());
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
