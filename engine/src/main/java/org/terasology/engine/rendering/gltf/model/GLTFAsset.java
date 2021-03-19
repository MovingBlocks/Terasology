// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

import com.google.common.base.Strings;

import javax.annotation.Nullable;

/**
 * Provides metadata on the GLTF asset. See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#reference-asset for details
 */
public class GLTFAsset {
    private GLTFVersion version;
    private GLTFVersion minVersion;
    private String generator = "";
    private String copyright = "";


    /**
     * @return The version of glTF this asset was generated against
     */
    public GLTFVersion getVersion() {
        return version;
    }

    /**
     * @return Copyright information
     */
    @Nullable
    public String getCopyright() {
        return Strings.nullToEmpty(copyright);
    }

    /**
     * @return Information on how this asset was generated
     */
    @Nullable
    public String getGenerator() {
        return Strings.nullToEmpty(generator);
    }

    /**
     * @return The minimum version of glTF needed to support this asset
     */
    @Nullable
    public GLTFVersion getMinVersion() {
        return minVersion;
    }
}
