// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureData;
import org.terasology.engine.rendering.nui.internal.LwjglCanvasRenderer;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.ResourceUrn;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * A OpenGL framebuffer. Generates the fbo and a backing texture.
 */
public class LwjglFrameBufferObject implements FrameBufferObject {
    private DisplayDevice displayDevice;
    private int frame;
    private Vector2ic size;
    private IntBuffer vp;
    private LwjglCanvasRenderer canvasRenderer;

    private final Matrix4fStack transforms;
    private final Matrix4f projectionMatrix;
    private final Matrix4f lastProjectionMatrix = new Matrix4f();

    public LwjglFrameBufferObject(Matrix4fStack transforms, Matrix4f projectionMatrix, DisplayDevice displayDevice, ResourceUrn urn, Vector2ic size) {
        this.displayDevice = displayDevice;
        this.size = new Vector2i(size);

        this.projectionMatrix = projectionMatrix;
        this.transforms = transforms;

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

        projectionMatrix.set(lastProjectionMatrix);
        transforms.popMatrix();
    }

    @Override
    public void bindFrame() {
        lastProjectionMatrix.set(projectionMatrix);

        vp = BufferUtils.createIntBuffer(16);
        GL30.glGetIntegerv(GL30.GL_VIEWPORT, vp);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frame);
        GL30.glViewport(0, 0, size.x(), size.y());

        projectionMatrix.setOrtho(0, size.x(), size.y(), 0, 0, 2048f);
        transforms.pushMatrix();
        transforms.identity();
    }
}
