/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.persistence.typeSerialization.typeHandlers.extension;

import com.google.common.collect.Lists;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetType;
import org.terasology.asset.Assets;
import org.terasology.persistence.typeSerialization.typeHandlers.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius
 */
public class AssetTypeHandler<T extends Asset> implements TypeHandler<T> {
    private Class<T> assetClass;
    private AssetType type;

    public AssetTypeHandler(AssetType type, Class<T> assetClass) {
        this.type = type;
        this.assetClass = assetClass;
    }

    @Override
    public EntityData.Value serialize(T value) {
        if (value != null && value.getURI() != null) {
            return EntityData.Value.newBuilder().addString(value.getURI().toSimpleString()).build();
        }
        return null;
    }

    @Override
    public T deserialize(EntityData.Value value) {
        if (value.getStringCount() > 0) {
            Asset asset = Assets.resolve(type, value.getString(0));
            if (asset != null && assetClass.isAssignableFrom(asset.getClass())) {
                return assetClass.cast(asset);
            }
        }
        return null;
    }

    @Override
    public EntityData.Value serializeCollection(Iterable<T> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (T item : value) {
            result.addString(item.getURI().toSimpleString());
        }
        return result.build();
    }

    @Override
    public List<T> deserializeCollection(EntityData.Value value) {
        List<T> result = Lists.newArrayListWithCapacity(value.getStringCount());
        for (String item : value.getStringList()) {
            Asset asset = Assets.resolve(type, item);
            if (asset != null && assetClass.isAssignableFrom(asset.getClass())) {
                result.add(assetClass.cast(asset));
            }
        }
        return result;
    }
}
