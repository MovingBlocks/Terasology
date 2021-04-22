// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL41;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.GameThread;
import org.terasology.engine.core.subsystem.lwjgl.LwjglGraphicsProcessing;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.MeshData;
import org.terasology.engine.rendering.assets.mesh.layout.ByteLayout;
import org.terasology.engine.rendering.assets.mesh.layout.DoubleLayout;
import org.terasology.engine.rendering.assets.mesh.layout.FloatLayout;
import org.terasology.engine.rendering.assets.mesh.layout.IntLayout;
import org.terasology.engine.rendering.assets.mesh.layout.Layout;
import org.terasology.engine.rendering.assets.mesh.layout.ShortLayout;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;
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

    public OpenGLMesh(ResourceUrn urn, AssetType<?, MeshData> assetType, MeshData data,
                      DisposalAction disposalAction, LwjglGraphicsProcessing graphicsProcessing) {
        super(urn, assetType);
        this.disposalAction = disposalAction;
        graphicsProcessing.asynchToDisplayThread(() -> {
            reload(data);
        });
    }

    public static OpenGLMesh create(ResourceUrn urn, AssetType<?, MeshData> assetType, MeshData data,
                                    LwjglGraphicsProcessing graphicsProcessing) {
        return new OpenGLMesh(urn, assetType, data, new DisposalAction(urn), graphicsProcessing);
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
            GL30.glDrawElements(data.getMode().glCall, this.indexCount, GL_UNSIGNED_INT, 0);
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
            GL30.glEnableVertexAttribArray(layout.location);
            if ((layout.flag & Layout.FLOATING_POINT) > 0) {
                if (layout instanceof FloatLayout) {
                    GL30.glVertexAttribPointer(layout.location, layout.size, GL30.GL_FLOAT, (layout.flag & Layout.NORMALIZED) > 0, stride, offset);
                } else if (layout instanceof ShortLayout) {
                    GL30.glVertexAttribPointer(layout.location, layout.size, GL30.GL_SHORT, (layout.flag & Layout.NORMALIZED) > 0, stride, offset);
                } else if (layout instanceof IntLayout) {
                    GL30.glVertexAttribPointer(layout.location, layout.size, GL30.GL_INT, (layout.flag & Layout.NORMALIZED) > 0, stride, offset);
                } else if (layout instanceof ByteLayout) {
                    GL30.glVertexAttribPointer(layout.location, layout.size, GL30.GL_BYTE, (layout.flag & Layout.NORMALIZED) > 0, stride, offset);
                } else if (layout instanceof DoubleLayout) {
                    GL41.glVertexAttribLPointer(layout.location, layout.size, GL30.GL_DOUBLE, stride, offset);
                } else {
                    throw new RuntimeException("invalid layout for class: " + layout.getClass());
                }
            } else {
                if (layout instanceof ShortLayout) {
                    GL30.glVertexAttribIPointer(layout.location, layout.size, GL30.GL_SHORT, stride, offset);
                } else if (layout instanceof IntLayout) {
                    GL30.glVertexAttribIPointer(layout.location, layout.size, GL30.GL_INT, stride, offset);
                } else if (layout instanceof ByteLayout) {
                    GL30.glVertexAttribIPointer(layout.location, layout.size, GL30.GL_BYTE, stride, offset);
                } else {
                    throw new RuntimeException("invalid layout for class: " + layout.getClass());
                }
            }
            offset += layout.bytes();
        }

        TIntList indices = newData.getIndices();
        IntBuffer bufferIndices = BufferUtils.createIntBuffer(indices.size());
        bufferIndices.put(indices.toArray());
        bufferIndices.flip();

        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, this.disposalAction.ebo);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, bufferIndices, GL30.GL_STATIC_DRAW);
        indexCount = indices.size();

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        getBound(newData, aabb);
    }

    private static class DisposalAction implements DisposableResource {

        private final ResourceUrn urn;

        private int vao = 0;
        private int vbo = 0;
        private int ebo = 0;

        DisposalAction(ResourceUrn urn) {
            this.urn = urn;
        }

        public void dispose() {
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
        public void close() {
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
