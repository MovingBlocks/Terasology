/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.rendering;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GLContext;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class VBOHelper {

    private static VBOHelper _instance = null;

    public static VBOHelper getInstance() {
        if (_instance == null) {
            _instance = new VBOHelper();
        }

        return _instance;
    }


    public int createVboId() {
        if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
            IntBuffer buffer = BufferUtils.createIntBuffer(1);
            ARBVertexBufferObject.glGenBuffersARB(buffer);
            return buffer.get(0);
        }
        return 0;
    }

    public void bufferVboData(int id, FloatBuffer buffer, int drawMode) {
        if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
            ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, id);
            ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, buffer, drawMode);
            ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
        }
    }

    public void bufferVboElementData(int id, IntBuffer buffer, int drawMode) {
        if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
            ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, id);
            ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, buffer, drawMode);
            ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
        }
    }

}
