// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.rendering.opengl;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.EXTPixelBufferObject.GL_PIXEL_PACK_BUFFER_EXT;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;
import static org.lwjgl.opengl.GL15.GL_STREAM_READ;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glMapBuffer;
import static org.lwjgl.opengl.GL15.glUnmapBuffer;

public class PBO {
    private int pboId;
    private ByteBuffer cachedBuffer;

    public PBO(int width, int height) {
        pboId = glGenBuffers();

        int byteSize = width * height * 4;
        cachedBuffer = BufferUtils.createByteBuffer(byteSize);

        bind();
        glBufferData(GL_PIXEL_PACK_BUFFER_EXT, byteSize, GL_STREAM_READ);
        unbind();
    }

    public void bind() {
        glBindBuffer(GL_PIXEL_PACK_BUFFER_EXT, pboId);
    }

    public void unbind() {
        glBindBuffer(GL_PIXEL_PACK_BUFFER_EXT, 0);
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

        cachedBuffer = glMapBuffer(GL_PIXEL_PACK_BUFFER_EXT, GL_READ_ONLY, cachedBuffer);

        // Maybe fix for the issues appearing on some platforms where accessing the "cachedBuffer" causes a JVM exception and therefore a crash...
        ByteBuffer resultBuffer = BufferUtils.createByteBuffer(cachedBuffer.capacity());
        resultBuffer.put(cachedBuffer);
        cachedBuffer.rewind();
        resultBuffer.flip();

        glUnmapBuffer(GL_PIXEL_PACK_BUFFER_EXT);
        unbind();

        return resultBuffer;
    }
}
