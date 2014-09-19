/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.rendering.assets.texture;

import org.terasology.asset.AssetFactory;
import org.terasology.asset.AssetResolver;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.naming.Name;
import org.terasology.rendering.nui.Color;

/**
 * Resolves references to engine:color.RRGGBBAA texture assets,
 * where RR is the red hex value in lowercase,
 * and GG, BB, and AA are green, blue, and alpha, respectively.
 * <p/>
 * The color is parsed from the name of the asset, then TextureDataFactory is used to create
 * a TextureData object which is used to build the Texture.
 *
 * @author mkienenb
 */
public class ColorTextureAssetResolver implements AssetResolver<Texture, TextureData> {

    @Override
    public AssetUri resolve(Name partialUri) {
        String[] parts = partialUri.toLowerCase().split("\\.", 2);
        if (parts.length != 2) {
            return null;
        }

        if (!TextureUtil.GENERATED_COLOR_NAME_PREFIX.equals(parts[0])) {
            return null;
        }

        return new AssetUri(AssetType.TEXTURE, TerasologyConstants.ENGINE_MODULE, partialUri);
    }

    @Override
    public Texture resolve(AssetUri uri, AssetFactory<TextureData, Texture> factory) {
        if (!TerasologyConstants.ENGINE_MODULE.equals(uri.getModuleName())) {
            return null;
        }

        String[] parts = uri.getAssetName().toLowerCase().split("\\.", 2);
        if (parts.length != 2) {
            return null;
        }

        if (!TextureUtil.GENERATED_COLOR_NAME_PREFIX.equals(parts[0])) {
            return null;
        }

        Color color = TextureUtil.getColorForColorName(parts[1]);
        TextureData textureData = TextureDataFactory.newInstance(color);
        return factory.buildAsset(uri, textureData);
    }

}
