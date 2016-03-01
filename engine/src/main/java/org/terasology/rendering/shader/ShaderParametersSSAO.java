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

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Post-processing shader program.
 *
 */
public class ShaderParametersSSAO extends ShaderParametersBase {

    public static final int SSAO_KERNEL_ELEMENTS = 32;
    public static final int SSAO_NOISE_SIZE = 4;

    private final Random random = new FastRandom();

    @Range(min = 0.01f, max = 12.0f)
    private float ssaoStrength = 1.75f;
    @Range(min = 0.1f, max = 25.0f)
    private float ssaoRad = 1.5f;

    private FloatBuffer ssaoSamples;

    @Override
    public void initialParameters(Material material) {
        if (ssaoSamples == null) {
            ssaoSamples = BufferUtils.createFloatBuffer(SSAO_KERNEL_ELEMENTS * 3);

            for (int i = 0; i < SSAO_KERNEL_ELEMENTS; ++i) {
                Vector3f vec = new Vector3f();
                vec.x = random.nextFloat(-1.0f, 1.0f);
                vec.y = random.nextFloat(-1.0f, 1.0f);
                vec.z = random.nextFloat();

                vec.normalize();
                vec.scale(random.nextFloat(0.0f, 1.0f));
                float scale = i / (float) SSAO_KERNEL_ELEMENTS;
                scale = TeraMath.lerp(0.25f, 1.0f, scale * scale);

                vec.scale(scale);

                ssaoSamples.put(vec.x);
                ssaoSamples.put(vec.y);
                ssaoSamples.put(vec.z);
            }

            ssaoSamples.flip();
        }
        material.setFloat3("ssaoSamples", ssaoSamples);
    }

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        FBO scene = CoreRegistry.get(FrameBuffersManager.class).getFBO("sceneOpaque");

        int texId = 0;

        if (scene != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            scene.bindDepthTexture();
            program.setInt("texDepth", texId++, true);
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            scene.bindNormalsTexture();
            program.setInt("texNormals", texId++, true);
        }

        Texture ssaoNoiseTexture = updateNoiseTexture();

        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        glBindTexture(GL11.GL_TEXTURE_2D, ssaoNoiseTexture.getId());
        program.setInt("texNoise", texId++, true);

        program.setFloat4("ssaoSettings", ssaoStrength, ssaoRad, 0.0f, 0.0f, true);

        if (CoreRegistry.get(WorldRenderer.class) != null) {
            Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
            if (activeCamera != null) {
                program.setMatrix4("invProjMatrix", activeCamera.getInverseProjectionMatrix(), true);
                program.setMatrix4("projMatrix", activeCamera.getProjectionMatrix(), true);
            }
        }
    }

    private Texture updateNoiseTexture() {
        Optional<Texture> texture = CoreRegistry.get(AssetManager.class).getAsset("engine:ssaoNoise", Texture.class);
        if (!texture.isPresent()) {
            ByteBuffer noiseValues = BufferUtils.createByteBuffer(SSAO_NOISE_SIZE * SSAO_NOISE_SIZE * 4);

            for (int i = 0; i < SSAO_NOISE_SIZE * SSAO_NOISE_SIZE; ++i) {
                Vector3f noiseVector = new Vector3f(random.nextFloat(-1.0f, 1.0f), random.nextFloat(-1.0f, 1.0f), 0.0f);
                noiseVector.normalize();

                noiseValues.put((byte) ((noiseVector.x * 0.5 + 0.5) * 255.0f));
                noiseValues.put((byte) ((noiseVector.y * 0.5 + 0.5) * 255.0f));
                noiseValues.put((byte) ((noiseVector.z * 0.5 + 0.5) * 255.0f));
                noiseValues.put((byte) 0x0);
            }

            noiseValues.flip();

            return Assets.generateAsset(new ResourceUrn("engine:ssaoNoise"), new TextureData(SSAO_NOISE_SIZE, SSAO_NOISE_SIZE,
                    new ByteBuffer[]{noiseValues}, Texture.WrapMode.REPEAT, Texture.FilterMode.NEAREST), Texture.class);
        }
        return texture.get();
    }

}
