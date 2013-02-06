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

import static org.lwjgl.opengl.GL11.glBindTexture;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.asset.Assets;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.PostProcessingRenderer;
import org.terasology.editor.properties.Property;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import java.util.List;

/**
 * Shader parameters for the Chunk shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersChunk extends ShaderParametersBase {
    private Texture lava = Assets.getTexture("engine:custom_lava_still");
    private Texture water = Assets.getTexture("engine:water_normal");
    private Texture effects = Assets.getTexture("engine:effects");

    Property skyInscatteringLength = new Property("skyInscatteringLength", 0.9f, 0.0f, 1.0f);
    Property skyInscatteringStrength = new Property("skyInscatteringStrength", 0.25f, 0.0f, 1.0f);

    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);

        Texture terrain = Assets.getTexture("engine:terrain");
        if (terrain == null) {
            return;
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        glBindTexture(GL11.GL_TEXTURE_2D, lava.getId());
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        glBindTexture(GL11.GL_TEXTURE_2D, water.getId());
        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        glBindTexture(GL11.GL_TEXTURE_2D, effects.getId());
        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        PostProcessingRenderer.getInstance().getFBO("sceneReflected").bindTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        glBindTexture(GL11.GL_TEXTURE_2D, terrain.getId());

        program.setInt("textureLava", 1);
        program.setInt("textureWaterNormal", 2);
        program.setInt("textureEffects", 3);
        program.setInt("textureWaterReflection", 4);
        program.setInt("textureAtlas", 0);

        program.setFloat("blockScale", 1.0f);

        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);

        if (worldProvider != null && worldRenderer != null) {
            float sunAngle = (float) worldRenderer.getSkysphere().getSunPosAngle();
            Vector4d sunNormalise = new Vector4d(0.0f, java.lang.Math.cos(sunAngle), java.lang.Math.sin(sunAngle), 1.0);
            sunNormalise.normalize();

            Vector3d zenithColor = ShaderParametersSky.getAllWeatherZenith((float) sunNormalise.y, (Float) worldRenderer.getSkysphere().getTurbidity().getValue());

            program.setFloat("skyInscatteringExponent", (Float) worldRenderer.getSkysphere().getColorExp().getValue());
            program.setFloat3("skyInscatteringColor", (float) zenithColor.x, (float) zenithColor.y, (float) zenithColor.z);
            program.setFloat("skyInscatteringLength", (Float) skyInscatteringLength.getValue());
            program.setFloat("skyInscatteringStrength", (Float) skyInscatteringStrength.getValue());
        }
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(skyInscatteringLength);
        properties.add(skyInscatteringStrength);
    }
}
