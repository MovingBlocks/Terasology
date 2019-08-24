/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.persistence.typeHandling.extensionTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;
import org.terasology.rendering.assets.texture.TextureRegionAsset;
import org.terasology.utilities.Assets;

import java.util.Optional;

public class TextureRegionAssetTypeHandler extends StringRepresentationTypeHandler<TextureRegionAsset> {
    private static final Logger logger = LoggerFactory.getLogger(TextureRegionTypeHandler.class);

    @Override
    public String getAsString(TextureRegionAsset item) {
        if (item != null) {
            return item.getUrn().toString();
        }
        return "";
    }

    @Override
    public TextureRegionAsset getFromString(String representation) {
        Optional<TextureRegionAsset> region = Assets.getTextureRegion(representation);
        if (region.isPresent()) {
            return region.get();
        } else {
            logger.error("Failed to resolve texture region '" + representation + "'");
            return Assets.getTextureRegion("engine:default").get();
        }
    }

}
