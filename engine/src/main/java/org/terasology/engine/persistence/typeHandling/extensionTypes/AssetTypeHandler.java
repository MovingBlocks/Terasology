// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.extensionTypes;

import com.google.common.base.Strings;
import org.terasology.gestalt.assets.Asset;
import org.terasology.engine.utilities.Assets;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

import java.util.Optional;

public class AssetTypeHandler<T extends Asset> extends StringRepresentationTypeHandler<T> {
    private Class<T> assetClass;

    public AssetTypeHandler(Class<T> assetClass) {
        this.assetClass = assetClass;
    }

    @Override
    public String getAsString(T item) {
        if (item == null) {
            return "";
        }
        return item.getUrn().toString();
    }

    @Override
    public T getFromString(String representation) {
        if (Strings.isNullOrEmpty(representation)) {
            return null;
        }
        Optional<T> asset = Assets.get(representation, assetClass);
        if (asset.isPresent()) {
            return assetClass.cast(asset.get());
        }
        return null;
    }
}
