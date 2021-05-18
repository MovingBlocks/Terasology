// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.rendering;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.RenderSystem;
import org.terasology.engine.particles.ParticleSystemManager;
import org.terasology.engine.particles.components.ParticleDataSpriteComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.cameras.PerspectiveCamera;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.utilities.Assets;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * ParticleRenderer for basic sprite particle systems.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class SpriteParticleRenderer implements RenderSystem {

    protected static final String PARTICLE_MATERIAL_URI = "engine:prog.particle";

    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    private  boolean opengl33 = false;
    @In
    WorldRenderer worldRenderer;

    @In
    ParticleSystemManager particleSystemManager;

    public void drawParticles(Material material, ParticleRenderingData<ParticleDataSpriteComponent> particleSystem) {
        ParticleDataSpriteComponent particleData = particleSystem.particleData;

        if (particleSystem.particleData.texture != null) {
            material.setBoolean("use_texture", true);
            material.setFloat2("texture_size", particleData.textureSize);
            material.setInt("texture_sampler", 0);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            glBindTexture(GL11.GL_TEXTURE_2D, particleData.texture.getId());
        }

        particleSystem.particlePool.draw();
    }

    @Override
    public void renderOpaque() {

    }

    @Override
    public void renderAlphaBlend() {
        if (!opengl33) {
            return;
        }
        PerspectiveCamera camera = (PerspectiveCamera) worldRenderer.getActiveCamera();
        Vector3f cameraPosition = camera.getPosition();
        Matrix4f viewProjection = new Matrix4f(camera.getViewProjectionMatrix())
            .translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);

        Material material = Assets.getMaterial(PARTICLE_MATERIAL_URI).get();
        material.enable();
        material.setFloat3("camera_position", cameraPosition.x, cameraPosition.y, cameraPosition.z);
        material.setMatrix4("view_projection", viewProjection.get(matrixBuffer));

        particleSystemManager.getParticleEmittersByDataComponent(ParticleDataSpriteComponent.class)
            .forEach(particleSystem -> drawParticles(material, particleSystem));
    }

    @Override
    public void renderOverlay() {

    }

    @Override
    public void renderShadows() {

    }

    @Override
    public void initialise() {
        opengl33 = GL.createCapabilities().OpenGL33;
    }

    @Override
    public void preBegin() {

    }

    @Override
    public void postBegin() {

    }

    @Override
    public void preSave() {

    }

    @Override
    public void postSave() {

    }

    @Override
    public void shutdown() {
    }
}
