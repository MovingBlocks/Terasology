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

package org.terasology.asset;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.terasology.engine.Uri;
import org.terasology.naming.Name;

/**
 * @author Immortius
 */
public final class AssetUri implements Uri, Comparable<AssetUri> {
    public static final String TYPE_SEPARATOR = ":";

    private AssetType type;

    private Name moduleName = Name.EMPTY;
    private Name assetName;

    public AssetUri() {
    }

    public AssetUri(AssetType type, String moduleName, String assetName) {
        this(type, new Name(moduleName), new Name(assetName));
    }

    public AssetUri(AssetType type, Name moduleName, String assetName) {
        this(type, moduleName, new Name(assetName));
    }

    public AssetUri(AssetType type, Name moduleName, Name assetName) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(moduleName);
        Preconditions.checkNotNull(assetName);
        this.type = type;
        this.moduleName = moduleName;
        this.assetName = assetName;
    }

    public AssetUri(AssetType type, String simpleUri) {
        this.type = type;
        String[] split = simpleUri.split(MODULE_SEPARATOR, 2);
        if (split.length > 1) {
            moduleName = new Name(split[0]);
            assetName = new Name(split[1]);
        }
    }

    public AssetUri(String uri) {
        String[] typeSplit = uri.split(TYPE_SEPARATOR, 2);
        if (typeSplit.length > 1) {
            type = AssetType.getTypeForId(typeSplit[0]);
            String[] packageSplit = typeSplit[1].split(MODULE_SEPARATOR, 2);
            if (packageSplit.length > 1) {
                moduleName = new Name(packageSplit[0]);
                assetName = new Name(packageSplit[1]);
            }
        }
    }

    public AssetType getAssetType() {
        return type;
    }

    @Override
    public Name getModuleName() {
        return moduleName;
    }

    public Name getAssetName() {
        return assetName;
    }

    @Override
    public boolean isValid() {
        return type != null && !moduleName.isEmpty() && !assetName.isEmpty();
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "";
        }
        return type.getTypeId() + TYPE_SEPARATOR + moduleName + MODULE_SEPARATOR + assetName;
    }

    /**
     * @return The asset uri, minus the type
     */
    public String toSimpleString() {
        if (!isValid()) {
            return "";
        }
        return moduleName + MODULE_SEPARATOR + assetName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AssetUri) {
            AssetUri other = (AssetUri) obj;
            return Objects.equal(type, other.type)
                    && Objects.equal(moduleName, other.moduleName)
                    && Objects.equal(assetName, other.assetName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, moduleName, assetName);
    }

    @Override
    public int compareTo(AssetUri o) {
        int result = moduleName.compareTo(o.getModuleName());
        if (result == 0 && assetName != null && o.assetName != null) {
            result = assetName.compareTo(o.getAssetName());
        }
        return result;
    }
}
