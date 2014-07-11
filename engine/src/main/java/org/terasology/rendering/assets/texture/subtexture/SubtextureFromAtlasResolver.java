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
package org.terasology.rendering.assets.texture.subtexture;

import org.terasology.asset.AssetFactory;
import org.terasology.asset.AssetResolver;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.naming.Name;
import org.terasology.rendering.assets.atlas.Atlas;

/**
 * @author Immortius
 */
public class SubtextureFromAtlasResolver implements AssetResolver<Subtexture, SubtextureData> {

    @Override
    public AssetUri resolve(Name partialUri) {
        String[] parts = partialUri.toLowerCase().split("\\.", 2);
        if (parts.length == 2) {
            AssetUri uri = Assets.resolveAssetUri(AssetType.ATLAS, parts[0]);
            if (uri != null) {
                return new AssetUri(AssetType.SUBTEXTURE, uri.getModuleName(), partialUri);
            }
        }
        return null;
    }

    @Override
    public Subtexture resolve(AssetUri uri, AssetFactory<SubtextureData, Subtexture> factory) {
        String[] parts = uri.getAssetName().toLowerCase().split("\\.", 2);
        if (parts.length == 2) {
            Atlas atlas = Assets.get(new AssetUri(AssetType.ATLAS, uri.getModuleName(), parts[0]), Atlas.class);
            if (atlas == null) {
                return null;
            }
            return atlas.getSubtexture(parts[1]);
        }
        return null;
    }
}
