/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.rendering.assets.skeletalmesh;

import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.math.AABB;

import java.util.Collection;

/**
 */
public abstract class SkeletalMesh extends Asset<SkeletalMeshData> {

    protected SkeletalMesh(ResourceUrn urn, AssetType<?, SkeletalMeshData> assetType) {
        super(urn, assetType);
    }

    public abstract int getVertexCount();

    public abstract Collection<Bone> getBones();

    public abstract Bone getBone(String boneName);

    /**
     *
     * @return the boundings of the mesh when it its not being animated.
     */
    public abstract AABB getStaticAabb();
}
