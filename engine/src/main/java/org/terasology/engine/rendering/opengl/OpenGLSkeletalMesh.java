// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.GameThread;
import org.terasology.engine.core.subsystem.lwjgl.LwjglGraphicsProcessing;
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.engine.rendering.assets.skeletalmesh.Bone;
import org.terasology.engine.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.engine.rendering.assets.skeletalmesh.SkeletalMeshData;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.AABBf;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;

public class OpenGLSkeletalMesh extends SkeletalMesh {
    private static final int VERTEX_SIZE = 3 * Float.BYTES;
    private static final int NORMAL_SIZE = 3 * Float.BYTES;
    private static final int UV_SIZE = 2 * Float.BYTES;
    private static final int VERTEX_NORMAL_SIZE = (VERTEX_SIZE + NORMAL_SIZE);

    private static final Logger logger = LoggerFactory.getLogger(OpenGLSkeletalMesh.class);

    private SkeletalMeshData data;

    private Vector3f scale;
    private Vector3f translate;

    private DisposalAction disposalAction;

    public OpenGLSkeletalMesh(ResourceUrn urn, AssetType<?, SkeletalMeshData> assetType,
                              SkeletalMeshData data, LwjglGraphicsProcessing graphicsProcessing,
                              OpenGLSkeletalMesh.DisposalAction disposalAction) {
        super(urn, assetType, disposalAction);
        this.disposalAction = disposalAction;
        graphicsProcessing.asynchToDisplayThread(() -> {
            reload(data);
        });
    }

    public static OpenGLSkeletalMesh create(ResourceUrn urn, AssetType<?, SkeletalMeshData> assetType,
                                            SkeletalMeshData data, LwjglGraphicsProcessing graphicsProcessing) {
        return new OpenGLSkeletalMesh(urn, assetType, data, graphicsProcessing, new DisposalAction(urn));
    }


    public void setScaleTranslate(Vector3f newScale, Vector3f newTranslate) {
        this.scale = newScale;
        this.translate = newTranslate;
    }

    @Override
    protected void doReload(SkeletalMeshData newData) {
        try {
            GameThread.synch(() -> {
                this.data = newData;

                if (this.disposalAction.vao == 0) {
                    this.disposalAction.vao = GL30.glGenVertexArrays();
                    this.disposalAction.vbo = GL30.glGenBuffers();
                    this.disposalAction.ebo = GL30.glGenBuffers();
                }
                // bind vertex array and buffer
                GL30.glBindVertexArray(this.disposalAction.vao);
                GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.disposalAction.vbo);

                GL30.glEnableVertexAttribArray(StandardMeshData.VERTEX_INDEX);
                GL30.glVertexAttribPointer(StandardMeshData.VERTEX_INDEX, 3, GL30.GL_FLOAT, false,
                        VERTEX_NORMAL_SIZE, 0);

                GL30.glEnableVertexAttribArray(StandardMeshData.NORMAL_INDEX);
                GL30.glVertexAttribPointer(StandardMeshData.NORMAL_INDEX, 3, GL30.GL_FLOAT, false,
                        VERTEX_NORMAL_SIZE, VERTEX_SIZE);

                GL30.glEnableVertexAttribArray(StandardMeshData.UV0_INDEX);
                GL30.glVertexAttribPointer(StandardMeshData.UV0_INDEX, 2, GL30.GL_FLOAT, false,
                        UV_SIZE, (long) VERTEX_NORMAL_SIZE * newData.getVertexCount());

                int payloadSize = (UV_SIZE + VERTEX_SIZE + NORMAL_SIZE) * newData.getVertexCount();
                ByteBuffer buffer = BufferUtils.createByteBuffer(payloadSize);

                buffer.position(newData.getVertexCount() * VERTEX_NORMAL_SIZE);

                for (Vector2f uv : newData.getUVs()) {
                    buffer.putFloat(uv.x);
                    buffer.putFloat(uv.y);
                }
                buffer.flip();
                GL30.glBufferData(GL30.GL_ARRAY_BUFFER, buffer, GL30.GL_DYNAMIC_DRAW);

                IntBuffer indexBuffer = BufferUtils.createIntBuffer(newData.getIndices().size());
                indexBuffer.put(newData.getIndices().toArray());
                indexBuffer.flip();
                GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, this.disposalAction.ebo);
                GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL30.GL_STATIC_DRAW);

                GL30.glBindVertexArray(0);
            });
        } catch (InterruptedException e) {
            logger.error("Failed to reload {}", getUrn(), e); //NOPMD
        }
    }


    public void doRender(List<Vector3f> verts, List<Vector3f> normals) {
        GL30.glBindVertexArray(disposalAction.vao);

        FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(verts.size() * VERTEX_NORMAL_SIZE);
        for (int i = 0; i < verts.size(); ++i) {
            Vector3f vert = verts.get(i);
            vertBuffer.put(vert.x * scale.x + translate.x);
            vertBuffer.put(vert.y * scale.y + translate.y);
            vertBuffer.put(vert.z * scale.z + translate.z);
            Vector3f norm = normals.get(i);
            vertBuffer.put(norm.x);
            vertBuffer.put(norm.y);
            vertBuffer.put(norm.z);
        }
        vertBuffer.flip();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.disposalAction.vbo);
        GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, vertBuffer);

        GL30.glDrawElements(GL30.GL_TRIANGLES, data.getIndices().size(), GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    public void render() {
        doRender(data.getBindPoseVertexPositions(), data.getBindPoseVertexNormals());
    }

    public void render(List<Matrix4f> boneTransforms) {
        doRender(data.getVertexPositions(boneTransforms), data.getVertexNormals(boneTransforms));
    }

    @Override
    public int getVertexCount() {
        return data.getVertexCount();
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
