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
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.exceptions.InvalidUrnException;
import org.terasology.engine.Uri;
import org.terasology.naming.Name;

import java.util.Optional;

/**
 * Identifier for both blocks and block families. It is a combination of the ResourceUrn of a block family definition, the id of a block, and optionally the
 * ResourceUrn of a shape.
 * The final pattern is
 * [package]:[blockFamily]:[shapePackage]:[shapeName].[blockIdentifier]
 * the third and forth parts are only used for blocks that don't use the engine:cube shape, and which
 * are generated from a multi-shape block.
 * The blockIdentifier is only used for blocks withing a block family that contains multiple blocks
 * e.g.
 * engine:brickstair.left for left-aligned stairs
 * engine:stone:engine:stair for the family of stone stairs generated from a multishape block definition
 *
 */
public class BlockUri implements Uri, Comparable<BlockUri> {
    public static final String IDENTIFIER_SEPARATOR = ".";
    public static final String IDENTIFIER_SEPARATOR_REGEX = "\\.";

    private final ResourceUrn blockFamilyDefinition;
    private final Optional<ResourceUrn> shape;
    private final Name blockName;

    public BlockUri(String uri) throws BlockUriParseException {
        try {
            String[] split = uri.split(MODULE_SEPARATOR, 4);
            if (split.length <= 1) {
                throw new BlockUriParseException("Could not parse block uri: '" + uri + "'");
            }
            Name blockFamilyDefModule = new Name(split[0]);
            if (split.length == 4) {
                blockFamilyDefinition = new ResourceUrn(blockFamilyDefModule, new Name(split[1]));
                Name shapeModuleName = new Name(split[2]);
                split = split[3].split(IDENTIFIER_SEPARATOR_REGEX, 2);
                shape = Optional.of(new ResourceUrn(shapeModuleName, new Name(split[0])));
                if (split.length > 1) {
                    blockName = new Name(split[1]);
                } else {
                    blockName = Name.EMPTY;
                }
            } else {
                shape = Optional.empty();
                split = split[1].split(IDENTIFIER_SEPARATOR_REGEX, 2);
                blockFamilyDefinition = new ResourceUrn(blockFamilyDefModule, new Name(split[0]));
                if (split.length > 1) {
                    blockName = new Name(split[1]);
                } else {
                    blockName = Name.EMPTY;
                }
            }
        } catch (InvalidUrnException e) {
            throw new BlockUriParseException("Could not parse block uri: '" + uri + "'", e);
        }
    }

    public BlockUri(ResourceUrn blockFamilyDefinition) {
        this(blockFamilyDefinition, Optional.empty(), Name.EMPTY);
    }

    public BlockUri(ResourceUrn blockFamilyDefinition, ResourceUrn shape) {
        this(blockFamilyDefinition, Optional.of(shape), Name.EMPTY);
    }

    public BlockUri(ResourceUrn blockFamilyDefinition, Name blockName) {
        this(blockFamilyDefinition, Optional.empty(), blockName);
    }

    public BlockUri(ResourceUrn blockFamilyDefinition, ResourceUrn shape, Name blockName) {
        this(blockFamilyDefinition, Optional.of(shape), blockName);
    }

    public BlockUri(BlockUri parentUri, Name blockName) {
        this(parentUri.getBlockFamilyDefinitionUrn(), parentUri.getShapeUrn(), blockName);
    }

    private BlockUri(ResourceUrn blockFamilyDefinition, Optional<ResourceUrn> shape, Name blockName) {
        this.blockFamilyDefinition = blockFamilyDefinition;
        this.shape = shape;
        this.blockName = blockName;
    }

    @Override
    public Name getModuleName() {
        return blockFamilyDefinition.getModuleName();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public ResourceUrn getBlockFamilyDefinitionUrn() {
        return blockFamilyDefinition;
    }

    public Optional<ResourceUrn> getShapeUrn() {
        return shape;
    }

    public Name getIdentifier() {
        return blockName;
    }

    /**
     * @return The uri of the block's family, including shape
     */
    public BlockUri getFamilyUri() {
        if (!blockName.isEmpty()) {
            return new BlockUri(blockFamilyDefinition, shape, Name.EMPTY);
        } else {
            return this;
        }
    }

    /**
     * @return The uri of the block's family, excluding shape
     */
    public BlockUri getRootFamilyUri() {
        if (!blockName.isEmpty() || shape.isPresent()) {
            return new BlockUri(blockFamilyDefinition);
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(blockFamilyDefinition.toString());
        if (shape.isPresent()) {
            builder.append(MODULE_SEPARATOR);
            builder.append(shape.get().toString());
        }
        if (!blockName.isEmpty()) {
            builder.append(IDENTIFIER_SEPARATOR);
            builder.append(blockName.toString());
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof BlockUri) {
            BlockUri other = (BlockUri) obj;
            return Objects.equal(other.blockFamilyDefinition, blockFamilyDefinition)
                    && Objects.equal(other.blockName, blockName)
                    && Objects.equal(other.shape, shape);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(blockFamilyDefinition, shape, blockName);
    }

    @Override
    public int compareTo(BlockUri o) {
        int result = blockFamilyDefinition.compareTo(o.blockFamilyDefinition);
        if (result == 0) {
            if (shape.isPresent()) {
                if (o.shape.isPresent()) {
                    result = shape.get().compareTo(o.shape.get());
                } else {
                    return 1;
                }
            } else {
                if (o.shape.isPresent()) {
                    return -1;
                }
            }
        }
        if (result == 0) {
            if (!blockName.isEmpty()) {
                if (!o.blockName.isEmpty()) {
                    result = blockName.compareTo(o.blockName);
                } else {
                    return 1;
                }
            } else {
                if (!o.blockName.isEmpty()) {
                    return -1;
                }
            }
        }
        return result;
    }

    public BlockUri getShapelessUri() {
        if (!getIdentifier().isEmpty()) {
            return new BlockUri(blockFamilyDefinition, getIdentifier());
        } else {
            return new BlockUri(blockFamilyDefinition);
        }
    }
}
