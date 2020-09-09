// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.ARBBufferObject.*;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.EXTPixelBufferObject.GL_PIXEL_PACK_BUFFER_EXT;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;

public class PBO {
    private final int pboId;
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
