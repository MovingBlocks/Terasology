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

import gnu.trove.list.TFloatList;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.math.AABB;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.mesh.MeshData;

public class HeadlessMesh extends Mesh {

    protected MeshData data;
    protected AABB aabb;

    public HeadlessMesh(ResourceUrn urn, AssetType<?, MeshData> assetType, MeshData meshData) {
        super(urn, assetType);
        reload(meshData);
    }

    @Override
    protected void doReload(MeshData meshData) {
        this.data = meshData;
        this.aabb = AABB.createEncompasing(meshData.getVertices());
    }

    @Override
    public AABB getAABB() {
        return aabb;
    }

    @Override
    public TFloatList getVertices() {
        return data.getVertices();
    }

    @Override
    public void render() {
        // do nothing
    }
}
