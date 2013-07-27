/*
 * Copyright 2013 Moving Blocks
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
import org.terasology.engine.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 * Parameters for the sky shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersSky extends ShaderParametersBase {

    private float sunExponent = 512.0f;
    private float moonExponent = 256.0f;
    private float skyDaylightBrightness = 1.3f;
    private float skyNightBrightness = 1.0f;

    public static Vector3d getAllWeatherZenith(float thetaSun, float turbidity) {
        thetaSun = (float) Math.acos(thetaSun);
        Vector4f cx1 = new Vector4f(0.0f, 0.00209f, -0.00375f, 0.00165f);
        Vector4f cx2 = new Vector4f(0.00394f, -0.03202f, 0.06377f, -0.02903f);
        Vector4f cx3 = new Vector4f(0.25886f, 0.06052f, -0.21196f, 0.11693f);
        Vector4f cy1 = new Vector4f(0.0f, 0.00317f, -0.00610f, 0.00275f);
        Vector4f cy2 = new Vector4f(0.00516f, -0.04153f, 0.08970f, -0.04214f);
        Vector4f cy3 = new Vector4f(0.26688f, 0.06670f, -0.26756f, 0.15346f);

        float t2 = turbidity * turbidity;
        float chi = (4.0f / 9.0f - turbidity / 120.0f) * ((float) Math.PI - 2.0f * thetaSun);

        Vector4f theta = new Vector4f(1, thetaSun, thetaSun * thetaSun, thetaSun * thetaSun * thetaSun);

        float Y = (4.0453f * turbidity - 4.9710f) * (float) Math.tan(chi) - 0.2155f * turbidity + 2.4192f;
        float x = t2 * cx1.dot(theta) + turbidity * cx2.dot(theta) + cx3.dot(theta);
        float y = t2 * cy1.dot(theta) + turbidity * cy2.dot(theta) + cy3.dot(theta);

        return new Vector3d(Y, x, y);
    }

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Assets.getTexture("engine:sky90").getId());
        program.setInt("texSky90", texId++, true);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Assets.getTexture("engine:sky180").getId());
        program.setInt("texSky180", texId++, true);

        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);

        if (worldProvider != null && worldRenderer != null) {
            program.setFloat("colorExp", worldRenderer.getSkysphere().getColorExp(), true);

            Vector3f sunDirection = worldRenderer.getSkysphere().getSunDirection(false);
            Vector3d zenithColor = getAllWeatherZenith(sunDirection.y, worldRenderer.getSkysphere().getTurbidity());

            program.setFloat("sunAngle", worldRenderer.getSkysphere().getSunPosAngle(), true);
            program.setFloat("turbidity", (Float) worldRenderer.getSkysphere().getTurbidity(), true);
            program.setFloat3("zenith", (float) zenithColor.x, (float) zenithColor.y, (float) zenithColor.z, true);
        }

        program.setFloat4("skySettings", sunExponent, moonExponent, skyDaylightBrightness, skyNightBrightness, true);
    }
}
