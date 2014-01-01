/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.persistence.typeHandling.extensionTypes;

import org.terasology.asset.Asset;
import org.terasology.asset.AssetType;
import org.terasology.asset.Assets;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

/**
 * @author Immortius
 */
public class AssetTypeHandler<T extends Asset> extends StringRepresentationTypeHandler<T> {
    private Class<T> assetClass;
    private AssetType type;

    public AssetTypeHandler(AssetType type, Class<T> assetClass) {
        this.type = type;
        this.assetClass = assetClass;
    }

    @Override
    public String getAsString(T item) {
        return item.getURI().toSimpleString();
    }

    @Override
    public T getFromString(String representation) {
        Asset asset = Assets.resolve(type, representation);
        if (asset != null && assetClass.isAssignableFrom(asset.getClass())) {
            return assetClass.cast(asset);
        }
        return null;
    }
}
