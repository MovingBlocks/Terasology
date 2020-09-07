/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.subsystem.headless.assets;

import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.math.AABB;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshData;

import java.util.Collection;
import java.util.Optional;

public class HeadlessSkeletalMesh extends SkeletalMesh {

    private SkeletalMeshData data;

    public HeadlessSkeletalMesh(ResourceUrn urn, AssetType<?, SkeletalMeshData> assetType, SkeletalMeshData data) {
        super(urn, assetType);
        reload(data);
    }

    @Override
    protected void doReload(SkeletalMeshData skeletalMeshData) {
        this.data = skeletalMeshData;
    }

    @Override
    protected Optional<? extends Asset<SkeletalMeshData>> doCreateCopy(ResourceUrn instanceUrn, AssetType<?, SkeletalMeshData> parentAssetType) {
        return Optional.of(new HeadlessSkeletalMesh(instanceUrn, parentAssetType, data));
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
    public AABB getStaticAabb() {
        return data.getStaticAABB();
    }
}
