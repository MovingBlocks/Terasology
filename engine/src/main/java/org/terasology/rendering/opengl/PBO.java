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
package org.terasology.rendering.opengl;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.ARBBufferObject.*;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.EXTPixelBufferObject.GL_PIXEL_PACK_BUFFER_EXT;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;

/**
 * Created by manu on 15.03.2015.
 */
public class PBO {
    private int pboId;
    private ByteBuffer cachedBuffer;

    public PBO(int width, int height) {
        pboId = glGenBuffersARB();

        int byteSize = width * height * 4;
        cachedBuffer = BufferUtils.createByteBuffer(byteSize);

        bind();
        glBufferDataARB(GL_PIXEL_PACK_BUFFER_EXT, byteSize, GL_STREAM_READ_ARB);
        unbind();
    }

    public void bind() {
        glBindBufferARB(GL_PIXEL_PACK_BUFFER_EXT, pboId);
    }

    public void unbind() {
        glBindBufferARB(GL_PIXEL_PACK_BUFFER_EXT, 0);
    }

    public void copyFromFBO(int fboId, int width, int height, int format, int type) {
        bind();
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboId);
        glReadPixels(0, 0, width, height, format, type, 0);
        unbind();
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }

    public ByteBuffer readBackPixels() {
        bind();

        cachedBuffer = glMapBufferARB(GL_PIXEL_PACK_BUFFER_EXT, GL_READ_ONLY, cachedBuffer);

        // Maybe fix for the issues appearing on some platforms where accessing the "cachedBuffer" causes a JVM exception and therefore a crash...
        ByteBuffer resultBuffer = BufferUtils.createByteBuffer(cachedBuffer.capacity());
        resultBuffer.put(cachedBuffer);
        cachedBuffer.rewind();
        resultBuffer.flip();

        glUnmapBufferARB(GL_PIXEL_PACK_BUFFER_EXT);
        unbind();

        return resultBuffer;
    }
}
