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
package org.terasology.rendering;

import org.terasology.utilities.Assets;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.opengl.GLSLMaterial;
import org.terasology.rendering.shader.ShaderParametersChunk;

/**
 */
public final class RenderHelper {

    // Parameters which are also defined on shader side
    private static final int OCEAN_OCTAVES = 16;
    private static final Vector2f[] OCEAN_WAVE_DIRECTIONS = {
            new Vector2f(-0.613392f, 0.617481f),
            new Vector2f(0.170019f, -0.040254f),
            new Vector2f(-0.299417f, 0.791925f),
            new Vector2f(0.645680f, 0.493210f),
            new Vector2f(-0.651784f, 0.717887f),
            new Vector2f(0.421003f, 0.027070f),
            new Vector2f(-0.817194f, -0.271096f),
            new Vector2f(-0.705374f, -0.668203f),
            new Vector2f(0.977050f, -0.108615f),
            new Vector2f(0.063326f, 0.142369f),
            new Vector2f(0.203528f, 0.214331f),
            new Vector2f(-0.667531f, 0.326090f),
            new Vector2f(-0.098422f, -0.295755f),
            new Vector2f(-0.885922f, 0.215369f),
            new Vector2f(0.566637f, 0.605213f),
            new Vector2f(0.039766f, -0.396100f)
    };

    private RenderHelper() {
    }

    // Various functions that are also available on the shader side but need to be
    // evaluated on the CPU
    public static float smoothCurve(float x) {
        return x * x * (3.f - 2.0f * x);
    }

    public static float triangleWave(float x) {
        float normX = x + 0.5f;
        float fract = normX - (float) Math.floor(normX);
        return Math.abs(fract * 2.0f - 1.0f);
    }

    public static float smoothTriangleWave(float x) {
        return smoothCurve(triangleWave(x)) * 2.0f - 1.0f;
    }

    public static float timeToTick(float time, float speed) {
        return time * 4000.0f * speed;
    }

    public static float evaluateOceanHeightAtPosition(Vector3f position, float days) {
        float height = 0.0f;

        GLSLMaterial chunkMaterial = (GLSLMaterial) Assets.getMaterial("engine:prog.chunk").get();
        ShaderParametersChunk chunkParameters = (ShaderParametersChunk) chunkMaterial.getShaderParameters();

        float size = chunkParameters.waveSize;
        float intens = chunkParameters.waveIntens;
        float timeFactor = chunkParameters.waveSpeed;

        for (int i = 0; i < OCEAN_OCTAVES; ++i) {
            height += (smoothTriangleWave(timeToTick(days,
                    timeFactor) + position.x * OCEAN_WAVE_DIRECTIONS[i].x * size + position.z * OCEAN_WAVE_DIRECTIONS[i].y * size) * 2.0 - 1.0) * intens;

            size *= chunkParameters.waveSizeFalloff;
            intens *= chunkParameters.waveIntensFalloff;
            timeFactor *= chunkParameters.waveSpeedFalloff;
        }

        height /= OCEAN_OCTAVES;

        return height + chunkParameters.waterOffsetY;
    }

}
