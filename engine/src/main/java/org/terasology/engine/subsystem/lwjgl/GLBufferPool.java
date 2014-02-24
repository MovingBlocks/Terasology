/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.engine.subsystem.lwjgl;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.terasology.rendering.VertexBufferObjectUtil;

import java.nio.IntBuffer;

/**
 * @author Immortius
 */
public class GLBufferPool {

    private static final int BUFFER_FETCH_SIZE = 16;

    private int totalPoolSize;

    private TIntList pool = new TIntArrayList();

    public int get() {
        if (pool.isEmpty()) {
            IntBuffer buffer = BufferUtils.createIntBuffer(BUFFER_FETCH_SIZE);
            GL15.glGenBuffers(buffer);
            for (int i = 0; i < BUFFER_FETCH_SIZE; ++i) {
                pool.add(buffer.get(i));
            }
            totalPoolSize += BUFFER_FETCH_SIZE;
        }

        return pool.removeAt(pool.size() - 1);
    }

    public void dispose(int buffer) {
        if (buffer != 0) {
            pool.add(buffer);
            IntBuffer dataBuffer = BufferUtils.createIntBuffer(1);
            dataBuffer.put(0);
            dataBuffer.flip();
            VertexBufferObjectUtil.bufferVboData(buffer, dataBuffer, GL15.GL_STATIC_DRAW);
            dataBuffer.flip();
        }
    }

    public int getActivePoolSize() {
        return totalPoolSize - pool.size();
    }

}
