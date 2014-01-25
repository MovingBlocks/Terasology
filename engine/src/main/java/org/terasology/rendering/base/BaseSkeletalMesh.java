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
package org.terasology.rendering.base;

import java.util.Collection;

import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshData;

public class BaseSkeletalMesh extends AbstractAsset<SkeletalMeshData> implements SkeletalMesh {

    private SkeletalMeshData data;

    public BaseSkeletalMesh(AssetUri uri, SkeletalMeshData data) {
        super(uri);
        reload(data);
    }

    @Override
    public void reload(SkeletalMeshData data) {
        this.data = data;
    }

    @Override
    public void dispose() {
        data = null;
    }

    @Override
    public boolean isDisposed() {
        return data == null;
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
}
