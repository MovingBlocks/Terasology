/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.rendering.gltf;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;

/**
 * A ByteBufferAsset is holds binary data.
 */
public class ByteBufferAsset extends Asset<ByteBufferData> {

    private byte[] bytes;

    /**
     * The constructor for an asset. It is suggested that implementing classes provide a constructor taking both the urn, and an initial AssetData to load.
     *
     * @param urn       The urn identifying the asset.
     * @param assetType The asset type this asset belongs to.
     */
    public ByteBufferAsset(ResourceUrn urn, AssetType<?, ByteBufferData> assetType, ByteBufferData data) {
        super(urn, assetType);
        reload(data);
    }

    @Override
    protected void doReload(ByteBufferData data) {
        bytes = data.getData();
    }

    public byte[] getBytes() {
        return bytes;
    }
}