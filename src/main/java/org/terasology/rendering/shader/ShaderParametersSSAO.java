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

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.asset.Assets;
import org.terasology.game.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.rendering.assets.GLSLShaderProgramInstance;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.renderingProcesses.DefaultRenderingProcess;
import org.terasology.editor.properties.Property;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersSSAO extends ShaderParametersBase {

    private static final int SSAO_KERNEL_SIZE = 16;
    private static final int SSAO_NOISE_SIZE = 4;

    private static final FastRandom rand = new FastRandom();

    private Property ssaoStrength = new Property("ssaoStrength", 2.5f, 0.01f, 12.0f);
    private Property ssaoRad = new Property("ssaoRad", 4.0f, 0.1f, 25.0f);

    private Texture ssaoNoiseTexture = null;
    private FloatBuffer ssaoSamples = null;

    @Override
    public void applyParameters(GLSLShaderProgramInstance program) {
        super.applyParameters(program);

        DefaultRenderingProcess.FBO scene = DefaultRenderingProcess.getInstance().getFBO("sceneOpaque");

        int texId = 0;

        if (scene != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            scene.bindDepthTexture();
            program.setInt("texDepth", texId++);
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            scene.bindNormalsTexture();
            program.setInt("texNormals", texId++);
        }

        updateAndSetHemisphereSamples(program);
        updateNoiseTexture();

        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        glBindTexture(GL11.GL_TEXTURE_2D, ssaoNoiseTexture.getId());
        program.setInt("texNoise", texId++);

        program.setFloat4("ssaoSettings", (Float) ssaoStrength.getValue(), (Float) ssaoRad.getValue(), 0.0f, 0.0f);

        Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        if (activeCamera != null) {
            program.setMatrix4("invProjMatrix", activeCamera.getInverseProjectionMatrix());
            program.setMatrix4("projMatrix", activeCamera.getProjectionMatrix());
        }
    }

    private void updateNoiseTexture() {
        if (ssaoNoiseTexture == null) {
            ByteBuffer noiseValues = BufferUtils.createByteBuffer(SSAO_NOISE_SIZE*SSAO_NOISE_SIZE*4);

            for (int i=0; i<SSAO_NOISE_SIZE*SSAO_NOISE_SIZE; ++i) {
                Vector3f noiseVector = new Vector3f(rand.randomFloat(), rand.randomFloat(), 0.0f);
                noiseVector.normalize();

                noiseValues.put((byte) ((noiseVector.x * 0.5 + 0.5) * 255.0f));
                noiseValues.put((byte) ((noiseVector.y * 0.5 + 0.5) * 255.0f));
                noiseValues.put((byte) ((noiseVector.z * 0.5 + 0.5) * 255.0f));
                noiseValues.put((byte) 0x0);
            }

            noiseValues.flip();

            ssaoNoiseTexture = new Texture(new ByteBuffer[] { noiseValues }, SSAO_NOISE_SIZE, SSAO_NOISE_SIZE, Texture.WrapMode.Repeat, Texture.FilterMode.Nearest);
        }

    }

    private void updateAndSetHemisphereSamples(GLSLShaderProgramInstance program) {
        if (ssaoSamples == null) {
            ssaoSamples = BufferUtils.createFloatBuffer(SSAO_KERNEL_SIZE*3);

            for (int i=0; i<SSAO_KERNEL_SIZE; ++i) {
                Vector3f vec = new Vector3f();
                vec.x = rand.randomFloat();
                vec.y = rand.randomFloat();
                vec.z = rand.randomPosFloat();

                vec.normalize();
                vec.scale(rand.randomPosFloat());
                float scale = i / (float) SSAO_KERNEL_SIZE;
                scale = TeraMath.lerpf(0.1f, 1.0f, scale * scale);

                vec.scale(scale);

                ssaoSamples.put(vec.x);
                ssaoSamples.put(vec.y);
                ssaoSamples.put(vec.z);
            }

            ssaoSamples.flip();
        }

        if (!program.wasSet("ssaoSamples")) {
            program.setFloat3("ssaoSamples", ssaoSamples);
        }
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(ssaoStrength);
        properties.add(ssaoRad);
    }
}
