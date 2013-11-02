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
import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.rendering.assets.subtexture.Subtexture;
import org.terasology.rendering.assets.subtexture.SubtextureData;

import java.util.Locale;
import java.util.Map;

/**
 * @author Immortius
 */
public class Atlas extends AbstractAsset<AtlasData> {
    private Map<String, Subtexture> subtextures = Maps.newHashMap();

    public Atlas(AssetUri uri, AtlasData data) {
        super(uri);
        reload(data);
    }

    @Override
    public void reload(AtlasData data) {
        subtextures.clear();
        for (Map.Entry<String, SubtextureData> entry : data.getSubtextures().entrySet()) {
            String subtextureName = getURI().getAssetName() + "." + entry.getKey();
            Subtexture subtexture = Assets.generateAsset(new AssetUri(AssetType.SUBTEXTURE, getURI().getModuleName(), subtextureName), entry.getValue(), Subtexture.class);
            if (subtexture != null) {
                subtextures.put(entry.getKey().toLowerCase(Locale.ENGLISH), subtexture);
            }
        }
    }

    @Override
    public void dispose() {
        subtextures.clear();
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    public Subtexture getSubtexture(String name) {
        return subtextures.get(name.toLowerCase(Locale.ENGLISH));
    }
}
