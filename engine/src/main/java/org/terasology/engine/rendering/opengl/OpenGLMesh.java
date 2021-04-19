// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.core.GameThread;
import org.terasology.engine.core.subsystem.lwjgl.LwjglGraphicsProcessing;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.MeshData;
import org.terasology.engine.rendering.assets.mesh.resouce.VertexResource;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;

import java.nio.ByteBuffer;
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

    public OpenGLMesh(ResourceUrn urn, AssetType<?, MeshData> assetType, MeshData data, LwjglGraphicsProcessing graphicsProcessing) {
        super(urn, assetType);
        this.disposalAction = new DisposalAction(urn);
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
    public Float[] getVertices() {
        return data.getVertices();
    }

    @Override
    public int getVertexCount() {
        return data.vertexCount();
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

        GL30.glBindVertexArray(this.disposalAction.vao);

        VertexResource[] resources = newData.getVertexResource();
        List<VertexResource> targets = new ArrayList<>();
        int bufferSize = 0;
        for(int x = 0; x < resources.length; x++) {
            if(resources[x].getVersion() >  0) {
                targets.add(resources[x]);
                bufferSize += resources[x].inSize;
            }
        }
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.disposalAction.vbo);
        GL30.glBufferData(this.disposalAction.vbo, bufferSize, GL30.GL_STATIC_DRAW);
        int offset = 0;
        for (VertexResource resource : targets) {
            resource.buffer.rewind();
            GL30.glBufferSubData(this.disposalAction.vbo, offset, resource.buffer);
            for (VertexResource.VertexDefinition attribute : resource.attributes) {
                GL30.glEnableVertexAttribArray(attribute.location);
                GL30.glVertexAttribPointer(attribute.location, attribute.attribute.count,
                        attribute.attribute.mapping.glType, false, resource.inStride, offset + resource.inStride);
            }
            offset += resource.inSize;
        }

        ByteBuffer indexBuffer = newData.getIndexResource().buffer;
        indexBuffer.rewind();
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, this.disposalAction.ebo);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL30.GL_STATIC_DRAW);
        this.indexCount = newData.getIndexResource().num;

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        getBound(newData, aabb);
    }

    private static class DisposalAction implements Runnable {

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
