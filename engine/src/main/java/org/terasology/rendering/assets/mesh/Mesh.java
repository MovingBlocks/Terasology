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
package org.terasology.rendering.assets.mesh;

import gnu.trove.list.TFloatList;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.math.AABB;

public abstract class Mesh extends Asset<MeshData> {

    public static final int VERTEX_SIZE = 3;
    public static final int TEX_COORD_0_SIZE = 2;
    public static final int TEX_COORD_1_SIZE = 3;
    public static final int COLOR_SIZE = 4;
    public static final int NORMAL_SIZE = 3;

    protected Mesh(ResourceUrn urn, AssetType<?, MeshData> assetType) {
        super(urn, assetType);
    }

    public abstract AABB getAABB();

    public abstract TFloatList getVertices();

    // TODO: Remove? At least review.
    public abstract void render();
}
