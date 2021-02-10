// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.assets.animation;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.joml.geom.AABBf;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;

import java.util.Optional;

/**
 */
public class MeshAnimationImpl extends MeshAnimation {

    private MeshAnimationData data;

    public MeshAnimationImpl(ResourceUrn urn, AssetType<?, MeshAnimationData> assetType, MeshAnimationData data) {
        super(urn, assetType);
        reload(data);
    }

    @Override
    public boolean isValidAnimationFor(SkeletalMesh mesh) {
        for (int i = 0; i < data.getBoneNames().size(); ++i) {
            Bone bone = mesh.getBone(data.getBoneNames().get(i));
            boolean hasParent = data.getBoneParent().get(i) != MeshAnimationData.NO_PARENT;
            if (hasParent && (bone.getParent() == null || !bone.getParent().getName().equals(data.getBoneNames().get(data.getBoneParent().get(i))))) {
                return false;
            } else if (!hasParent && bone.getParent() != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getBoneCount() {
        return data.getBoneNames().size();
    }

    @Override
    public int getFrameCount() {
        return data.getFrames().size();
    }

    @Override
    public MeshAnimationFrame getFrame(int frame) {
        return data.getFrames().get(frame);
    }

    @Override
    public String getBoneName(int index) {
        return data.getBoneNames().get(index);
    }

    @Override
    public float getTimePerFrame() {
        return data.getTimePerFrame();
    }

    @Override
    public AABBf getAabb() {
        return data.getAabb();
    }

    @Override
    protected void doReload(MeshAnimationData newData) {
        this.data = newData;
    }

    @Override
    protected Optional<? extends Asset<MeshAnimationData>> doCreateCopy(ResourceUrn copyUrn, AssetType<?, MeshAnimationData> parentAssetType) {
        return Optional.of(new MeshAnimationImpl(copyUrn, parentAssetType, data));
    }

}
