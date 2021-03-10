// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering;

import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Provides support for buffering Vertex Buffer Objects.
 *
 */
public final class VertexBufferObjectUtil {

    private VertexBufferObjectUtil() {
    }

    public static void bufferVboData(int id, FloatBuffer buffer, int drawMode) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, drawMode);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public static void bufferVboData(int id, IntBuffer buffer, int drawMode) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, drawMode);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public static void bufferVboElementData(int id, IntBuffer buffer, int drawMode) {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, id);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, drawMode);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public static void bufferVboData(int id, ByteBuffer buffer, int drawMode) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, drawMode);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public static void bufferVboData(int id, ShortBuffer buffer, int drawMode) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, drawMode);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public static void bufferVboElementData(int id, ByteBuffer buffer, int drawMode) {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, id);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, drawMode);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}
