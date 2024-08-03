// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.extensionTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.Asset;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.engine.utilities.Assets;
import org.terasology.nui.UITextureRegion;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

import java.util.Optional;

// NOTE: This is a copy of TextureRegionTypeHandler that is to be used with UITextureRegions
public class UITextureRegionTypeHandler extends StringRepresentationTypeHandler<UITextureRegion> {
    private static final Logger logger = LoggerFactory.getLogger(UITextureRegionTypeHandler.class);

    @Override
    public String getAsString(UITextureRegion item) {
        if (item instanceof Asset) {
            return ((Asset) item).getUrn().toString();
        }
        return "";
    }

    @Override
    public UITextureRegion getFromString(String representation) {
        Optional<TextureRegionAsset> region = Assets.getTextureRegion(representation);
        if (region.isPresent()) {
            return region.get();
        } else {
            logger.error("Failed to resolve texture region '{}'", representation);
            return Assets.getTextureRegion("engine:default").get();
        }
    }
}
