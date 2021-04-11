// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.core.GameThread;
import org.terasology.engine.core.subsystem.lwjgl.GLBufferPool;
import org.terasology.engine.core.subsystem.lwjgl.LwjglGraphicsProcessing;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.MeshData;
import org.terasology.engine.rendering.assets.mesh.layout.FloatLayout;
import org.terasology.engine.rendering.assets.mesh.layout.Layout;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;

/**
 */
public class OpenGLMesh extends Mesh {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLMesh.class);
    private AABBf aabb = new AABBf();
    private MeshData data;
    private int indexCount;
    private DisposalAction disposalAction;

    public OpenGLMesh(ResourceUrn urn, AssetType<?, MeshData> assetType, GLBufferPool bufferPool, MeshData data, LwjglGraphicsProcessing graphicsProcessing) {
        super(urn, assetType);
        this.disposalAction = new DisposalAction(urn, bufferPool);
        graphicsProcessing.asynchToDisplayThread(() -> {
            reload(data);
        });
    }

    @Override
    protected void doReload(MeshData newData) {
        try {
            GameThread.synch(() -> buildMesh(newData));
        } catch (InterruptedException e) {
            logger.error("Failed to reload {}", getUrn(), e);
        }
    }

    @Override
    public AABBfc getAABB() {
        return aabb;
    }

    @Override
    public TFloatList getVertices() {
        return data.getVertices();
    }

    @Override
    public void render() {
        if (!isDisposed()) {
            GL30.glBindVertexArray(disposalAction.vao);
            GL30.glDrawElements(GL30.GL_TRIANGLES, this.indexCount,  GL_UNSIGNED_INT, 0);
            GL30.glBindVertexArray(0);
        } else {
            logger.error("Attempted to render disposed mesh: {}", getUrn());
        }
    }


    private void buildMesh(MeshData newData) {
        this.data = newData;

        this.disposalAction.dispose();
        this.disposalAction.vao = GL30.glGenVertexArrays();
        this.disposalAction.vbo = GL30.glGenBuffers();
        this.disposalAction.ebo = GL30.glGenBuffers();

        List<Layout> layouts = newData.getLayouts();
        List<Layout> targets = new ArrayList<>();

        int stride = 0;
        for (Layout layout : layouts) {
            if (layout.hasContent()) {
                stride += layout.bytes();
                targets.add(layout);
            }
        }

        ByteBuffer buffer = BufferUtils.createByteBuffer(stride * newData.getSize());
        for (int x = 0; x < newData.getSize(); x++) {
            for (Layout layout : targets) {
                layout.write(x, buffer);
            }
        }
        buffer.flip();

        // bind vertex array and buffer
        GL30.glBindVertexArray(this.disposalAction.vao);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.disposalAction.vbo);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, buffer, GL30.GL_STATIC_DRAW);

        int offset = 0;
        for (Layout layout : targets) {
            GL20.glEnableVertexAttribArray(layout.location);
            if (layout instanceof FloatLayout) {
                GL20.glVertexAttribPointer(layout.location, layout.size, GL11.GL_FLOAT, false, stride, offset);
            }
//            else if (layout instanceof IntBuffer) {
//                GL20.glVertexAttribPointer(layout.location, layout.size, GL11.GL_INT, false, stride, offset);
//            }
            offset += layout.bytes();
        }

        TIntList indices = newData.getIndices();
        IntBuffer bufferIndices = BufferUtils.createIntBuffer(indices.size());
        for (int x = 0; x < indices.size(); x++) {
            bufferIndices.put(indices.get(x));
        }
        bufferIndices.flip();

        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, this.disposalAction.ebo);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, bufferIndices, GL30.GL_STATIC_DRAW);
        indexCount = indices.size();

        GL30.glBindVertexArray(0);
        getBound(newData, aabb);
    }

    private static class DisposalAction implements Runnable {

        private final ResourceUrn urn;
        private final GLBufferPool bufferPool;

        private int vao = 0;
        private int vbo = 0;
        private int ebo = 0;

        DisposalAction(ResourceUrn urn, GLBufferPool bufferPool) {
            this.urn = urn;
            this.bufferPool = bufferPool;
        }

        public void  dispose() {
            if (vao != 0) {
                GL30.glDeleteVertexArrays(vao);
            }
            if (vbo != 0) {
                GL30.glDeleteBuffers(vbo);
            }
            if (ebo != 0) {
                GL30.glDeleteBuffers(ebo);
            }
            vao = 0;
            vbo = 0;
            ebo = 0;
        }

        @Override
        public void run() {
            try {
                GameThread.synch(() -> {
                    dispose();
                });
            } catch (InterruptedException e) {
                logger.error("Failed to dispose {}", urn, e);
            }
        }
    }
}
