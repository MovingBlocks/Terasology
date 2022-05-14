// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.bgfx;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.bgfx.BGFX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.MeshData;
import org.terasology.engine.rendering.assets.mesh.resource.AllocationType;
import org.terasology.engine.rendering.assets.mesh.resource.DrawingMode;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;

import java.util.Optional;

public class BGFXMesh extends Mesh implements BGFXMeshBase {
    private static final Logger logger = LoggerFactory.getLogger(BGFXMesh.class);

    private AABBf aabb = new AABBf();

    private int indexCount;
    private final DisposableAction disposalAction;

    private DrawingMode drawMode;

    private AllocationType allocationType;

    private VertexAttributeBinding<Vector3fc, Vector3f> positions;


    protected BGFXMesh(ResourceUrn urn, AssetType<?, MeshData> assetType, DisposableAction disposableAction) {
        super(urn, assetType, disposableAction);
        this.disposalAction = disposableAction;
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
    public void render() {
        disposalAction.resource.ifPresent((data) -> {
            for (int i = 0; i < data.vertex.length; i++) {
                BGFX.bgfx_set_vertex_buffer(i, data.vertex[i].bufferId, 0, 0);
            }
        });
    }


    @Override
    protected void doReload(MeshData data) {
        positions = data.positions();
        disposalAction.setVertexResource(buildBGFXResource(data.vertexResources()));
    }

    private static class DisposableAction implements DisposableResource {
        public Optional<BGFXResource> resource = Optional.empty();

        DisposableAction() {

        }

        public void setVertexResource(BGFXResource data) {
            resource.ifPresent(BGFXResource::free);
            resource = Optional.of(data);
        }

        @Override
        public void close() {
            resource.ifPresent(BGFXResource::free);
            resource = Optional.empty();
        }
    }
}
