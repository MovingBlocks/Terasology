/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.world.block;

import com.google.common.base.Objects;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;

import java.util.Locale;

/**
 * Identifier for both blocks and block families.
 * The block uri has a pattern of:
 * [package]:[blockFamily]:[shapePackage]:[shapeName].[blockIdentifier]
 * the third and forth parts are only used for blocks that don't use the engine:cube shape, and which
 * are generated from a multi-shape block.
 * The blockIdentifier is only used for blocks withing a block family that contains multiple blocks
 * e.g.
 * engine:brickstair.left for left-aligned stairs
 * engine:stone:engine:stair for the family of stone stairs generated from a multishape block definition
 *
 * @author Immortius
 */
public class BlockUri {
    private static final String PACKAGE_SEPARATOR = ":";
    private static final String IDENTIFIER_SEPARATOR = ".";
    private static final String IDENTIFIER_SEPARATOR_REGEX = "\\.";

    private String packageName = "";
    private String familyName = "";
    private String shapePackageName = "";
    private String shapeName = "";
    private String blockIdentifier = "";

    public BlockUri(String packageName, String familyName) {
        this.packageName = packageName.toLowerCase(Locale.ENGLISH);
        this.familyName = familyName.toLowerCase(Locale.ENGLISH);
    }

    public BlockUri(String packageName, String familyName, String identifier) {
        this(packageName, familyName);
        this.blockIdentifier = identifier.toLowerCase(Locale.ENGLISH);
    }

    public BlockUri(String packageName, String familyName, String shapePackageName, String shapeName) {
        this(packageName, familyName);
        this.shapePackageName = shapePackageName;
        this.shapeName = shapeName;
    }

    public BlockUri(BlockUri familyUri, String identifier) {
        packageName = familyUri.packageName;
        familyName = familyUri.familyName;
        shapePackageName = familyUri.shapePackageName;
        shapeName = familyUri.shapeName;
        blockIdentifier = identifier.toLowerCase(Locale.ENGLISH);
    }

    public BlockUri(String uri) {
        String[] split = uri.toLowerCase(Locale.ENGLISH).split(PACKAGE_SEPARATOR, 4);
        if (split.length > 1) {
            packageName = split[0];
        }
        if (split.length == 4) {
            familyName = split[1];
            shapePackageName = split[2];
            split = split[3].split(IDENTIFIER_SEPARATOR_REGEX, 2);
            if (split.length > 1) {
                shapeName = split[0];
                blockIdentifier = split[1];
            } else if (split.length == 1) {
                shapeName = split[0];
            }
        } else if (split.length == 2) {
            split = split[1].split(IDENTIFIER_SEPARATOR_REGEX, 2);
            if (split.length > 1) {
                familyName = split[0];
                blockIdentifier = split[1];
            } else if (split.length == 1) {
                familyName = split[0];
            }
        }
    }

    public boolean isValid() {
        return !packageName.isEmpty() && !familyName.isEmpty() && (shapePackageName.isEmpty() == shapeName.isEmpty());
    }

    public String getPackage() {
        return packageName;
    }

    public String getFamily() {
        return familyName;
    }

    public AssetUri getShapeUri() {
        return new AssetUri(AssetType.SHAPE, shapePackageName, shapeName);
    }

    public String getIdentifier() {
        return blockIdentifier;
    }

    public BlockUri getFamilyUri() {
        if (blockIdentifier.isEmpty()) {
            return this;
        }
        if (shapePackageName != null) {
            return new BlockUri(packageName, familyName, shapePackageName, shapeName);
        }
        return new BlockUri(packageName, familyName);
    }

    @Override
    public String toString() {
        if (isValid()) {
            StringBuilder builder = new StringBuilder();
            builder.append(packageName);
            builder.append(PACKAGE_SEPARATOR);
            builder.append(familyName);
            if (!shapePackageName.isEmpty()) {
                builder.append(PACKAGE_SEPARATOR);
                builder.append(shapePackageName);
                builder.append(PACKAGE_SEPARATOR);
                builder.append(shapeName);
            }
            if (blockIdentifier.isEmpty()) {
                return packageName + PACKAGE_SEPARATOR + familyName;
            }
            return packageName + PACKAGE_SEPARATOR + familyName + IDENTIFIER_SEPARATOR + blockIdentifier;
        }
        return "";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof BlockUri) {
            BlockUri other = (BlockUri) obj;
            return Objects.equal(other.packageName, packageName) && Objects.equal(other.familyName, familyName) && Objects.equal(other.shapePackageName, shapePackageName) && Objects.equal(other.shapeName, shapeName) && Objects.equal(other.blockIdentifier, blockIdentifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(packageName, familyName, shapePackageName, shapeName, blockIdentifier);
    }

}
