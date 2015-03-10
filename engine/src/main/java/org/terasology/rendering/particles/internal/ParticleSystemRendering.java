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
package org.terasology.rendering.particles.internal;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.terasology.asset.Assets;
import org.terasology.rendering.particles.internal.ParticlePool;
import org.terasology.rendering.particles.internal.ParticleSystemStateData;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.world.WorldRenderer;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;

/**
 * Created by Linus on 1-3-2015.
 */
public class ParticleSystemRendering {

    public static void render(final WorldRenderer worldRenderer, Iterable<ParticleSystemStateData> particleSystems) {
        // init our rendering environment
        Assets.getMaterial("engine:prog.newParticle").enable();
        Vector3f camPos = worldRenderer.getActiveCamera().getPosition();

        glDisable(GL11.GL_CULL_FACE);
        for (ParticleSystemStateData particleSystem: particleSystems) {


            //drawEmmiter(particleSystemComponent, camPos);
            drawParticles(particleSystem, camPos);
        }

        glEnable(GL11.GL_CULL_FACE);
    }
    /*
    private static void drawEmmiter(ParticleSystemComponent particleSystem, Vector3f camera) {
        Vector3f position = new Vector3f(particleSystem.emmiter.getComponent(LocationComponent.class).getWorldPosition());
        position.sub(camera);
        glPushMatrix();
        glTranslatef(position.x(), position.y(), position.z());

        glBegin(GL_QUADS);
        GL11.glVertex3f(-0.2f, 0.0f, 0.2f);
        GL11.glVertex3f(0.2f, 0.0f, 0.2f);
        GL11.glVertex3f(0.2f, 0.0f, -0.2f);
        GL11.glVertex3f(-0.2f, 0.0f, -0.2f);
        glEnd();
        glPopMatrix();
    }
    */
    private static void pushParticleDataToGPU() {

    }

    private static void drawParticles(ParticleSystemStateData particleSystem, Vector3f camera) {
        ParticlePool particlePool = particleSystem.particlePool;
        /*
        //loading the positions into a buffer
        FloatBuffer positions  = BufferUtils.createFloatBuffer(particlePool.position.length);
        positions.put(particlePool.position);
        Assets.getMaterial("engine:prog.newParticle").setFloat3("position", positions);

        FloatBuffer colors = BufferUtils.createFloatBuffer(particlePool.color.length);
        colors.put(particlePool.color);
        Assets.getMaterial("engine:prog.newParticle").setFloat4("color", colors);
        */

        // move into camera space
        glPushMatrix();
        glTranslatef(-camera.x(), -camera.y(), -camera.z());


        for(int i = 0; i < particlePool.livingParticles(); i++) {
            glPushMatrix();

            int i3 = i * 3;
            int i4 = i * 4;
            Vector3f pos = new Vector3f(
                    particlePool.position[i3 + 0],
                    particlePool.position[i3 + 1],
                    particlePool.position[i3 + 2]
            );
            glTranslatef(pos.x(), pos.y(), pos.z());
            applyOrientation();

            final float halfWidth  = 0.5f * particlePool.scale[i3 + 0];
            final float halfHeight = 0.5f * particlePool.scale[i3 + 1];

            glColor4f(particlePool.color[i4 + 0], particlePool.color[i4 + 1], particlePool.color[i4 + 2], particlePool.color[i4 + 3]);
            glBegin(GL_QUADS);
            GL11.glVertex3f(-halfWidth, +halfHeight, 0.0f);
            GL11.glVertex3f(+halfWidth, +halfHeight, 0.0f);
            GL11.glVertex3f(+halfWidth, -halfHeight, 0.0f);
            GL11.glVertex3f(-halfWidth, -halfHeight, 0.0f);
            glEnd();

            glPopMatrix();
        }

        //move out of camera space
        glPopMatrix();
    }

    private static void applyOrientation() {
        // Fetch the current modelview matrix
        final FloatBuffer model = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, model);

        // And undo all rotations and scaling
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    model.put(i * 4 + j, 1.0f);
                } else {
                    model.put(i * 4 + j, 0.0f);
                }
            }
        }

        GL11.glLoadMatrix(model);
    }
}
