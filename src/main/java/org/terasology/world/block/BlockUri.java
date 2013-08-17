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
import org.terasology.engine.AbstractBaseUri;

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
public class BlockUri extends AbstractBaseUri {
    public static final String IDENTIFIER_SEPARATOR = ".";
    public static final String IDENTIFIER_SEPARATOR_REGEX = "\\.";

    private AssetUri shape = null;

    private String moduleName = "";
    private String familyName = "";
    private String blockIdentifier = "";

    private String normalisedModuleName = "";
    private String normalisedFamilyName = "";
    private String normalisedBlockIdentifier = "";

    public BlockUri(String moduleName, String familyName) {
        this.moduleName = moduleName;
        this.familyName = familyName;
        this.normalisedModuleName = normalise(moduleName);
        this.normalisedFamilyName = normalise(familyName);
    }

    public BlockUri(String moduleName, String familyName, String identifier) {
        this(moduleName, familyName);
        this.blockIdentifier = identifier;
        this.normalisedBlockIdentifier = normalise(identifier);
    }

    public BlockUri(String moduleName, String familyName, String shapeModuleName, String shapeName) {
        this(moduleName, familyName);
        this.shape = new AssetUri(AssetType.SHAPE, shapeModuleName, shapeName);
    }

    public BlockUri(BlockUri familyUri, String identifier) {
        this.shape = familyUri.shape;
        moduleName = familyUri.moduleName;
        familyName = familyUri.familyName;
        normalisedModuleName = familyUri.normalisedModuleName;
        normalisedFamilyName = familyUri.normalisedFamilyName;

        blockIdentifier = identifier;
        normalisedBlockIdentifier = normalise(blockIdentifier);
    }

    public BlockUri(String uri) {
        String[] split = uri.split(MODULE_SEPARATOR, 4);
        if (split.length > 1) {
            moduleName = split[0];
        }
        if (split.length == 4) {
            familyName = split[1];
            String shapeModuleName = split[2];
            split = split[3].split(IDENTIFIER_SEPARATOR_REGEX, 2);
            if (split.length > 1) {
                shape = new AssetUri(AssetType.SHAPE, shapeModuleName, split[0]);
                blockIdentifier = split[1];
            } else if (split.length == 1) {
                shape = new AssetUri(AssetType.SHAPE, shapeModuleName, split[0]);
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
        normalisedModuleName = normalise(moduleName);
        normalisedFamilyName = normalise(familyName);
        normalisedBlockIdentifier = normalise(blockIdentifier);
    }

    public boolean isValid() {
        return !moduleName.isEmpty() && !familyName.isEmpty() && (shape == null || shape.isValid());
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public String getNormalisedModuleName() {
        return normalisedModuleName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getNormalisedFamilyName() {
        return normalisedFamilyName;
    }

    public boolean hasShape() {
        return shape != null;
    }

    public AssetUri getShapeUri() {
        return shape;
    }

    public String getIdentifier() {
        return blockIdentifier;
    }

    public String getNormalisedIdentifier() {
        return normalisedBlockIdentifier;
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
    public String toNormalisedString() {
        if (isValid()) {
            StringBuilder builder = new StringBuilder();
            builder.append(normalisedModuleName);
            builder.append(MODULE_SEPARATOR);
            builder.append(normalisedFamilyName);
            if (shape != null) {
                builder.append(MODULE_SEPARATOR);
                builder.append(shape.toNormalisedSimpleString());
            }
            if (!normalisedBlockIdentifier.isEmpty()) {
                builder.append(IDENTIFIER_SEPARATOR);
                builder.append(normalisedBlockIdentifier);
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
            return Objects.equal(other.normalisedModuleName, normalisedModuleName) &&
                    Objects.equal(other.normalisedFamilyName, normalisedFamilyName) &&
                    Objects.equal(other.shape, shape) &&
                    Objects.equal(other.normalisedBlockIdentifier, normalisedBlockIdentifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(normalisedModuleName, normalisedFamilyName, shape, normalisedBlockIdentifier);
    }

}
