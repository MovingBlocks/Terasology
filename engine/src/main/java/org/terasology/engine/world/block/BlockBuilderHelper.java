// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block;

import org.terasology.engine.math.Rotation;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.loader.SectionDefinitionData;
import org.terasology.engine.world.block.shapes.BlockShape;

/**
 * A helper class for building and registering blocks with specific properties.
 * Intended for use with Block Family related code.
 * <p>
 * The core features of this helper is to convert from definition data, into a Block
 * and to register this block into the engine for use.
 */
public interface BlockBuilderHelper {

    /**
     * @see #constructSimpleBlock(BlockFamilyDefinition, BlockShape, String, BlockUri, BlockFamily)
     */
    Block constructSimpleBlock(BlockFamilyDefinition definition, BlockUri uri, BlockFamily blockFamily);

    /**
     * @see #constructSimpleBlock(BlockFamilyDefinition, BlockShape, String, BlockUri, BlockFamily)
     */
    Block constructSimpleBlock(BlockFamilyDefinition definition, BlockShape shape, BlockUri uri, BlockFamily blockFamily);

    /**
     * @see #constructSimpleBlock(BlockFamilyDefinition, BlockShape, String, BlockUri, BlockFamily)
     */
    Block constructSimpleBlock(BlockFamilyDefinition definition, String section, BlockUri uri, BlockFamily blockFamily);

    /**
     * Constructs a basic block from the data provided. This block is assumed to have no rotations, and to use
     * reasonable defaults The block is registered into the engine as to be directly usable
     * <p>
     * For blocks that contain rotations or that use custom values see
     * {@link #constructTransformedBlock(BlockFamilyDefinition, Rotation, BlockUri, BlockFamily)} and
     * {@link #constructCustomBlock(String, BlockShape, Rotation, SectionDefinitionData, BlockUri, BlockFamily)}
     *
     * @param definition The definition for the block family this block should belong to
     * @param shape The shape this block should be displayed with
     * @param section The block family subsection this block will represent
     * @param uri The URI to use for the block
     * @param blockFamily The block family instance this block will belong to
     * @return The constructed and registered block
     */
    Block constructSimpleBlock(BlockFamilyDefinition definition, BlockShape shape, String section,
                               BlockUri uri, BlockFamily blockFamily);

    /**
     * @see #constructTransformedBlock(BlockFamilyDefinition, Rotation, BlockUri, BlockFamily)
     */
    Block constructTransformedBlock(BlockFamilyDefinition definition, Rotation rotation,
                                    BlockUri uri, BlockFamily blockFamily);

    /**
     * @see #constructTransformedBlock(BlockFamilyDefinition, Rotation, BlockUri, BlockFamily)
     */
    Block constructTransformedBlock(BlockFamilyDefinition definition, String section, Rotation rotation,
                                    BlockUri uri, BlockFamily blockFamily);

    /**
     * @see #constructTransformedBlock(BlockFamilyDefinition, Rotation, BlockUri, BlockFamily)
     */
    Block constructTransformedBlock(BlockFamilyDefinition definition, BlockShape shape, Rotation rotation,
                                    BlockUri uri, BlockFamily blockFamily);

    /**
     * Construct a block that has a specific rotation applied to it.
     * <p>
     * For blocks that do not have a rotation, see
     * {@link #constructSimpleBlock(BlockFamilyDefinition, BlockShape, String, BlockUri, BlockFamily)}
     * For blocks that require more custom control/values see
     * {@link #constructCustomBlock(String, BlockShape, Rotation, SectionDefinitionData, BlockUri, BlockFamily)}
     *
     * @param definition  The definition for the block family this block will belong to
     * @param shape       The shape to apply to the block
     * @param section     The block family subsection this block will represent
     * @param rotation    The rotation to apply to the block
     * @param uri         The URI to use for this block
     * @param blockFamily The instance of the block family this block will belong to
     * @return
     */
    Block constructTransformedBlock(BlockFamilyDefinition definition, BlockShape shape, String section, Rotation rotation,
                                    BlockUri uri, BlockFamily blockFamily);

    /**
     * Construct a block as a member of a family with specified values and information.
     * This method, and the other helper methods in this class, should be used to create the component blocks of a block family.
     * <p>
     * In particular, this method relies upon the section parameter, as this is what determines a large quantity of the block's properties.
     *
     * @param defaultName The name of the block, used for the display name
     * @param shape       The shape to apply to the block
     * @param rotation    The rotation to apply to the block
     * @param section     The definition data for the section of the family this block represents
     * @param uri         The URI of the block
     * @param blockFamily The family instance to add to the block
     * @return The newly created and registered block
     */
    Block constructCustomBlock(String defaultName, BlockShape shape, Rotation rotation, SectionDefinitionData section,
                               BlockUri uri, BlockFamily blockFamily);
}
