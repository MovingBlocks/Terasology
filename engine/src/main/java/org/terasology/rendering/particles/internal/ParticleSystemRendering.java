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
import org.lwjgl.opengl.*;
import org.terasology.asset.Assets;
import org.terasology.logic.location.LocationComponent;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.particles.components.ParticleSystemComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.world.WorldRenderer;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL15.glBindBuffer;

/**
 * Created by Linus on 1-3-2015.
 */
public class ParticleSystemRendering {

    private static boolean useInstancedDrawing;

    // instanced drawing
    private static VertexBuffer vertexVBO;
    private static VertexBuffer positionVBO;
    private static VertexBuffer colorVBO;
    private static VertexBuffer scaleVBO;

    private static FloatBuffer unitQuadVertices;
    // non-instanced drawing
    private static DisplayList drawUnitQuad;

    private static void bakeUnitQuad() {
        final float left    = -0.5f;
        final float right   = +0.5f;
        final float bottom  = -0.5f;
        final float top     = +0.5f;

        //if (useInstancedDrawing) {
            unitQuadVertices = BufferUtils.createFloatBuffer(4 * 3);
            unitQuadVertices.put(new float[]{
                    left, top, 0.0f,
                    right, top, 0.0f,
                    right, bottom, 0.0f,
                    left, bottom, 0.0f
            });
        //} else {
            drawUnitQuad = new DisplayList(() -> {
                glBegin(GL_QUADS);
                GL11.glVertex3f(left,  top,    0.0f);
                GL11.glVertex3f(right, top,    0.0f);
                GL11.glVertex3f(right, bottom, 0.0f);
                GL11.glVertex3f(left,  bottom, 0.0f);
                glEnd();
            });
        //}
    }

    public static void initialise() {
        useInstancedDrawing = GLContext.getCapabilities().GL_ARB_draw_instanced;

        if (useInstancedDrawing) {
            vertexVBO = new VertexBuffer();
            positionVBO = new VertexBuffer();
            colorVBO = new VertexBuffer();
            scaleVBO = new VertexBuffer();
        }

        bakeUnitQuad();
    }

    public static void shutdown() {
        if (useInstancedDrawing) {
            vertexVBO.dispose();
            vertexVBO = null;

            positionVBO.dispose();
            positionVBO = null;

            colorVBO.dispose();
            colorVBO = null;

            scaleVBO.dispose();
            scaleVBO = null;
        } else {
            drawUnitQuad.dispose();
            drawUnitQuad = null;
        }
    }


    public static void render(final WorldRenderer worldRenderer, Iterable<ParticleSystemStateData> particleSystems) {
        // init our rendering environment
        Material material = Assets.getMaterial("engine:prog.newParticle");
        material.enable();
        Vector3f camPos = worldRenderer.getActiveCamera().getPosition();

        glDisable(GL11.GL_CULL_FACE);
        for (ParticleSystemStateData particleSystem: particleSystems) {
            drawEmmiter(material, particleSystem, camPos);
            //drawParticles(material, particleSystem, camPos);
            //drawParticlesDisplayList(material, particleSystem, camPos);
            drawParticlesInstanced(material, particleSystem, camPos);
        }

        glEnable(GL11.GL_CULL_FACE);
    }

    private static void drawEmmiter(final Material material, ParticleSystemStateData particleSystem, Vector3f camera) {
        Vector3f position = particleSystem.entityRef.getComponent(ParticleSystemComponent.class).emitter.getComponent(LocationComponent.class).getWorldPosition();

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


    private static void drawParticlesDisplayList(final Material material, ParticleSystemStateData particleSystem, Vector3f camera) {
        ParticlePool particlePool = particleSystem.particlePool;

        // move into camera space
        glPushMatrix();
        glTranslatef(-camera.x(), -camera.y(), -camera.z());

        for (int i = 0; i < particlePool.livingParticles(); i++ ) {
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

            drawUnitQuad.call();
        }

        glPopMatrix();
    }

    private static void drawParticlesInstanced(final Material material, ParticleSystemStateData particleSystem, Vector3f camera) {
        ParticlePool particlePool = particleSystem.particlePool;

        // move into camera space
        glPushMatrix();
        glTranslatef(-camera.x(), -camera.y(), -camera.z());

        material.setFloat3("scale",
                1,
                1,
                1
        );

        material.setFloat4("color",
                1,
                0,
                0,
                1
        );



        final int partsPerPass = 1024;
        final int nrOfPasses = (particlePool.livingParticles() + partsPerPass - 1) / partsPerPass;

        material.enableVertexAttributeArray("position", true);
        FloatBuffer positionBuffer = BufferUtils.createFloatBuffer(partsPerPass * 3);

        material.enableVertexAttributeArray("color", true);
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(partsPerPass * 4);

        for (int pass = 0; pass < nrOfPasses; pass++ ) {
            int from = pass * partsPerPass;
            int from3 = from * 3;
            int from4 = from * 4;

            int count  = Math.min(partsPerPass, particlePool.livingParticles() - from );
            int count3 = Math.min(partsPerPass * 3, particlePool.livingParticles() * 3 - from3);
            int count4 = Math.min(partsPerPass * 4, particlePool.livingParticles() * 4 - from4);



            colorBuffer.clear();
            colorBuffer.put(particlePool.color, from4, count4);
            colorBuffer.flip();

            colorVBO.bind();
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, positionBuffer, GL15.GL_STATIC_DRAW);
            material.setVertexAttribPointer("color", 4, false, 0, 0L, true);
            glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);



            positionBuffer.clear();
            positionBuffer.put(particlePool.position, from3, count3);
            positionBuffer.flip();

            positionVBO.bind();
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, positionBuffer, GL15.GL_STATIC_DRAW);
            material.setVertexAttribPointer("position", 3, false, 0, 0L, true);
            glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);


            for(int particleI = from; particleI < (from + count); particleI++) {
                /*final int i3 = particleI * 3;


                material.setFloat3("position",
                        particlePool.position[i3],
                        particlePool.position[i3 + 1],
                        particlePool.position[i3 + 2]
                );
                */

                drawUnitQuad.call();
            }
        }

        material.disableVertexAttributeArray("position", true);

        glPopMatrix();
    }

    private static void drawParticles(final Material material, ParticleSystemStateData particleSystem, Vector3f camera) {
        ParticlePool particlePool = particleSystem.particlePool;

        // move into camera space
        glPushMatrix();
        glTranslatef(-camera.x(), -camera.y(), -camera.z());


        //For maximal hardware compatibility, only load data of 1024 particles at once
        final int partsPerPass = 1024;
        final int nrOfPasses = (particlePool.livingParticles() + partsPerPass - 1) / partsPerPass;

        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(partsPerPass * 4);
        FloatBuffer positionBuffer = BufferUtils.createFloatBuffer(partsPerPass * 3);
        FloatBuffer scaleBuffer = BufferUtils.createFloatBuffer(partsPerPass * 3);

        for (int pass = 0; pass < nrOfPasses; pass++ ) {
            int from = pass * partsPerPass;
            int from3 = from * 3;
            int from4 = from * 4;

            int count  = Math.min(partsPerPass, particlePool.livingParticles() - from );
            int count3 = Math.min(partsPerPass * 3, particlePool.livingParticles() * 3 - from3);
            int count4 = Math.min(partsPerPass * 4, particlePool.livingParticles() * 4 - from4);

            /*
            colorBuffer.clear();
            colorBuffer.put(particlePool.color, from4, count4);
            colorBuffer.flip();

            material.enableVertexAttributeArray("color", true);
            colorVBO.bind();
            material.setFloatVertexBuffer("color", 4, false, 0, colorBuffer, true);
            glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            //material.setFloat4("color", colorBuffer, true);

            positionBuffer.clear();
            positionBuffer.put(particlePool.position, from3, count3);
            positionBuffer.flip();

            material.enableVertexAttributeArray("position", true);
            positionVBO.bind();
            material.setFloatVertexBuffer("position", 3, false, 0, positionBuffer, true);
            glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

            scaleBuffer.clear();
            scaleBuffer.put(particlePool.scale, from3, count3);
            scaleBuffer.flip();

            material.enableVertexAttributeArray("scale", true);
            scaleVBO.bind();
            material.setFloatVertexBuffer("scale", 3, false, 0, scaleBuffer, true);
            glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            //material.setFloat3("scale", scaleBuffer, true);

            *
            vertexVBO.bind();
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, unitQuadVertices, GL15.GL_STATIC_DRAW);
            glVertexPointer(3, GL_FLOAT, 0, 0);
            glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

            ARBDrawInstanced.glDrawArraysInstancedARB(GL_TRIANGLE_STRIP, 0, 4, count);

            glDisableClientState(GL_VERTEX_ARRAY);
            glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            //glBindVertexArray(0);


            */


            for(int particleI = from; particleI < (from + count); particleI++) {
                material.setFloat3("color", particlePool.color[particleI + 0], particlePool.color[particleI + 1], particlePool.color[particleI + 2]);
                material.setFloat3("position", particlePool.position[particleI + 0], particlePool.position[particleI + 1], particlePool.position[particleI + 2]);

                glBegin(GL_QUADS);
                GL11.glVertex3f(-0.5f,  0.5f,    0.0f);
                GL11.glVertex3f(0.5f, 0.5f,    0.0f);
                GL11.glVertex3f(0.5f, -0.5f, 0.0f);
                GL11.glVertex3f(-0.5f,  -0.5f, 0.0f);
                glEnd();

                //drawUnitQuad.call();
            }

            /*
            material.disableVertexAttributeArray("scale", true);
            material.disableVertexAttributeArray("position", true);
            material.disableVertexAttributeArray("color", true);
            /*
            final float left    = -0.5f;
            final float right   = +0.5f;
            final float bottom  = -0.5f;
            final float top     = +0.5f;


            for(int particleI = from; particleI < to; particleI++) {
                material.setInt("particleIndex", particleI % partsPerPass);
                glBegin(GL_QUADS);
                GL11.glVertex3f(left,  top,    0.0f);
                GL11.glVertex3f(right, top,    0.0f);
                GL11.glVertex3f(right, bottom, 0.0f);
                GL11.glVertex3f(left,  bottom, 0.0f);
                glEnd();
                //drawUnitQuad.call();
            }
             */
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

    private static class VertexBuffer {
        private static final int DISPOSED = 0;
        private int id;

        public VertexBuffer() {
            id = GL15.glGenBuffers();
        }

        public void bind() {
            glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        }

        public void dispose() {
            if (id != DISPOSED) {
                GL15.glDeleteBuffers(id);
                id = DISPOSED;
            }
        }

        public int getId() {
            return id;
        }

        @Override
        public void finalize() throws Throwable {
            super.finalize();
            dispose();
        }
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
