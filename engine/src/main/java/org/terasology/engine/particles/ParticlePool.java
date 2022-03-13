// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles;

import com.google.common.base.Preconditions;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;


/**
 * Object to keep track of the state of the living particles in a particle system and
 * also maintains a pool of dead particles that can be recycled.
 */
public final class ParticlePool {

    // Static constants
    private static final int X_OFFSET = 0;
    private static final int Y_OFFSET = 1;
    private static final int Z_OFFSET = 2;
    private static final int W_OFFSET = 3;

    public final ParticleData temporaryParticleData = new ParticleData();

    //== package private attributes =====================

    // Per particle scalars
    public final float[] energy;

    // Per particle 3d vectors
    public final float[] previousPosition;
    public final float[] velocity;
    public final float[] position;
    public final float[] scale;
    public final float[] color;
    public final float[] textureOffset;
    private final FloatBuffer positionBuffer;
    private final FloatBuffer scaleBuffer;
    private final FloatBuffer colorBuffer;
    private final FloatBuffer textureOffsetBuffer;

    //== private attributes =============================

    private int firstDeadParticleIndex;
    private final int rawSize;

    private int vao;
    private int positionVbo;
    private int scaleVbo;
    private int colorVbo;
    private int textureOffsetVbo;

    //== Constructors ===================================

    public ParticlePool(final int size) {
        Preconditions.checkArgument(size > 0, "Size must be >0, but was %s", size);

        this.rawSize = size;
        this.firstDeadParticleIndex = 0;

        // Per particle scalars
        this.energy = new float[size];

        // Per particle 3d vectors
        this.position = new float[size * 3];
        this.positionBuffer = BufferUtils.createFloatBuffer(position.length);
        this.scale = new float[size * 3];
        this.scaleBuffer = BufferUtils.createFloatBuffer(scale.length);
        this.color = new float[size * 4];
        this.colorBuffer = BufferUtils.createFloatBuffer(color.length);
        this.textureOffset = new float[size * 2];
        this.textureOffsetBuffer = BufferUtils.createFloatBuffer(textureOffset.length);

        this.previousPosition = new float[size * 3];
        this.velocity = new float[size * 3];
    }

    private ParticlePool() {
        throw new UnsupportedOperationException();
    }

    //== public methods =================================

    public int size() {
        return rawSize;
    }

    public int livingParticles() {
        return firstDeadParticleIndex;
    }

    public int deadParticles() {
        return rawSize - firstDeadParticleIndex;
    }

    public int reviveParticle() {
        resetParticleData(firstDeadParticleIndex);
        firstDeadParticleIndex++;

        return firstDeadParticleIndex - 1;
    }

    public void moveDeceasedParticle(final int index) {
        firstDeadParticleIndex--;
        // First dead particle now points to the last living particle and there is a dead particle in the living pool.
        // Moving the last living particle to the location of the deceased particle fixes both issues.
        loadTemporaryDataFrom(firstDeadParticleIndex, ParticleDataMask.toInt(ParticleDataMask.ALL));
        storeTemporaryDataAt(index, ParticleDataMask.toInt(ParticleDataMask.ALL));
    }

    //== moving particle data ===========================

    public void loadTemporaryDataFrom(final int index, int rawMask) {
        final int index2 = 2 * index;
        final int index3 = 3 * index;
        final int index4 = 4 * index;

        // scalars
        if (ParticleDataMask.ENERGY.isEnabled(rawMask)) {
            temporaryParticleData.energy = energy[index];
        }

        // 2d vectors
        if (ParticleDataMask.TEXTURE_OFFSET.isEnabled(rawMask)) {
            temporaryParticleData.textureOffset.set(
                    textureOffset[index2 + X_OFFSET],
                    textureOffset[index2 + Y_OFFSET]
            );
        }

        // 3d vectors
        if (ParticleDataMask.POSITION.isEnabled(rawMask)) {
            temporaryParticleData.position.set(
                    position[index3 + X_OFFSET],
                    position[index3 + Y_OFFSET],
                    position[index3 + Z_OFFSET]
            );
        }

        if (ParticleDataMask.PREVIOUS_POSITION.isEnabled(rawMask)) {
            temporaryParticleData.previousPosition.set(
                    previousPosition[index3 + X_OFFSET],
                    previousPosition[index3 + Y_OFFSET],
                    previousPosition[index3 + Z_OFFSET]
            );
        }

        if (ParticleDataMask.VELOCITY.isEnabled(rawMask)) {
            temporaryParticleData.velocity.set(
                    velocity[index3 + X_OFFSET],
                    velocity[index3 + Y_OFFSET],
                    velocity[index3 + Z_OFFSET]
            );
        }

        if (ParticleDataMask.SCALE.isEnabled(rawMask)) {
            temporaryParticleData.scale.set(
                    scale[index3 + X_OFFSET],
                    scale[index3 + Y_OFFSET],
                    scale[index3 + Z_OFFSET]
            );
        }

        // 4d vectors
        if (ParticleDataMask.COLOR.isEnabled(rawMask)) {
            temporaryParticleData.color.set(
                    color[index4 + X_OFFSET],
                    color[index4 + Y_OFFSET],
                    color[index4 + Z_OFFSET],
                    color[index4 + W_OFFSET]
            );
        }
    }

    public void storeTemporaryDataAt(final int index, final int rawMask) {
        final int index2 = 2 * index;
        final int index3 = 3 * index;
        final int index4 = 4 * index;

        // scalars
        if (ParticleDataMask.ENERGY.isEnabled(rawMask)) {
            energy[index] = temporaryParticleData.energy;
        }

        // 2d vectors
        if (ParticleDataMask.TEXTURE_OFFSET.isEnabled(rawMask)) {
            textureOffset[index2 + X_OFFSET] = temporaryParticleData.textureOffset.x();
            textureOffset[index2 + Y_OFFSET] = temporaryParticleData.textureOffset.y();
        }

        // 3d vectors
        if (ParticleDataMask.POSITION.isEnabled(rawMask)) {
            position[index3 + X_OFFSET] = temporaryParticleData.position.x();
            position[index3 + Y_OFFSET] = temporaryParticleData.position.y();
            position[index3 + Z_OFFSET] = temporaryParticleData.position.z();
        }

        if (ParticleDataMask.PREVIOUS_POSITION.isEnabled(rawMask)) {
            previousPosition[index3 + X_OFFSET] = temporaryParticleData.previousPosition.x();
            previousPosition[index3 + Y_OFFSET] = temporaryParticleData.previousPosition.y();
            previousPosition[index3 + Z_OFFSET] = temporaryParticleData.previousPosition.z();
        }

        if (ParticleDataMask.VELOCITY.isEnabled(rawMask)) {
            velocity[index3 + X_OFFSET] = temporaryParticleData.velocity.x();
            velocity[index3 + Y_OFFSET] = temporaryParticleData.velocity.y();
            velocity[index3 + Z_OFFSET] = temporaryParticleData.velocity.z();
        }

        if (ParticleDataMask.SCALE.isEnabled(rawMask)) {
            scale[index3 + X_OFFSET] = temporaryParticleData.scale.x();
            scale[index3 + Y_OFFSET] = temporaryParticleData.scale.y();
            scale[index3 + Z_OFFSET] = temporaryParticleData.scale.z();
        }

        // 4d vectors
        if (ParticleDataMask.COLOR.isEnabled(rawMask)) {
            color[index4 + X_OFFSET] = temporaryParticleData.color.x();
            color[index4 + Y_OFFSET] = temporaryParticleData.color.y();
            color[index4 + Z_OFFSET] = temporaryParticleData.color.z();
            color[index4 + W_OFFSET] = temporaryParticleData.color.w();
        }
    }

    /**
     * Initializes this particle pool for rendering.
     * The method should be called right after object creation.
     */
    public void initRendering() {
        vao = GL30.glGenVertexArrays();
        positionVbo = GL15.glGenBuffers();
        scaleVbo = GL15.glGenBuffers();
        colorVbo = GL15.glGenBuffers();
        textureOffsetVbo = GL15.glGenBuffers();

        GL30.glBindVertexArray(vao);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, positionVbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, scaleVbo);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 0, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVbo);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, 0, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textureOffsetVbo);
        GL20.glEnableVertexAttribArray(3);
        GL20.glVertexAttribPointer(3, 2, GL11.GL_FLOAT, false, 0, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    /**
     * Renders the particles in the pool by doing an OpenGL draw call.
     */
    public void draw() {
        GL30.glBindVertexArray(vao);
        GL11.glDrawArrays(GL11.GL_POINTS, 0, livingParticles());
        GL30.glBindVertexArray(0);
    }

    public void prepareRendering() {
        refreshBuffer(positionBuffer, position, 3);
        refreshBuffer(scaleBuffer, scale, 3);
        refreshBuffer(colorBuffer, color, 4);
        refreshBuffer(textureOffsetBuffer, textureOffset, 2);

        bufferData(positionBuffer, positionVbo);
        bufferData(scaleBuffer, scaleVbo);
        bufferData(colorBuffer, colorVbo);
        bufferData(textureOffsetBuffer, textureOffsetVbo);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private void refreshBuffer(FloatBuffer buffer, float[] data, int typeSize) {
        buffer.position(0).limit(data.length);
        buffer.put(data, 0, livingParticles() * typeSize);
        buffer.flip();
    }

    private void bufferData(FloatBuffer data, int vbo) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 0, GL15.GL_STREAM_DRAW);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STREAM_DRAW);
    }

    private void resetParticleData(final int i) {
        final int i2 = i * 2;
        final int i3 = i * 3;
        final int i4 = i * 4;

        // scalars
        energy[i] = 1.0f;

        // 2D vectors
        textureOffset[i2 + X_OFFSET] = 0.0f;

        // 3D vectors

        position[i3 + X_OFFSET] = 0.0f;
        position[i3 + Y_OFFSET] = 0.0f;
        position[i3 + Z_OFFSET] = 0.0f;
        previousPosition[i3 + X_OFFSET] = 0.0f;
        previousPosition[i3 + Y_OFFSET] = 0.0f;
        previousPosition[i3 + Z_OFFSET] = 0.0f;
        velocity[i3 + X_OFFSET] = 0.0f;
        velocity[i3 + Y_OFFSET] = 0.0f;
        velocity[i3 + Z_OFFSET] = 0.0f;
        scale[i3 + X_OFFSET] = 1.0f;
        scale[i3 + Y_OFFSET] = 1.0f;
        scale[i3 + Z_OFFSET] = 1.0f;

        // 4D vectors
        color[i4 + X_OFFSET] = 1.0f;
        color[i4 + Y_OFFSET] = 1.0f;
        color[i4 + Z_OFFSET] = 1.0f;
        color[i4 + W_OFFSET] = 1.0f;
    }
}
