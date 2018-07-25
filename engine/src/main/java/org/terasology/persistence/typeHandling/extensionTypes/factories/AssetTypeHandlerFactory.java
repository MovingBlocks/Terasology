/*
 * Copyright 2018 MovingBlocks
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

import org.terasology.assets.Asset;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.extensionTypes.AssetTypeHandler;
import org.terasology.reflection.TypeInfo;

import java.util.Optional;

public class AssetTypeHandlerFactory implements TypeHandlerFactory {
    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeSerializationLibrary typeSerializationLibrary) {
        Class<? super T> rawType = typeInfo.getRawType();

        if (!Asset.class.isAssignableFrom(rawType)) {
            return Optional.empty();
        }

        @SuppressWarnings({"unchecked"})
        TypeHandler<T> typeHandler = new AssetTypeHandler(rawType);

        return Optional.of(typeHandler);
    }
}
