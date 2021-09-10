// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.atlas;

import com.google.common.collect.Maps;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.rendering.assets.texture.subtexture.SubtextureData;
import org.terasology.gestalt.naming.Name;

import java.util.Map;
import java.util.Optional;

public class Atlas extends Asset<AtlasData> {
    private Map<ResourceUrn, SubtextureData> subtextures = Maps.newHashMap();

    /**
     * The constructor for an asset. It is suggested that implementing classes provide a constructor taking both the urn,
     * and an initial AssetData to load.
     *
     * @param urn       The urn identifying the asset.
     * @param assetType The asset type this asset belongs to.
     */
    public Atlas(ResourceUrn urn, AssetType<?, AtlasData> assetType, AtlasData data) {
        super(urn, assetType);
        reload(data);
    }

    @Override
    protected void doReload(AtlasData data) {
        subtextures.clear();
        for (Map.Entry<Name, SubtextureData> entry : data.getSubtextures().entrySet()) {
            ResourceUrn subtextureUrn = new ResourceUrn(getUrn().getModuleName(), getUrn().getResourceName(), entry.getKey());
            subtextures.put(subtextureUrn, entry.getValue());
        }
    }

    public Optional<SubtextureData> getSubtexture(ResourceUrn name) {
        return Optional.ofNullable(subtextures.get(name));
    }
}
