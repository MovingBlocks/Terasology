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
import org.terasology.engine.rendering.assets.mesh.SkinnedMesh;
import org.terasology.engine.rendering.assets.mesh.SkinnedMeshData;
import org.terasology.engine.rendering.assets.mesh.resource.AllocationType;
import org.terasology.engine.rendering.assets.mesh.resource.DrawingMode;
import org.terasology.engine.rendering.assets.mesh.resource.IndexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexByteAttributeBinding;
import org.terasology.engine.rendering.assets.skeletalmesh.Bone;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;

public class OpenGLSkinnedMesh extends SkinnedMesh implements OpenGLMeshBase {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLSkinnedMesh.class);
    private OpenGLSkinnedMesh.DisposalAction disposalAction;
    private DrawingMode drawMode;
    private AllocationType allocationType;
    private VBOContext state = null;
    private AABBf aabb = new AABBf();

    private VertexAttributeBinding<Vector3fc, Vector3f> positions;
    private VertexByteAttributeBinding boneIndex0;
    private VertexByteAttributeBinding boneIndex1;
    private VertexByteAttributeBinding boneIndex2;
    private VertexByteAttributeBinding boneIndex3;

    private List<Bone> bones;

    private int indexCount;

    protected OpenGLSkinnedMesh(ResourceUrn urn, AssetType<?, SkinnedMeshData> assetType, SkinnedMeshData data,
                                OpenGLSkinnedMesh.DisposalAction disposalAction, LwjglGraphicsProcessing graphicsProcessing) {
        super(urn, assetType, disposalAction);
        this.disposalAction = disposalAction;
        graphicsProcessing.asynchToDisplayThread(() -> {
            reload(data);
        });
    }

    public static OpenGLSkinnedMesh create(ResourceUrn urn, AssetType<?, SkinnedMeshData> assetType, SkinnedMeshData data,
                                    LwjglGraphicsProcessing graphicsProcessing) {
        return new OpenGLSkinnedMesh(urn, assetType, data, new OpenGLSkinnedMesh.DisposalAction(urn), graphicsProcessing);
    }

    @Override
    protected void doReload(SkinnedMeshData newData) {
        try {
            GameThread.synch(() -> buildMesh(newData));
        } catch (InterruptedException e) {
            logger.error("Failed to reload {}", getUrn(), e);
        }
    }

    @Override
    public VertexByteAttributeBinding boneIndex0() {
        return boneIndex0;
    }

    @Override
    public VertexByteAttributeBinding boneIndex1() {
        return boneIndex1;
    }

    @Override
    public VertexByteAttributeBinding boneIndex2() {
        return boneIndex2;
    }

    @Override
    public VertexByteAttributeBinding boneIndex3() {
        return boneIndex3;
    }

    @Override
    public AABBfc getAABB() {
        return aabb;
    }

    @Override
    public VertexAttributeBinding<Vector3fc, Vector3f> vertices() {
        return positions;
    }

    @Override
    public int elementCount() {
        return positions.elements();
    }

    @Override
    public List<Bone> bones() {
        return bones;
    }

    @Override
    public void render() {
        if (!isDisposed()) {
            updateState(state);
            GL30.glBindVertexArray(disposalAction.vao);
            if (this.indexCount == 0) {
                GL30.glDrawArrays(drawMode.glCall, 0, positions.elements());
            } else {
                GL30.glDrawElements(drawMode.glCall, this.indexCount, GL_UNSIGNED_INT, 0);
            }
            GL30.glBindVertexArray(0);
        } else {
            logger.error("Attempted to render disposed mesh: {}", getUrn());
        }
    }

    private void buildMesh(SkinnedMeshData newData) {
        if (this.disposalAction.vao == 0) {
            this.disposalAction.vao = GL30.glGenVertexArrays();
            this.disposalAction.vbo = GL30.glGenBuffers();
            this.disposalAction.ebo = GL30.glGenBuffers();
        }
        allocationType = newData.allocationType();
        drawMode = newData.getMode();

        GL30.glBindVertexArray(this.disposalAction.vao);
        this.positions = newData.positions();
        this.boneIndex0 = newData.boneIndex0();
        this.boneIndex1 = newData.boneIndex0();
        this.boneIndex2 = newData.boneIndex0();
        this.boneIndex3 = newData.boneIndex0();
        this.bones = newData.bones();
        this.state = buildVBO(this.disposalAction.vbo, allocationType, newData.vertexResources());

        IndexResource indexResource = newData.indexResource();
        this.indexCount = indexResource.indices();
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, this.disposalAction.ebo);
        indexResource.writeBuffer((buffer) -> GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, buffer, GL30.GL_STATIC_DRAW));

        GL30.glBindVertexArray(0);
        getBound(aabb);
    }

    private static class DisposalAction implements DisposableResource {
        private final ResourceUrn urn;

        private int vao = 0;
        private int vbo = 0;
        private int ebo = 0;

        private DisposalAction(ResourceUrn urn) {
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
