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

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.particles.internal.ParticlePool;
import org.terasology.rendering.particles.internal.ParticleSystemStateData;

import java.nio.BufferOverflowException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Linus on 8-4-2015.
 */
class InstancedParticleRenderer extends ParticleRenderer {

    private static final int MAX_INSTANCES_PER_DRAW = 1024;

    private final int vaoId;

    private final int quadVertexVboId;
    private final int positionVboId;
    private final int colorVboId;
    private final int scaleVboId;

    private final FloatBuffer positionBuffer;
    private final FloatBuffer scaleBuffer;
    private final FloatBuffer colorBuffer;

    public static boolean hardwareIsCapable() {
        final ContextCapabilities capabilities = GLContext.getCapabilities();

        return capabilities.GL_ARB_draw_instanced
            && capabilities.GL_ARB_instanced_arrays
            && capabilities.GL_ARB_vertex_array_object;
    }

    InstancedParticleRenderer(Material material) {
        vaoId = ARBVertexArrayObject.glGenVertexArrays();

        IntBuffer vboIdBuffer = BufferUtils.createIntBuffer(4);
        GL15.glGenBuffers(vboIdBuffer);

        ARBVertexArrayObject.glBindVertexArray(vaoId);
        quadVertexVboId = vboIdBuffer.get();
        positionVboId = vboIdBuffer.get();
        colorVboId = vboIdBuffer.get();
        scaleVboId = vboIdBuffer.get();

        initializeQuadVertexBuffer();
        Util.checkGLError();
        positionBuffer = null;//initializePositionBuffer(material);
        Util.checkGLError();
        scaleBuffer = null;//initializeScaleBuffer(material);
        Util.checkGLError();
        colorBuffer = null;//initializeColorBuffer(material);
        Util.checkGLError();

        ARBVertexArrayObject.glBindVertexArray(0);
    }


    private void initializeQuadVertexBuffer() {
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(4 * 3);
        vertexBuffer.put(UNIT_QUAD_VERTICES);
        vertexBuffer.flip();

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        Util.checkGLError();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, quadVertexVboId);
        Util.checkGLError();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
        Util.checkGLError();
        //ARBInstancedArrays.glVertexAttribDivisorARB(quadVertexVboId, 0);
        //Util.checkGLError();
        GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
        Util.checkGLError();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        Util.checkGLError();
    }

    private FloatBuffer initializePositionBuffer(Material material) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(MAX_INSTANCES_PER_DRAW * 3);
        buffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, positionVboId);
        Util.checkGLError();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        Util.checkGLError();
        material.vertexAttribPointer("position", 3, GL11.GL_FLOAT, false, 0, 0L);
        Util.checkGLError();
        material.enableVertexAttributeArray("position");
        Util.checkGLError();
        //ARBInstancedArrays.glVertexAttribDivisorARB(positionVboId, 1);
        Util.checkGLError();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        Util.checkGLError();

        return buffer;
    }

    private FloatBuffer initializeScaleBuffer(Material material) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(MAX_INSTANCES_PER_DRAW * 3);
        buffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, scaleVboId);
        Util.checkGLError();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        Util.checkGLError();
        material.vertexAttribPointer("scale", 3, GL11.GL_FLOAT, false, 0, 0L);
        Util.checkGLError();
        material.enableVertexAttributeArray("scale");
        Util.checkGLError();
        //ARBInstancedArrays.glVertexAttribDivisorARB(scaleVboId, 1);
        Util.checkGLError();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        Util.checkGLError();

        return buffer;
    }

    private FloatBuffer initializeColorBuffer(Material material) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(MAX_INSTANCES_PER_DRAW * 4);
        buffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVboId);
        Util.checkGLError();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        Util.checkGLError();
        material.vertexAttribPointer("color", 4, GL11.GL_FLOAT, false, 0, 0L);
        Util.checkGLError();
        material.enableVertexAttributeArray("color");
        Util.checkGLError();
        Util.checkGLError();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        Util.checkGLError();

        return buffer;
    }

    private void updateBufferData(int from, int to, ParticlePool particlePool) {
        final int from3 = from * 3;
        final int from4 = from * 4;

        final int count = to - from;
        final int count3 = count * 3;
        final int count4 = count * 4;

        positionBuffer.clear();
        positionBuffer.put(particlePool.position, from3, count3);
        positionBuffer.flip();

        scaleBuffer.clear();
        scaleBuffer.put(particlePool.scale, from3, count3);
        scaleBuffer.flip();

        colorBuffer.clear();
        colorBuffer.put(particlePool.color, from4, count4);
        colorBuffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, positionVboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, positionBuffer, GL15.GL_STATIC_DRAW);
        ARBInstancedArrays.glVertexAttribDivisorARB(positionVboId, 1);
        Util.checkGLError();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, scaleVboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, scaleBuffer, GL15.GL_STATIC_DRAW);
        ARBInstancedArrays.glVertexAttribDivisorARB(scaleVboId, 1);
        Util.checkGLError();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_STATIC_DRAW);
        ARBInstancedArrays.glVertexAttribDivisorARB(colorVboId, 1);
        Util.checkGLError();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }


    @Override
    protected void drawParticles(Material material, ParticleSystemStateData particleSystem, Vector3f camera) {
        ParticlePool particlePool = particleSystem.particlePool;
        ARBVertexArrayObject.glBindVertexArray(vaoId);

        glPushMatrix();
        glTranslatef(-camera.x(), -camera.y(), -camera.z());
        GL11.glEnableClientState(GL_VERTEX_ARRAY);
        GL20.glEnableVertexAttribArray(0);

        for (int from = 0, to = Math.min(particlePool.livingParticles(), MAX_INSTANCES_PER_DRAW);
             from < particlePool.livingParticles();
             from = to, to = Math.min(particlePool.livingParticles(), to + MAX_INSTANCES_PER_DRAW)
        ) {
            //updateBufferData(from, to, particlePool);

            int i3 = from * 3;
            int i4 = from * 4;

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

            ARBDrawInstanced.glDrawArraysInstancedARB(
                    GL11.GL_TRIANGLE_FAN,
                    0,
                    4,
                    to - from
            );
            Util.checkGLError();
        }

        glPopMatrix();
        ARBVertexArrayObject.glBindVertexArray(0);
    }

    @Override
    public void dispose() {
        IntBuffer vboIdBuffer = BufferUtils.createIntBuffer(4)
                .put(quadVertexVboId)
                .put(positionVboId)
                .put(colorVboId)
                .put(scaleVboId);
        vboIdBuffer.flip();

        ARBVertexArrayObject.glBindVertexArray(vaoId);
        GL15.glDeleteBuffers(vboIdBuffer);
        ARBVertexArrayObject.glBindVertexArray(0);
        ARBVertexArrayObject.glDeleteVertexArrays(vaoId);
    }
}
