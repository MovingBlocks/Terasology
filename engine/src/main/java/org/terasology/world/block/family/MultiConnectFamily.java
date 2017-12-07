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
package org.terasology.world.block.family;

import com.google.common.collect.Sets;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.SideBitFlag;
import org.terasology.math.geom.Vector3i;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Locale;
import java.util.Set;

/**
 * Multi-Connect family describes a block family that will connect to other neighboring blocks.
 *
 * examples:
 * - Rail Segments
 * - Cables
 * - Fence
 */
public abstract class MultiConnectFamily extends AbstractBlockFamily implements UpdatesWithNeighboursFamily {
    private static final Logger logger = LoggerFactory.getLogger(FreeformFamily.class);

    @In
    protected WorldProvider worldProvider;
    
    @In
    protected BlockEntityRegistry blockEntityRegistry;

    protected TByteObjectMap<Block> blocks = new TByteObjectHashMap<>();
    
    public MultiConnectFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        super(definition, shape, blockBuilder);
        this.setBlockUri(new BlockUri(definition.getUrn()));
        this.setCategory(definition.getCategories());
    }

    public MultiConnectFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        super(definition, blockBuilder);
        this.setBlockUri(new BlockUri(definition.getUrn()));
        this.setCategory(definition.getCategories());

    }

    /**
     * A condition that identifies which sides are valid to connect to
     * @param blockLocation
     * @param connectSide
     * @return
     */
    protected abstract boolean connectionCondition(Vector3i blockLocation, Side connectSide);

    /**
     * Sides that are valid to connect to using SideBitFlag
     * @return
     */
    public abstract byte getConnectionSides();

    public abstract boolean horizontalOnly();

    @Override
    public abstract Block getArchetypeBlock();


    public Set<Block> registerBlock(BlockUri root,BlockFamilyDefinition definition,final BlockBuilderHelper blockBuilder,String name,byte sides,Iterable<Rotation> rotations){
        Set<Block> result = Sets.newLinkedHashSet();
        for(Rotation rotation: rotations)
        {
            byte sideBits = 0;
            for(Side side : SideBitFlag.getSides(sides)){
                sideBits += SideBitFlag.getSide(rotation.rotate(side));
            }
            Block block = blockBuilder.constructTransformedBlock(definition,name,rotation);
            block.setBlockFamily(this);
            block.setUri(new BlockUri(root,new Name(String.valueOf(sideBits))));

            blocks.put(sideBits,block);
            result.add(block);
        }
        return result;
    }


    @Override
    public Block getBlockForPlacement(Vector3i location, Side attachmentSide, Side direction) {
        byte connections = 0;
        for (Side connectSide : SideBitFlag.getSides(getConnectionSides())) {
            if (this.connectionCondition(location, connectSide)) {
                connections += SideBitFlag.getSide(connectSide);
            }
        }
        return blocks.get(connections);
    }

    public Block getBlockForNeighborUpdate(Vector3i location, Block oldBlock) {
        byte connections = 0;
        for (Side connectSide : SideBitFlag.getSides(getConnectionSides())) {
            if (this.connectionCondition(location, connectSide)) {
                connections += SideBitFlag.getSide(connectSide);
            }
        }
        return blocks.get(connections);
    }


    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                byte connections = Byte.parseByte(blockUri.getIdentifier().toString().toLowerCase(Locale.ENGLISH));
                return blocks.get(connections);
            } catch (IllegalArgumentException e) {
                logger.error("can't find block with URI: {}", blockUri, e);
                return null;
            }
        }
        return null;
    }

    @Override
    public Iterable<Block> getBlocks() {
        return blocks.valueCollection();
    }

}
