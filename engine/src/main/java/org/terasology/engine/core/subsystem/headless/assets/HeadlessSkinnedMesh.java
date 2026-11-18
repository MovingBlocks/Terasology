// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.assets;

import com.google.common.collect.Maps;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.rendering.assets.mesh.SkinnedMesh;
import org.terasology.engine.rendering.assets.mesh.SkinnedMeshData;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexByteAttributeBinding;
import org.terasology.engine.rendering.assets.skeletalmesh.Bone;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;

import java.util.List;
import java.util.Map;

public class HeadlessSkinnedMesh extends SkinnedMesh {

    private List<Bone> bones;
    private Map<String, Bone> boneLookup = Maps.newHashMap();
    private VertexAttributeBinding<Vector3fc, Vector3f> positions;
    private VertexByteAttributeBinding boneIndex0;
    private VertexByteAttributeBinding boneIndex1;
    private VertexByteAttributeBinding boneIndex2;
    private VertexByteAttributeBinding boneIndex3;
    private AABBf bounds;

    public HeadlessSkinnedMesh(ResourceUrn urn, AssetType<?, SkinnedMeshData> assetType, SkinnedMeshData skinnedMesh) {
        super(urn, assetType);
        reload(skinnedMesh);
    }

    @Override
    protected void doReload(SkinnedMeshData data) {
        this.boneLookup.clear();
        this.positions = data.positions();
        this.boneIndex0 = data.boneIndex0();
        this.boneIndex1 = data.boneIndex0();
        this.boneIndex2 = data.boneIndex0();
        this.boneIndex3 = data.boneIndex0();
        this.bones.forEach(b -> {
            boneLookup.put(b.getName(), b);
        });
        getBound(bounds);
    }

    @Override
    public AABBfc getAABB() {
        return bounds;
    }

    @Override
    public VertexAttributeBinding<Vector3fc, Vector3f> vertices() {
        return positions;
    }

    @Override
    public int elementCount() {
        return this.positions.elements();
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
    public List<Bone> bones() {
        return bones;
    }

    @Override
    public Bone getBone(String boneName) {
        return boneLookup.get(boneName);
    }

    @Override
    public void render() {

    }

}
