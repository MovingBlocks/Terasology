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
package org.terasology.persistence.typeHandling.extensionTypes.factories;

import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.extensionTypes.TextureRegionAssetTypeHandler;
import org.terasology.reflection.TypeInfo;
import org.terasology.rendering.assets.texture.TextureRegionAsset;

import java.util.Optional;

public class TextureRegionAssetTypeHandlerFactory implements TypeHandlerFactory {
    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeSerializationLibrary typeSerializationLibrary) {
        if (!TextureRegionAsset.class.equals(typeInfo.getRawType())) {
            return Optional.empty();
        }

        TypeHandler<T> handler = (TypeHandler<T>) new TextureRegionAssetTypeHandler();
        return Optional.of(handler);
    }
}
