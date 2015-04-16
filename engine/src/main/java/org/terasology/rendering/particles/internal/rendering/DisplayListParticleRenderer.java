/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.particles.internal.rendering;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.particles.components.ParticleSystemComponent;
import org.terasology.rendering.particles.internal.data.ParticlePool;
import org.terasology.rendering.particles.internal.data.ParticleSystemStateData;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Linus on 7-4-2015.
 */
class DisplayListParticleRenderer extends ParticleRenderer {

    private final DisplayList drawUnitQuad;

    public DisplayListParticleRenderer() {
        drawUnitQuad =  new DisplayList(() -> {
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

    public void finalize() throws Throwable {
        super.finalize();
        drawUnitQuad.dispose();
    }

    @Override
    public void dispose() {
        drawUnitQuad.dispose();
    }

    public void drawParticles(Material material, ParticleSystemStateData particleSystem, Vector3f camera) {
        ParticlePool particlePool = particleSystem.particlePool;
        ParticleSystemComponent component = particleSystem.entityRef.getComponent(ParticleSystemComponent.class);

        material.setBoolean("useTexture", component.texture != null);
        if (component.texture != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            glBindTexture(GL11.GL_TEXTURE_2D, component.texture.getId());

            material.setFloat2("texSize", component.textureSize.getX(), component.textureSize.getY());
        }

        glPushMatrix();
        glTranslatef(-camera.x(), -camera.y(), -camera.z());

        for (int i = 0; i < particlePool.livingParticles(); i++) {
            final int i2 = i * 2;
            final int i3 = i * 3;
            final int i4 = i * 4;

            material.setFloat3("position",
                    particlePool.position[i3],
                    particlePool.position[i3 + 1],
                    particlePool.position[i3 + 2]
            );

            material.setFloat3("scale",
                    particlePool.scale[i3],
                    particlePool.scale[i3 + 1],
                    particlePool.scale[i3 + 2]
            );

            material.setFloat4("color",
                    particlePool.color[i4],
                    particlePool.color[i4 + 1],
                    particlePool.color[i4 + 2],
                    particlePool.color[i4 + 3]
            );


            material.setFloat2("texOffset",
                    particlePool.textureOffset[i2],
                    particlePool.textureOffset[i2 + 1]
            );

            drawUnitQuad.call();
        }

        glPopMatrix();
    }

    private static class DisplayList {
        private static final int DISPOSED = 0;
        private int id;

        public DisplayList(Runnable commands) {
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
