// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.extensionTypes;

import com.google.common.base.Strings;
import org.terasology.engine.utilities.Assets;
import org.terasology.nui.skin.UISkin;
import org.terasology.nui.skin.UISkinAsset;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

import java.util.Optional;

public class UISkinTypeHandler extends StringRepresentationTypeHandler<UISkin> {
    @Override
    public String getAsString(UISkin item) {
        if (item == null) {
            return "";
        }
        // TODO
        return item.toString();
    }

    @Override
    public UISkin getFromString(String representation) {
        if (Strings.isNullOrEmpty(representation)) {
            return null;
        }
        Optional<UISkinAsset> asset = Assets.get(representation, UISkinAsset.class);
        if (asset.isPresent()) {
            return asset.get().getSkin();
        }
        return null;
    }
}
