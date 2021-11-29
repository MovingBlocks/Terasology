// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.assets;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.rendering.assets.mesh.SkinnedMesh;
import org.terasology.engine.rendering.assets.mesh.SkinnedMeshData;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexByteAttributeBinding;
import org.terasology.engine.rendering.assets.skeletalmesh.Bone;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.AABBfc;

import java.util.List;

public class HeadlessSkeletalMesh extends SkinnedMesh {
    public HeadlessSkeletalMesh(ResourceUrn urn, AssetType<?, SkinnedMeshData> assetType, SkinnedMeshData skinnedMesh) {
        super(urn, assetType);

    }


//    private SkeletalMeshData data;
//
//    public HeadlessSkeletalMesh(ResourceUrn urn, AssetType<?, SkeletalMeshData> assetType, SkeletalMeshData data) {
//        super(urn, assetType);
//        reload(data);
//    }
//
//    @Override
//    protected void doReload(SkeletalMeshData skeletalMeshData) {
//        this.data = skeletalMeshData;
//    }
//
//    @Override
//    protected Optional<? extends Asset<SkeletalMeshData>> doCreateCopy(ResourceUrn instanceUrn, AssetType<?, SkeletalMeshData> parentAssetType) {
//        return Optional.of(new HeadlessSkeletalMesh(instanceUrn, parentAssetType, data));
//    }
//
//    @Override
//    public int getVertexCount() {
//        return data.getVertexCount();
//    }
//
//    @Override
//    public Collection<Bone> getBones() {
//        return data.getBones();
//    }

    @Override
    public AABBfc getAABB() {
        return null;
    }

    @Override
    public VertexAttributeBinding<Vector3fc, Vector3f> vertices() {
        return null;
    }

    @Override
    public int elementCount() {
        return 0;
    }

    @Override
    public VertexByteAttributeBinding boneIndex0() {
        return null;
    }

    @Override
    public VertexByteAttributeBinding boneIndex1() {
        return null;
    }

    @Override
    public VertexByteAttributeBinding boneIndex2() {
        return null;
    }

    @Override
    public VertexByteAttributeBinding boneIndex3() {
        return null;
    }

    @Override
    public List<Bone> bones() {
        return null;
    }

    @Override
    public Bone getBone(String boneName) {
//        return data.getBone(boneName);
        return null;
    }

    @Override
    public void render() {

    }

//    @Override
//    public AABBf getStaticAabb() {
//        return data.getStaticAABB();
//    }

    @Override
    protected void doReload(SkinnedMeshData data) {

    }
}
