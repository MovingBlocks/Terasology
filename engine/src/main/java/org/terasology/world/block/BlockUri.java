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

package org.terasology.world.block;

import com.google.common.base.Objects;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.engine.Uri;
import org.terasology.naming.Name;

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
public class BlockUri implements Uri, Comparable<BlockUri> {
    public static final String IDENTIFIER_SEPARATOR = ".";
    public static final String IDENTIFIER_SEPARATOR_REGEX = "\\.";

    private AssetUri shape;

    private Name moduleName = Name.EMPTY;
    private Name familyName = Name.EMPTY;
    private Name blockIdentifier = Name.EMPTY;

    public BlockUri(String moduleName, String familyName) {
        this(new Name(moduleName), new Name(familyName));
    }

    public BlockUri(Name moduleName, Name familyName) {
        this.moduleName = moduleName;
        this.familyName = familyName;
    }

    public BlockUri(String moduleName, String familyName, String identifier) {
        this(new Name(moduleName), new Name(familyName), new Name(identifier));
    }

    public BlockUri(Name moduleName, Name familyName, Name identifier) {
        this(moduleName, familyName);
        this.blockIdentifier = identifier;
    }

    public BlockUri(String moduleName, String familyName, String shapeModuleName, String shapeName) {
        this(new Name(moduleName), new Name(familyName), new Name(shapeModuleName), new Name(shapeName));
    }

    public BlockUri(Name moduleName, Name familyName, Name shapeModuleName, Name shapeName) {
        this(moduleName, familyName);
        this.shape = new AssetUri(AssetType.SHAPE, shapeModuleName, shapeName);
    }

    public BlockUri(Name moduleName, Name familyName, AssetUri shape, Name identifier) {
        this(moduleName, familyName, identifier);
        this.shape = shape;
    }

    public BlockUri(BlockUri familyUri, String identifier) {
        this(familyUri.moduleName, familyUri.familyName, familyUri.shape, new Name(identifier));
    }

    public BlockUri(String uri) {
        String[] split = uri.split(MODULE_SEPARATOR, 4);
        if (split.length > 1) {
            moduleName = new Name(split[0]);
        }
        if (split.length == 4) {
            familyName = new Name(split[1]);
            String shapeModuleName = split[2];
            split = split[3].split(IDENTIFIER_SEPARATOR_REGEX, 2);
            if (split.length > 1) {
                shape = new AssetUri(AssetType.SHAPE, shapeModuleName, split[0]);
                blockIdentifier = new Name(split[1]);
            } else if (split.length == 1) {
                shape = new AssetUri(AssetType.SHAPE, shapeModuleName, split[0]);
            }
        } else if (split.length == 2) {
            split = split[1].split(IDENTIFIER_SEPARATOR_REGEX, 2);
            if (split.length > 1) {
                familyName = new Name(split[0]);
                blockIdentifier = new Name(split[1]);
            } else if (split.length == 1) {
                familyName = new Name(split[0]);
            }
        }
    }

    public boolean isValid() {
        return !moduleName.isEmpty() && !familyName.isEmpty() && (shape == null || shape.isValid());
    }

    @Override
    public Name getModuleName() {
        return moduleName;
    }

    public Name getFamilyName() {
        return familyName;
    }

    public boolean hasShape() {
        return shape != null;
    }

    public AssetUri getShapeUri() {
        return shape;
    }

    public Name getIdentifier() {
        return blockIdentifier;
    }

    /**
     * @return The uri of the block's family, including shape
     */
    public BlockUri getFamilyUri() {
        if (blockIdentifier.isEmpty()) {
            return this;
        }
        if (shape != null) {
            return new BlockUri(moduleName, familyName, shape.getModuleName(), shape.getAssetName());
        }
        return new BlockUri(moduleName, familyName);
    }

    /**
     * @return The uri of the block's family, excluding shape
     */
    public BlockUri getRootFamilyUri() {
        if (blockIdentifier.isEmpty() && shape == null) {
            return this;
        }
        return new BlockUri(moduleName, familyName);
    }

    @Override
    public String toString() {
        if (isValid()) {
            StringBuilder builder = new StringBuilder();
            builder.append(moduleName);
            builder.append(MODULE_SEPARATOR);
            builder.append(familyName);
            if (shape != null) {
                builder.append(MODULE_SEPARATOR);
                builder.append(shape.toSimpleString());
            }
            if (!blockIdentifier.isEmpty()) {
                builder.append(IDENTIFIER_SEPARATOR);
                builder.append(blockIdentifier);
            }
            return builder.toString();
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
            return Objects.equal(other.moduleName, moduleName)
                    && Objects.equal(other.familyName, familyName)
                    && Objects.equal(other.shape, shape)
                    && Objects.equal(other.blockIdentifier, blockIdentifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(moduleName, familyName, shape, blockIdentifier);
    }

    @Override
    public int compareTo(BlockUri o) {
        int result = moduleName.compareTo(o.getModuleName());
        if (result == 0) {
            result = familyName.compareTo(o.getFamilyName());
        }
        if (result == 0 && shape != null && o.shape != null) {
            result = shape.compareTo(o.shape);
        }
        if (result == 0) {
            result = blockIdentifier.compareTo(o.blockIdentifier);
        }
        return result;
    }
}
