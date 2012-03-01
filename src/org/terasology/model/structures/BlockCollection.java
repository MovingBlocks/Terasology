package org.terasology.model.structures;

/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

import org.terasology.game.Terasology;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.model.blocks.Block;

import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

/**
 * A collection of actual blocks and their relative positions, _not_ absolute positions in an existing world
 * Can also store a specific position that the collection should be oriented around rather than always a specific corner
 * Useful for for blueprints and other things that apply a particular set of expectations on a spot in a world
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class BlockCollection {

    /** Map of what blocks are in which positions */
    private final HashMap<BlockPosition, Block> _blocks = new HashMap<BlockPosition, Block>();

    /** A specific position to use for attaching to a spot in the world - for a tree this could be the bottom trunk block. */
    private BlockPosition _attachPos = new BlockPosition(0,0,0);

    /**
     * Simple return of the BlockPositions this collection holds
     * @return The positions contained within this collection
     */
    public Set<BlockPosition> positions() {
        return _blocks.keySet();
    }

    /**
     * Get the Block that matches the given position
     * @param pos The position we care about
     * @return The block at the position
     */
    public Block getBlock(BlockPosition pos) {
        return _blocks.get(pos);
    }

    /**
     * Return the main collection
     * @return The Position-Block map
     */
    public HashMap<BlockPosition, Block> getBlocks() {
        return _blocks;
    }

    /**
     * Simply forwards to the main build method including itself as the BlockCollection to draw blocks from
     * @param provider The world to build the collection in
     * @param position The position to build the collection at (using the collection's attachment position)
     * @return A BlockSelection containing the final positions the blocks were built at
     */
    public BlockSelection build(IWorldProvider provider, BlockPosition position) {
        return build(provider, position, this);
    }

    /**
     * Builds and returns only the blocks in the collection that matches the blockName
     * @param provider The world to build the collection in
     * @param position The position to build the collection at (using the collection's attachment position)
     * @param blockName The name of the blocks we want to filter by
     * @return The BlockSelection for the built blocks matching the filter
     */
    public BlockSelection buildWithFilter(IWorldProvider provider, BlockPosition position, String blockName) {
        BlockCollection filteredBlocks = filter(blockName);
        return build(provider, position, filteredBlocks);
    }

    /**
     * Create the contents of the collection at a specific position in the world, returning the absolute BlockPositions.
     * This can thus be used to for instance build a blueprint, then optionally sort out what was built where so different
     * block types can be made reactive to subsequent input (say a PortalComponent storing what's frame and what's portal)
     * @param provider The world to build the collection in
     * @param position The position to build the collection at (using the collection's attachment position)
     * @param buildingBlocks The BlockCollection we are using to build with, which could be filtered or the main one here
     * @return A BlockSelection containing the final positions the blocks were built at
     */
    public BlockSelection build(IWorldProvider provider, BlockPosition position, BlockCollection buildingBlocks) {
        BlockSelection result = new BlockSelection();
        Terasology.getInstance().getLogger().log(Level.INFO, "Going to build this collection into the world at " + position + ", attaching at relative " + _attachPos);
        //System.out.println(toString());
        for (BlockPosition pos : buildingBlocks.getBlocks().keySet()) {
            //System.out.println("Processing block " + getBlock(pos) + " relative position " + pos);
            int x = position.x + pos.x - _attachPos.x;
            int y = position.y + pos.y - _attachPos.y;
            int z = position.z + pos.z - _attachPos.z;
            //System.out.println("This block is being placed at " + x + "," + y + "," + z);
            provider.setBlock(x, y, z, buildingBlocks.getBlocks().get(pos).getId(), true, true);
            result.add(new BlockPosition(x,y,z));
        }
        return result;
    }

    /**
     * Returns a filtered BlockCollection only including Blocks matching the supplied name
     * @param blockName The name of the Block we're interested in
     * @return A BlockCollection only containing the interesting blocks
     */
    public BlockCollection filter(String blockName) {
        BlockCollection filtered = new BlockCollection();
        for (BlockPosition pos : _blocks.keySet()) {
            Block b = _blocks.get(pos);
            if (b.getTitle().equals(blockName)) {
                //System.out.println("Block " + b + " matches the filter for " + blockName + " so adding it");
                filtered.addBlock(pos, b);
            }
        }
        return filtered;
    }

    /**
     * TODO: Can this be used by build to not duplicate code? Maybe a static version that takes a BlockCollection param?
     * Calculates a localized version of the BlockSelection in this Collection's map as per a given position
     * @param localPos The local position we're going to localize against
     * @return The localized BlockSelection
     */
    public BlockSelection getLocalizedSelection(BlockPosition localPos) {
        BlockSelection result = new BlockSelection();
        Terasology.getInstance().getLogger().log(Level.INFO, "Going to localize this BlockCollection to position" + localPos);
        for (BlockPosition pos : _blocks.keySet()) {
            //System.out.println("Processing block " + getBlock(pos) + " relative position " + pos);
            int x = localPos.x + pos.x - _attachPos.x;
            int y = localPos.y + pos.y - _attachPos.y;
            int z = localPos.z + pos.z - _attachPos.z;
            //System.out.println("Localized to " + x + "," + y + "," + z);
            result.add(new BlockPosition(x,y,z));
        }
        return result;
    }

    public void addBlock(BlockPosition pos, Block b) {
        _blocks.put(pos,b);
    }

    public BlockPosition getAttachPos() {
        return _attachPos;
    }

    public void setAttachPos(BlockPosition pos) {
        _attachPos = pos;
    }

    /**
     * Returns the width (x) of the BlockCollection's widest points (horizontally measured, not diagonally)
     * @return int holding calculated width or -1 if there are no elements
     */
    public int calcWidth() {
        if (_blocks.isEmpty()) {
            return -1;
        }
        int xMax = Integer.MIN_VALUE;
        int xMin = Integer.MAX_VALUE;
        for (BlockPosition pos : _blocks.keySet()) {
            if (pos.x > xMax) {
                xMax = pos.x;
            }
            if (pos.x < xMin) {
                xMin = pos.x;
            }
        }

        // Calculate the width + 1 (a one-block wide construct would calculate to 0)
        return Math.abs(xMax) - Math.abs(xMin) + 1;
    }

    /**
     * Returns the height (y) of the BlockCollection's highest points (vertically measured, not diagonally)
     * @return int holding calculated height or -1 if there are no elements
     */
    public int calcHeight() {
        if (_blocks.isEmpty()) {
            return -1;
        }
        int yMax = Integer.MIN_VALUE;
        int yMin = Integer.MAX_VALUE;
        for (BlockPosition pos : _blocks.keySet()) {
            if (pos.y > yMax) {
                yMax = pos.y;
            }
            if (pos.y < yMin) {
                yMin = pos.y;
            }
        }

        // Calculate the height + 1 (a one-block wide construct would calculate to 0)
        return Math.abs(yMax) - Math.abs(yMin) + 1;
    }

    /**
     * Returns the depth (z) of the BlockCollection's deepest points (horizontally measured, not diagonally)
     * @return int holding calculated depth or -1 if there are no elements
     */
    public int calcDepth() {
        if (_blocks.isEmpty()) {
            return -1;
        }
        int zMax = Integer.MIN_VALUE;
        int zMin = Integer.MAX_VALUE;
        for (BlockPosition pos : _blocks.keySet()) {
            if (pos.z > zMax) {
                zMax = pos.z;
            }
            if (pos.z < zMin) {
                zMin = pos.z;
            }
        }

        // Calculate the depth + 1 (a one-block deep construct would calculate to 0)
        return Math.abs(zMax) - Math.abs(zMin) + 1;
    }

    public String toString() {
        String result = "[[";
        for (BlockPosition pos : _blocks.keySet()) {
            result += pos + ":" + getBlock(pos) + "],[";
        }
        result += "]],attachPos:" + _attachPos;
        return result;
    }
}
