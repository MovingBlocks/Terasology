// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf;

import org.terasology.assets.AssetData;

/**
 * Holds a byte array for loading a ByteBufferAsset
 */
public class ByteBufferData implements AssetData {

    private byte[] data;

    public ByteBufferData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
