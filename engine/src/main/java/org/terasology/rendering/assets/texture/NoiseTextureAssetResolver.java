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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetFactory;
import org.terasology.asset.AssetResolver;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.naming.Name;

/**
 * Resolves references to <code>engine:noise</code> texture assets,
 * <br/><br/>
 * The noise parameters are parsed from the name of the asset, then TextureDataFactory is used to create
 * a TextureData object which is used to build the Texture.
 *
 * @author Martin Steiger
 */
public class NoiseTextureAssetResolver implements AssetResolver<Texture, TextureData> {

    private static final Logger logger = LoggerFactory.getLogger(NoiseTextureAssetResolver.class);

    @Override
    public AssetUri resolve(Name partialUri) {
        String[] parts = partialUri.toLowerCase().split("\\.");
        if (parts.length != 6) {
            return null;
        }

        if (!TextureUtil.GENERATED_NOISE_NAME_PREFIX.equals(parts[0])) {
            return null;
        }

        return new AssetUri(AssetType.TEXTURE, TerasologyConstants.ENGINE_MODULE, partialUri);
    }

    @Override
    public Texture resolve(AssetUri uri, AssetFactory<TextureData, Texture> factory) {
        if (!TerasologyConstants.ENGINE_MODULE.equals(uri.getModuleName())) {
            return null;
        }

        String[] parts = uri.getAssetName().toLowerCase().split("\\.");
        if (parts.length != 6) {
            return null;
        }

        if (!TextureUtil.GENERATED_NOISE_NAME_PREFIX.equals(parts[0])) {
            return null;
        }

        try {
            String type = parts[1];
            int size = Integer.parseInt(parts[2]);
            long seed = Long.parseLong(parts[3]);
            int min = Integer.parseInt(parts[4]);
            int max = Integer.parseInt(parts[5]);
            TextureData textureData;
            switch (type) {
                case "white":
                    textureData = TextureDataFactory.createWhiteNoiseTexture(size, seed, min, max);
                    break;
                default:
                    logger.warn("Invalid noise texture type encountered: {}", type);
                    return null;
            }
            return factory.buildAsset(uri, textureData);
        } catch (NumberFormatException e) {
            logger.warn("Could not parse noise texture id", e);
            return null;
        }
    }

}
