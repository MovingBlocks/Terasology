// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf;

import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;

/**
 * A ByteBufferAsset is holds binary data.
 */
public class ByteBufferAsset extends Asset<ByteBufferData> {

    private byte[] bytes;

    /**
     * The constructor for an asset. It is suggested that implementing classes provide a constructor taking both the urn,
     * and an initial AssetData to load.
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
