/*
 * Copyright 2013 Moving Blocks
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
import org.terasology.asset.Asset;
import org.terasology.math.AABB;

public interface Mesh extends Asset<MeshData> {

    int VERTEX_SIZE = 3;
    int TEX_COORD_0_SIZE = 2;
    int TEX_COORD_1_SIZE = 3;
    int COLOR_SIZE = 4;
    int NORMAL_SIZE = 3;

    AABB getAABB();

    TFloatList getVertices();

    // TODO: Remove? At least review.
    void render();
}
