/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.assets.atlas;

import com.google.common.collect.Maps;
import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.naming.Name;
import org.terasology.rendering.assets.texture.subtexture.SubtextureData;

import java.util.Map;
import java.util.Optional;

/**
 */
public class Atlas extends Asset<AtlasData> {
    private Map<ResourceUrn, SubtextureData> subtextures = Maps.newHashMap();

    /**
     * The constructor for an asset. It is suggested that implementing classes provide a constructor taking both the urn, and an initial AssetData to load.
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
