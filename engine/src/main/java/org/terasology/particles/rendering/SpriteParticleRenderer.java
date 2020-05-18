/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.particles.rendering;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.lwjgl.LwjglDisplayDevice;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.particles.ParticleSystemManager;
import org.terasology.particles.components.ParticleDataSpriteComponent;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.PerspectiveCamera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.Assets;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glDeleteLists;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;

/**
 * ParticleRenderer for basic sprite particle systems.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class SpriteParticleRenderer implements RenderSystem {

    protected static final String PARTICLE_MATERIAL_URI = "engine:prog.particle";
    private Material material;

    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    /**
     * Vertices of a unit quad on the xy plane, centered on the origin.
     * Vertices are in counter-clockwise order starting from the bottom right vertex.
     *
     * @return vertices coordinates
     */
    protected static final float[] UNIT_QUAD_VERTICES = {
            +0.5f, -0.5f, 0.0f,
            +0.5f, +0.5f, 0.0f,
            -0.5f, +0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f
    };
    @In
    WorldRenderer worldRenderer;

    @In
    ParticleSystemManager particleSystemManager;

    @In
    DisplayDevice displayDevice;

    private DisplayList drawUnitQuad;

    public void finalize() throws Throwable {
        super.finalize();
        if (null != drawUnitQuad) {
            drawUnitQuad.dispose();
        }
    }

    public void dispose() {
        if (null != drawUnitQuad) {
            drawUnitQuad.dispose();
        }
    }

    public void drawParticles(ParticleRenderingData<ParticleDataSpriteComponent> particleSystem) {
        ParticleDataSpriteComponent particleData = particleSystem.particleData;

        if (particleSystem.particleData.texture != null) {
            material.setBoolean("use_texture", true);
            material.setFloat2("texture_size", particleData.textureSize.x, particleData.textureSize.y);
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
        PerspectiveCamera camera = (PerspectiveCamera) worldRenderer.getActiveCamera();
        Vector3f cameraPosition = camera.getPosition();
        Matrix4f viewProjection = new Matrix4f(camera.getViewProjectionMatrix())
                .transpose()
                .translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);

        material = Assets.getMaterial(PARTICLE_MATERIAL_URI).get();
        material.enable();
        material.setFloat3("camera_position", cameraPosition.x, cameraPosition.y, cameraPosition.z);
        material.setMatrix4("view_projection", viewProjection.get(matrixBuffer));

        particleSystemManager.getParticleEmittersByDataComponent(ParticleDataSpriteComponent.class)
                .forEach(this::drawParticles);
    }

    @Override
    public void renderOverlay() {

    }

    @Override
    public void renderShadows() {

    }

    @Override
    public void initialise() {
        // Nasty hack to only run LWJGL code with a LwjglDisplayDevice.  Should be unnecessary once "big todo" for drawParticles is resolved.
        if (!(displayDevice instanceof LwjglDisplayDevice)) {
            return;
        }

        drawUnitQuad = new DisplayList(() -> {
            glBegin(GL_TRIANGLE_FAN);
            GL11.glTexCoord2f(UNIT_QUAD_VERTICES[0] + 0.5f, -UNIT_QUAD_VERTICES[1] + 0.5f);
            GL11.glVertex3f(UNIT_QUAD_VERTICES[0], UNIT_QUAD_VERTICES[1], UNIT_QUAD_VERTICES[2]);

            GL11.glTexCoord2f(UNIT_QUAD_VERTICES[3] + 0.5f, -UNIT_QUAD_VERTICES[4] + 0.5f);
            GL11.glVertex3f(UNIT_QUAD_VERTICES[3], UNIT_QUAD_VERTICES[4], UNIT_QUAD_VERTICES[5]);

            GL11.glTexCoord2f(UNIT_QUAD_VERTICES[6] + 0.5f, -UNIT_QUAD_VERTICES[7] + 0.5f);
            GL11.glVertex3f(UNIT_QUAD_VERTICES[6], UNIT_QUAD_VERTICES[7], UNIT_QUAD_VERTICES[8]);

            GL11.glTexCoord2f(UNIT_QUAD_VERTICES[9] + 0.5f, -UNIT_QUAD_VERTICES[10] + 0.5f);
            GL11.glVertex3f(UNIT_QUAD_VERTICES[9], UNIT_QUAD_VERTICES[10], UNIT_QUAD_VERTICES[11]);
            glEnd();
        });
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
        dispose();
    }

    private static class DisplayList {
        private static final int DISPOSED = 0;
        private int id;

        DisplayList(Runnable commands) {
            id = glGenLists(1);
            glNewList(id, GL11.GL_COMPILE);
            commands.run();
            glEndList();
        }

        public void call() {
            glCallList(id);
        }

        public void dispose() {
            if (id != DISPOSED) {
                glDeleteLists(id, 1);
                id = DISPOSED;
            }
        }

        @Override
        public void finalize() throws Throwable {
            super.finalize();
            dispose();
        }
    }
}
