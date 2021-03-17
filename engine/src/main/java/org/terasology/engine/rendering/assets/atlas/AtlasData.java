// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.atlas;

import org.terasology.assets.AssetData;
import org.terasology.engine.rendering.assets.texture.subtexture.SubtextureData;
import org.terasology.naming.Name;

import java.util.Map;

/**
 */
public class AtlasData implements AssetData {

    private Map<Name, SubtextureData> subtextures;

    public AtlasData(Map<Name, SubtextureData> subtextureMap) {
        this.subtextures = subtextureMap;
    }

    public Map<Name, SubtextureData> getSubtextures() {
        return subtextures;
    }
}
