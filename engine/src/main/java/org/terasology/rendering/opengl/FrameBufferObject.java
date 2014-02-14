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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.math.Rect2i;
import org.terasology.rendering.assets.texture.Texture;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * A OpenGL framebuffer. Generates the fbo and a backing texture.
 *
 * @author synopia
 */
public class FrameBufferObject {
    private int frame;
    private int color;

    public FrameBufferObject(AssetUri uri, final Rect2i region) {
        IntBuffer fboId = BufferUtils.createIntBuffer(1);
        GL30.glGenFramebuffers(fboId);
        frame = fboId.get(0);

        IntBuffer texIds = BufferUtils.createIntBuffer(2);
        GL11.glGenTextures(texIds);
        color = texIds.get(0);

        generateTexture(uri, region);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, color);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, region.width(), region.height(), 0, GL11.GL_RGBA, GL11.GL_INT, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frame);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, color, 0);

        int result = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        if (result != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Something went wrong with framebuffer! " + result);
        }

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        unbindFrame();
    }

    private Texture generateTexture(AssetUri uri, Rect2i region) {
        BaseOpenGLTexture texture = Assets.generateAsset(uri, null, BaseOpenGLTexture.class);
        texture.setId(color);
        texture.setWidth(region.width());
        texture.setHeight(region.height());
        texture.setWrapMode(Texture.WrapMode.CLAMP);
        texture.setFilterMode(Texture.FilterMode.LINEAR);
        return texture;
    }

    public void unbindFrame() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void bindFrame() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frame);
    }
}
