// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.extensionTypes.factories;

import org.terasology.engine.persistence.typeHandling.TypeHandler;
import org.terasology.engine.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.engine.persistence.typeHandling.TypeHandlerContext;
import org.terasology.engine.persistence.typeHandling.extensionTypes.TextureRegionAssetTypeHandler;
import org.terasology.nui.reflection.TypeInfo;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;

import java.util.Optional;

public class TextureRegionAssetTypeHandlerFactory implements TypeHandlerFactory {
    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeHandlerContext context) {
        if (!TextureRegionAsset.class.equals(typeInfo.getRawType())) {
            return Optional.empty();
        }

        TypeHandler<T> handler = (TypeHandler<T>) new TextureRegionAssetTypeHandler();
        return Optional.of(handler);
    }
}
