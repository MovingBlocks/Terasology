// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.extensionTypes;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.rendering.nui.skin.UISkinWithUrn;
import org.terasology.engine.utilities.Assets;
import org.terasology.nui.skin.UISkin;
import org.terasology.nui.skin.UISkinAsset;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

import java.util.Optional;

public class UISkinTypeHandler extends StringRepresentationTypeHandler<UISkin> {
    private static final Logger logger = LoggerFactory.getLogger(UISkinTypeHandler.class);

    @Override
    public String getAsString(UISkin item) {
        if (item == null) {
            return "";
        } else if (item instanceof UISkinWithUrn) {
            return ((UISkinWithUrn) item).getUrn().toString();
        } else {
            // Can't associate UISkin with urn.
            logger.warn("Couldn't associate UISkin with urn. Unable to serialize.");
            return "";
        }
    }

    @Override
    public UISkin getFromString(String representation) {
        if (Strings.isNullOrEmpty(representation)) {
            return null;
        }
        Optional<UISkinAsset> asset = Assets.get(representation, UISkinAsset.class);
        if (asset.isPresent()) {
            return UISkinWithUrn.createFromSkin(asset.get().getSkin(), asset.get().getUrn());
        }
        return null;
    }
}
