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
package org.terasology.rendering.headless;

import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.mesh.MeshData;
import org.terasology.rendering.base.BaseMesh;

public class HeadlessMesh extends BaseMesh {

    public HeadlessMesh(AssetUri uri, MeshData data) {
        super(uri, data);
    }

    @Override
    public void render() {
        // do nothing
    }
}
