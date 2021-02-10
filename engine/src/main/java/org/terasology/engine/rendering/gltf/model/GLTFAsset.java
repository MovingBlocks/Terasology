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
package org.terasology.rendering.gltf.model;

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
