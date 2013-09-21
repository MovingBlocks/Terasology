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
import org.terasology.engine.AbstractBaseUri;
import org.terasology.engine.module.UriUtil;

/**
 * @author Immortius
 */
public final class AssetUri extends AbstractBaseUri {
    public static final String TYPE_SEPARATOR = ":";

    private AssetType type;
    private String fullUri;

    private String moduleName = "";
    private String assetName = "";
    private String normalisedModuleName = "";
    private String normalisedAssetName = "";

    public AssetUri() {
    }

    public AssetUri(AssetType type, String moduleName, String assetName) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(moduleName);
        Preconditions.checkNotNull(assetName);
        this.type = type;
        this.moduleName = moduleName;
        this.assetName = assetName;
        this.normalisedModuleName = UriUtil.normalise(moduleName);
        this.normalisedAssetName = UriUtil.normalise(assetName);
    }

    public AssetUri(AssetType type, String simpleUri) {
        this.type = type;
        String[] split = simpleUri.split(MODULE_SEPARATOR, 2);
        if (split.length > 1) {
            moduleName = split[0];
            assetName = split[1];
            normalisedModuleName = UriUtil.normalise(split[0]);
            normalisedAssetName = UriUtil.normalise(split[1]);
        }
    }

    public AssetUri(String uri) {
        String[] typeSplit = uri.split(TYPE_SEPARATOR, 2);
        if (typeSplit.length > 1) {
            type = AssetType.getTypeForId(typeSplit[0]);
            String[] packageSplit = typeSplit[1].split(MODULE_SEPARATOR, 2);
            if (packageSplit.length > 1) {
                moduleName = packageSplit[0];
                assetName = packageSplit[1];
                normalisedModuleName = UriUtil.normalise(packageSplit[0]);
                normalisedAssetName = UriUtil.normalise(packageSplit[1]);
            }
        }
    }

    public AssetType getAssetType() {
        return type;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getNormalisedModuleName() {
        return normalisedModuleName;
    }

    public String getAssetName() {
        return assetName;
    }

    public String getNormalisedAssetName() {
        return normalisedAssetName;
    }

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

    @Override
    public String toNormalisedString() {
        if (!isValid()) {
            return "";
        }
        return type.getTypeId() + TYPE_SEPARATOR + normalisedModuleName + MODULE_SEPARATOR + normalisedAssetName;
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

    public String toNormalisedSimpleString() {
        if (!isValid()) {
            return "";
        }
        return normalisedModuleName + MODULE_SEPARATOR + normalisedAssetName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AssetUri) {
            AssetUri other = (AssetUri) obj;
            return Objects.equal(type, other.type)
                    && Objects.equal(normalisedModuleName, other.normalisedModuleName)
                    && Objects.equal(normalisedAssetName, other.normalisedAssetName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, normalisedModuleName, normalisedAssetName);
    }


}
