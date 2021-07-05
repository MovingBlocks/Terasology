// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;
import java.util.function.Consumer;


public class PBO {
    private int pboId;
    private ByteBuffer cachedBuffer;

    public PBO(int width, int height) {
        pboId = GL33.glGenBuffers();

        int byteSize = width * height * 4;
        cachedBuffer = BufferUtils.createByteBuffer(byteSize);

        bind();
        GL33.glBufferData(GL30.GL_PIXEL_PACK_BUFFER, byteSize, GL33.GL_STREAM_READ);
        unbind();
    }

    public void bind() {
        GL33.glBindBuffer(GL30.GL_PIXEL_PACK_BUFFER, pboId);
    }

    public void unbind() {
        GL33.glBindBuffer(GL30.GL_PIXEL_PACK_BUFFER, 0);
    }

    public void copyFromFBO(int fboId, int width, int height, int format, int type) {
        bind();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboId);
        GL33.glReadPixels(0, 0, width, height, format, type, 0);
        unbind();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void readBackPixels(Consumer<ByteBuffer> consumer) {
        bind();
        cachedBuffer = GL33.glMapBuffer(GL30.GL_PIXEL_PACK_BUFFER, GL33.GL_READ_ONLY, cachedBuffer);
        consumer.accept(cachedBuffer);
        cachedBuffer.rewind();
        GL33.glUnmapBuffer(GL33.GL_PIXEL_PACK_BUFFER);
        unbind();
    }
}
