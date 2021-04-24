// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.core.GameThread;
import org.terasology.engine.core.subsystem.lwjgl.LwjglGraphicsProcessing;
import org.terasology.engine.rendering.assets.mesh.resouce.IndexResource;
import org.terasology.engine.rendering.assets.mesh.resouce.VertexResource;
import org.terasology.engine.rendering.assets.skeletalmesh.Bone;
import org.terasology.engine.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.engine.rendering.assets.skeletalmesh.SkeletalMeshData;
import org.terasology.joml.geom.AABBf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;

/**
 */
public class OpenGLSkeletalMesh extends SkeletalMesh implements OpenGLMeshBase {
    private static final int VERTEX_SIZE = 3 * Float.BYTES;
    private static final int NORMAL_SIZE = 3 * Float.BYTES;
    private static final int UV_SIZE = 2 * Float.BYTES;
    private static final int VERTEX_NORMAL_SIZE = (VERTEX_SIZE + NORMAL_SIZE);

    private static final Logger logger = LoggerFactory.getLogger(OpenGLSkeletalMesh.class);

    private SkeletalMeshData data;
    private int indexCount;

    private Vector3f scale;
    private Vector3f translate;

    private DisposalAction disposalAction;
    private VBOContext state = null;

    public OpenGLSkeletalMesh(ResourceUrn urn, AssetType<?, SkeletalMeshData> assetType,
                              SkeletalMeshData data, LwjglGraphicsProcessing graphicsProcessing) {
        super(urn, assetType);
        disposalAction = new DisposalAction(urn);
        getDisposalHook().setDisposeAction(disposalAction);
        graphicsProcessing.asynchToDisplayThread(() -> {
            reload(data);
        });
    }

    public void setScaleTranslate(org.joml.Vector3f newScale, org.joml.Vector3f newTranslate) {
        this.scale = newScale;
        this.translate = newTranslate;
    }

    @Override
    protected void doReload(SkeletalMeshData newData) {
        try {
            GameThread.synch(() -> {
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
                this.state = buildVBO(this.disposalAction.vbo, GL30.GL_DYNAMIC_DRAW, targets);

                IndexResource indexResource = newData.indexResource();
                ByteBuffer indexBuffer = indexResource.buffer;

                this.indexCount = indexResource.num;
                indexBuffer.rewind();
                GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, this.disposalAction.ebo);
                GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL30.GL_STATIC_DRAW);

                GL30.glBindVertexArray(0);
            });
        } catch (InterruptedException e) {
            logger.error("Failed to reload {}", getUrn(), e);
        }
    }


    public void render() {
        GL30.glBindVertexArray(disposalAction.vao);
        data.applyBind();
        updateState(state);
        GL30.glDrawElements(GL30.GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    public void render(List<Matrix4f> boneTransforms) {
        GL30.glBindVertexArray(disposalAction.vao);
        data.apply(boneTransforms);
        updateState(state);
        GL30.glDrawElements(GL30.GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    @Override
    public Collection<Bone> getBones() {
        return data.getBones();
    }

    @Override
    public Bone getBone(String boneName) {
        return data.getBone(boneName);
    }

    @Override
    public AABBf getStaticAabb() {
        return data.getStaticAABB();
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
