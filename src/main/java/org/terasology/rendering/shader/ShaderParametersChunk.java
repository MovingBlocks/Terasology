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
import org.terasology.logic.manager.DefaultRenderingProcess;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.List;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Chunk shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersChunk extends ShaderParametersBase {
    private Texture water = Assets.getTexture("engine:custom_water_still");
    private Texture lava = Assets.getTexture("engine:custom_lava_still");
    private Texture waterNormal = Assets.getTexture("engine:water_normal");
    private Texture effects = Assets.getTexture("engine:effects");

    Property skyInscatteringLength = new Property("skyInscatteringLength", 1.0f, 0.0f, 1.0f);
    Property skyInscatteringStrength = new Property("skyInscatteringStrength", 0.075f, 0.0f, 1.0f);
    Property skyInscatteringThreshold = new Property("skyInscatteringThreshold", 0.60f, 0.0f, 1.0f);

    Property waveIntens = new Property("waveIntens", 1.0f, 0.0f, 2.0f);
    Property waveIntensFalloff = new Property("waveIntensFalloff", 0.88f, 0.0f, 2.0f);
    Property waveSize = new Property("waveSize", 0.24f, 0.0f, 2.0f);
    Property waveSizeFalloff = new Property("waveSizeFalloff", 0.88f, 0.0f, 2.0f);
    Property waveSpeed = new Property("waveSpeed", 0.14f, 0.0f, 2.0f);
    Property waveSpeedFalloff = new Property("waveSpeedFalloff", 0.75f, 0.0f, 2.0f);

    Property waveOverallScale = new Property("waveOverallScale", 1.0f, 0.0f, 2.0f);

    Property waterRefraction = new Property("waterRefraction", 0.04f, 0.0f, 1.0f);
    Property waterFresnelBias = new Property("waterFresnelBias", 0.01f, 0.01f, 0.1f);
    Property waterFresnelPow = new Property("waterFresnelPow", 2.5f, 0.0f, 10.0f);
    Property waterNormalBias = new Property("waterNormalBias", 25.0f, 1.0f, 100.0f);
    Property waterTint = new Property("waterTint", 0.24f, 0.0f, 1.0f);

    Property waterOffsetY = new Property("waterOffsetY", 0.0f, 0.0f, 5.0f);

    Property torchWaterSpecExp = new Property("torchWaterSpecExp", 30.0f, 0.0f, 64.0f);
    Property waterSpecExp = new Property("waterSpecExp", 512.0f, 0.0f, 1024.0f);
    Property torchSpecExp = new Property("torchSpecExp", 32.0f, 0.0f, 64.0f);

    Property shadowIntens = new Property("shadowIntens", 0.5f, 0.0f, 1.0f);
    Property shadowMapBias = new Property("shadowMapBias", 0.01f, 0.0f, 0.1f);

    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);

        Texture terrain = Assets.getTexture("engine:terrain");
        if (terrain == null) {
            return;
        }

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, terrain.getId());
        program.setInt("textureAtlas", texId++);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, water.getId());
        program.setInt("textureWater", texId++);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, lava.getId());
        program.setInt("textureLava", texId++);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, waterNormal.getId());
        program.setInt("textureWaterNormal", texId++);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, effects.getId());
        program.setInt("textureEffects", texId++);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().getFBO("sceneReflected").bindTexture();
        program.setInt("textureWaterReflection", texId++);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().getFBO("sceneOpaque").bindTexture();
        program.setInt("texSceneOpaque", texId++);

        if (CoreRegistry.get(Config.class).getRendering().isDynamicShadows()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().getFBO("sceneShadowMap").bindDepthTexture();
            program.setInt("texSceneShadowMap", texId++);

            Camera lightCamera = CoreRegistry.get(WorldRenderer.class).getLightCamera();
            if (lightCamera != null) {
                program.setMatrix4("lightViewProjMatrix", lightCamera.getViewProjectionMatrix());
            }

            Vector4f shadowSettingsFrag = new Vector4f();
            shadowSettingsFrag.x = (Float) shadowIntens.getValue();
            shadowSettingsFrag.y = (Float) shadowMapBias.getValue();

            program.setFloat4("shadowSettingsFrag", shadowSettingsFrag);
        }

        Vector4f lightingSettingsFrag = new Vector4f();
        lightingSettingsFrag.x = (Float) torchSpecExp.getValue();
        lightingSettingsFrag.y = (Float) torchWaterSpecExp.getValue();
        lightingSettingsFrag.z = (Float) waterSpecExp.getValue();
        program.setFloat4("lightingSettingsFrag", lightingSettingsFrag);

        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);

        if (worldProvider != null && worldRenderer != null) {
            Vector3f sunDirection = worldRenderer.getSkysphere().getSunDirection(false);

            Vector3d zenithColor = ShaderParametersSky.getAllWeatherZenith(sunDirection.y, (Float) worldRenderer.getSkysphere().getTurbidity().getValue());
            program.setFloat3("skyInscatteringColor", (float) zenithColor.x, (float) zenithColor.y, (float) zenithColor.z);

            Vector4f skyInscatteringSettingsFrag = new Vector4f();
            skyInscatteringSettingsFrag.x = (Float) worldRenderer.getSkysphere().getColorExp().getValue();
            skyInscatteringSettingsFrag.y = (Float) skyInscatteringStrength.getValue();
            skyInscatteringSettingsFrag.z = (Float) skyInscatteringLength.getValue();
            skyInscatteringSettingsFrag.w = (Float) skyInscatteringThreshold.getValue();
            program.setFloat4("skyInscatteringSettingsFrag", skyInscatteringSettingsFrag);
        }

        Vector4f waterSettingsFrag = new Vector4f();
        waterSettingsFrag.x = (Float) waterNormalBias.getValue();
        waterSettingsFrag.y = (Float) waterRefraction.getValue();
        waterSettingsFrag.z = (Float) waterFresnelBias.getValue();
        waterSettingsFrag.w = (Float) waterFresnelPow.getValue();
        program.setFloat4("waterSettingsFrag", waterSettingsFrag);

        Vector4f alternativeWaterSettingsFrag = new Vector4f();
        alternativeWaterSettingsFrag.x = (Float) waterTint.getValue();
        program.setFloat4("alternativeWaterSettingsFrag", alternativeWaterSettingsFrag);

        if (CoreRegistry.get(Config.class).getRendering().isAnimateWater()) {
            program.setFloat("waveIntensFalloff", (Float) waveIntensFalloff.getValue());
            program.setFloat("waveSizeFalloff", (Float) waveSizeFalloff.getValue());
            program.setFloat("waveSize", (Float) waveSize.getValue());
            program.setFloat("waveSpeedFalloff", (Float) waveSpeedFalloff.getValue());
            program.setFloat("waveSpeed", (Float) waveSpeed.getValue());
            program.setFloat("waveIntens", (Float) waveIntens.getValue());
            program.setFloat("waterOffsetY", (Float) waterOffsetY.getValue());
            program.setFloat("waveOverallScale", (Float) waveOverallScale.getValue());
        }

        program.setFloat("blockScale", 1.0f);
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(skyInscatteringLength);
        properties.add(skyInscatteringStrength);
        properties.add(skyInscatteringThreshold);
        properties.add(waveIntens);
        properties.add(waveIntensFalloff);
        properties.add(waveSize);
        properties.add(waveSizeFalloff);
        properties.add(waveSpeed);
        properties.add(waveSpeedFalloff);
        properties.add(torchSpecExp);
        properties.add(torchWaterSpecExp);
        properties.add(waterSpecExp);
        properties.add(waterNormalBias);
        properties.add(waterFresnelBias);
        properties.add(waterFresnelPow);
        properties.add(waterRefraction);
        properties.add(waterOffsetY);
        properties.add(waveOverallScale);
        properties.add(shadowIntens);
        properties.add(shadowMapBias);
        properties.add(waterTint);
    }
}
