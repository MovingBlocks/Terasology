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
package org.terasology.rendering.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.ImmutableVector2i;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_TEXTURE;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;

/**
 * A OpenGL framebuffer. Generates the fbo and a backing texture.
 *
 */
public class LwjglFrameBufferObject implements FrameBufferObject {
    private int frame;
    private ImmutableVector2i size;
    private IntBuffer vp;

    public LwjglFrameBufferObject(ResourceUrn urn, BaseVector2i size) {
        this.size = ImmutableVector2i.createOrUse(size);

        IntBuffer fboId = BufferUtils.createIntBuffer(1);
        GL30.glGenFramebuffers(fboId);
        frame = fboId.get(0);

        Texture texture = generateTexture(urn);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frame);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture.getId(), 0);

        int result = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        if (result != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Something went wrong with framebuffer! " + result);
        }

        // clear and fill with full alpha
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_COLOR_CLEAR_VALUE, buffer);
        GL11.glClearColor(0f, 0f, 0f, 1f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glClearColor(buffer.get(), buffer.get(), buffer.get(), buffer.get());  // reset

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void dispose() {
        // texture assets are disposed automatically
        GL30.glDeleteFramebuffers(frame);
    }

    private Texture generateTexture(ResourceUrn urn) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(size.x() * size.y() * Integer.BYTES);
        ByteBuffer[] mipmaps = new ByteBuffer[]{buffer};
        TextureData data = new TextureData(size.x(), size.y(), mipmaps, Texture.WrapMode.CLAMP, Texture.FilterMode.NEAREST);
        return Assets.generateAsset(urn, data, Texture.class);
    }

    @Override
    public void unbindFrame() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(vp.get(0), vp.get(1), vp.get(2), vp.get(3));

        glMatrixMode(GL_TEXTURE);
        glLoadIdentity();
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, Display.getWidth(), Display.getHeight(), 0, 0, 2048f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // reset color mask
        GL11.glColorMask(true, true, true, true);
    }

    @Override
    public void bindFrame() {
        vp = BufferUtils.createIntBuffer(16);
        GL11.glGetInteger(GL11.GL_VIEWPORT, vp);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frame);
        GL11.glViewport(0, 0, size.x(), size.y());

        glMatrixMode(GL_TEXTURE);
        glLoadIdentity();
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, size.x(), size.y(), 0, 0, 2048f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // disable writing alpha values so that blending semi-transparent billboards works as expected
        GL11.glColorMask(true, true, true, false);
    }
}
