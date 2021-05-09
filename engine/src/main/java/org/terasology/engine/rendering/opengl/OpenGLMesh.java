// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.GameThread;
import org.terasology.engine.core.subsystem.lwjgl.LwjglGraphicsProcessing;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.MeshData;
import org.terasology.engine.rendering.assets.mesh.resource.IndexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResource;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;

/**
 */
public class OpenGLMesh extends Mesh implements OpenGLMeshBase{
    private static final Logger logger = LoggerFactory.getLogger(OpenGLMesh.class);
    private AABBf aabb = new AABBf();
    private MeshData data;
    private int indexCount;
    private DisposalAction disposalAction;

    private VBOContext state = null;

    public OpenGLMesh(ResourceUrn urn, AssetType<?, MeshData> assetType, MeshData data,
                      DisposalAction disposalAction, LwjglGraphicsProcessing graphicsProcessing) {
        super(urn, assetType, disposalAction);
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
    public VertexAttributeBinding<Vector3fc, Vector3f> getVertices() {
        return data.positions();
    }

    @Override
    public int getVertexCount() {
        return data.positions().numberOfElements();
    }


    @Override
    public void render() {
        if (!isDisposed()) {
            updateState(state);
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

        VertexResource[] resources = newData.vertexResources();
        List<VertexResource> targets = new ArrayList<>();
        for (VertexResource vertexResource : resources) {
            if (vertexResource.getVersion() > 0) {
                targets.add(vertexResource);
            }
        }

        this.state = buildVBO(this.disposalAction.vbo, GL30.GL_STATIC_DRAW, targets);

        IndexResource indexResource = newData.indexResource();
        this.indexCount = indexResource.getNumberOfIndices();
        ByteBuffer indexBuffer = indexResource.buffer;
        indexBuffer.rewind();
        indexBuffer.limit(indexResource.getSize());
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, this.disposalAction.ebo);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL30.GL_STATIC_DRAW);

        GL30.glBindVertexArray(0);
        getBound(aabb);
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
