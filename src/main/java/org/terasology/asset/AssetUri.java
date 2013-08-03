/*
 * Copyright 2013 Moving Blocks
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

import java.util.Locale;

/**
 * @author Immortius
 */
public final class AssetUri implements Comparable<AssetUri> {
    public static final String TYPE_SEPARATOR = ":";
    public static final String PACKAGE_SEPARATOR = ":";

    private AssetType type;
    private String packageName = "";
    private String assetName = "";
    private String normalisedPackageName = "";
    private String normalisedAssetName = "";

    public AssetUri() {
    }

    public AssetUri(AssetType type, String packageName, String assetName) {
        this.type = type;
        this.packageName = packageName;
        this.normalisedPackageName = packageName.toLowerCase(Locale.ENGLISH);
        this.assetName = assetName;
        this.normalisedAssetName = assetName.toLowerCase(Locale.ENGLISH);
    }

    public AssetUri(AssetType type, String simpleUri) {
        this.type = type;
        String[] split = simpleUri.split(PACKAGE_SEPARATOR, 2);
        if (split.length > 1) {
            packageName = split[0];
            assetName = split[1];
            normalisedPackageName = split[0].toLowerCase(Locale.ENGLISH);
            normalisedAssetName = split[1].toLowerCase(Locale.ENGLISH);
        }
    }

    public AssetUri(String uri) {
        // TODO: handle incomplete/relative uris?
        String[] typeSplit = uri.split(TYPE_SEPARATOR, 2);
        if (typeSplit.length > 1) {
            type = AssetType.getTypeForId(typeSplit[0]);
            String[] packageSplit = typeSplit[1].split(PACKAGE_SEPARATOR, 2);
            if (packageSplit.length > 1) {
                packageName = packageSplit[0];
                assetName = packageSplit[1];
                normalisedPackageName = packageSplit[0].toLowerCase(Locale.ENGLISH);
                normalisedAssetName = packageSplit[1].toLowerCase(Locale.ENGLISH);
            }
        }
    }

    public AssetType getAssetType() {
        return type;
    }

    public String getPackage() {
        return packageName;
    }

    public String getNormalisedPackage() {
        return normalisedPackageName;
    }

    public String getAssetName() {
        return assetName;
    }

    public String getNormalisedAssetName() {
        return normalisedAssetName;
    }

    public boolean isValid() {
        return type != null && !packageName.isEmpty() && !assetName.isEmpty();
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "";
        }
        return type.getTypeId() + TYPE_SEPARATOR + packageName + PACKAGE_SEPARATOR + assetName;
    }

    /**
     * @return The asset uri, minus the type
     */
    public String getSimpleString() {
        if (!isValid()) {
            return "";
        }
        return packageName + PACKAGE_SEPARATOR + assetName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AssetUri) {
            AssetUri other = (AssetUri) obj;
            return Objects.equal(type, other.type)
                    && Objects.equal(normalisedPackageName, other.normalisedPackageName)
                    && Objects.equal(normalisedAssetName, other.normalisedAssetName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, normalisedPackageName, normalisedAssetName);
    }

    @Override
    public int compareTo(AssetUri o) {
        return toString().compareTo(o.toString());
    }
}
