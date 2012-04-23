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
package org.terasology.model.structures;

import org.terasology.game.Terasology;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A specialized version of a BlockSelection, specifically a rectangular cuboid made up by exactly two BlockPositions
 * In theory the positions could be identical in which case this would be no different than a single BlockPosition
 * Mainly the idea is that it'll be used to mark cubic areas in the world, like spawn zones or regular rooms ?
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class CubicSelection extends BlockSelection {
    private static Logger logger = Logger.getLogger(CubicSelection.class.getName());

    //TODO: Make factory methods instead of public constructors - that way Cubic can be more picky and not allow default ctor
    //TODO: Add more validation to make sure _positions contains exactly 2 BlockPositions

    /**
     * Constructor simply based on two given positions
     * @param firstCorner The first corner
     * @param secondCorner The second corner
     */
    public CubicSelection(BlockPosition firstCorner, BlockPosition secondCorner) {
        _positions.add(firstCorner);
        _positions.add(secondCorner);
    }

    /**
     * Constructor that creates a CubicSelection of certain stats around a single central anchor position.
     * Note: The top left front corner is placed at half the width, height, and depth from the anchor
     * Rounding issues from odd parameters have NOT been considered
     * @param anchor The central position to place the CubicSelection around
     * @param width Width of the whole selection
     * @param height Height of the whole selection
     * @param depth Depth of the whole selection
     */
    public CubicSelection(BlockPosition anchor, int width, int height, int depth) {
        BlockPosition topLeftFront = new BlockPosition(anchor.x - width / 2, anchor.y - height / 2, anchor.z - depth / 2);
        BlockPosition bottomRightBack = new BlockPosition(anchor.x + width, anchor.y + height, anchor.z + depth);
        logger.log(Level.INFO, "Creating CubicSelection around anchor " + anchor + " with width " + width + ", height " + height + ", depth " + depth);
        logger.log(Level.INFO, "Calculated topLeftFront is " + topLeftFront + ", bottomRightBack is " + bottomRightBack);
        _positions.add(topLeftFront);
        _positions.add(bottomRightBack);
    }

    public BlockPosition randomPosition() {
        //TODO: Make up a random position inside the selection
        return null;
    }

    /**
     * Checks if this CubicSelection overlaps any of the positions in the supplied BlockSelection (which is fairly easy)
     * @param otherSelection A BlockSelection to test against
     * @return boolean for overlap or not
     */
    @Override
    public boolean overlaps(BlockSelection otherSelection) {
        // To see if we have overlap simply need to check positions till we find a single one within the bounds of the cube
        int maxX = calcMaxX();
        int minX = calcMinX();
        int maxY = calcMaxY();
        int minY = calcMinY();
        int maxZ = calcMaxZ();
        int minZ = calcMinZ();

        for (BlockPosition targetPos : otherSelection.positions()) {
            if (targetPos.x <= maxX
                    && targetPos.x >= minX
                    && targetPos.y <= maxY
                    && targetPos.y >= minY
                    && targetPos.z <= maxZ
                    && targetPos.z >= minZ) {
                logger.log(Level.INFO, "Selections overlap at " + targetPos + ", maybe more, returning true");
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if this CubicSelection contain all the positions in the supplied BlockSelection (which is very easy)
     * @param otherSelection A BlockSelection to test against
     * @return boolean for containment or not
     */
    @Override
    public boolean contains(BlockSelection otherSelection) {
        return calcMaxX() >= otherSelection.calcMaxX()
                && calcMinX() <= otherSelection.calcMinX()
                && calcMaxY() >= otherSelection.calcMaxY()
                && calcMinY() <= otherSelection.calcMinY()
                && calcMaxZ() >= otherSelection.calcMaxZ()
                && calcMinZ() <= otherSelection.calcMinZ();
    }

    /**
     * Simple toString
     * @return String with the cubic selection's two positions
     */
    @Override
    public String toString() {
        return "Cube" + super.toString();
    }
}
