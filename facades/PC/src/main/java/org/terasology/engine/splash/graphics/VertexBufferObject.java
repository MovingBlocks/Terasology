/*
 * The MIT License (MIT)
 *
 * Copyright © 2014-2015, Heiko Brumme
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.terasology.engine.splash.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

/**
 * This class represents a Vertex Buffer Object (VBO).
 *
 * @author Heiko Brumme
 */
public class VertexBufferObject {

    /**
     * Stores the handle of the VBO.
     */
    private final int id;

    /**
     * Creates a Vertex Buffer Object (VBO).
     */
    public VertexBufferObject() {
        id = glGenBuffers();
    }

    /**
     * Binds this VBO with specified target. The target in the tutorial should be <code>GL_ARRAY_BUFFER</code> most of
     * the time.
     *
     * @param target Target to bind
     */
    public void bind(int target) {
        glBindBuffer(target, id);
    }

    /**
     * Upload vertex data to this VBO with specified target, data and usage. The target in the tutorial should be
     * <code>GL_ARRAY_BUFFER</code> and usage should be <code>GL_STATIC_DRAW</code> most of the time.
     *
     * @param target Target to upload
     * @param data Buffer with the data to upload
     * @param usage Usage of the data
     */
    public void uploadData(int target, FloatBuffer data, int usage) {
        glBufferData(target, data, usage);
    }

    /**
     * Upload null data to this VBO with specified target, size and usage. The target in the tutorial should be
     * <code>GL_ARRAY_BUFFER</code> and usage should be <code>GL_STATIC_DRAW</code> most of the time.
     *
     * @param target Target to upload
     * @param size Size in bytes of the VBO data store
     * @param usage Usage of the data
     */
    public void uploadData(int target, long size, int usage) {
        glBufferData(target, size, usage);
    }

    /**
     * Upload sub data to this VBO with specified target, offset and data. The target in the tutorial should be
     * <code>GL_ARRAY_BUFFER</code> most of the time.
     *
     * @param target Target to upload
     * @param offset Offset where the data should go in bytes
     * @param data Buffer with the data to upload
     */
    public void uploadSubData(int target, long offset, FloatBuffer data) {
        glBufferSubData(target, offset, data);
    }

    /**
     * Upload element data to this EBO with specified target, data and usage. The target in the tutorial should be
     * <code>GL_ELEMENT_ARRAY_BUFFER</code> and usage should be <code>GL_STATIC_DRAW</code> most of the time.
     *
     * @param target Target to upload
     * @param data Buffer with the data to upload
     * @param usage Usage of the data
     */
    public void uploadData(int target, IntBuffer data, int usage) {
        glBufferData(target, data, usage);
    }

    /**
     * Deletes this VBO.
     */
    public void delete() {
        glDeleteBuffers(id);
    }

    /**
     * Getter for the Vertex Buffer Object ID.
     *
     * @return Handle of the VBO
     */
    public int getID() {
        return id;
    }

}
