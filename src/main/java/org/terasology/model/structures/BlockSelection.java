/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A selection of block positions, which may be relative (within a BlockCollection) or absolute (placed in a world)
 * Useful for tracking meta-block objects like Portals, Doors, Trees, etc
 * In other words simply a central wrapper around a Set of BlockPositions in case we change Set later or add utility
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class BlockSelection {
    private static Logger logger = Logger.getLogger(BlockSelection.class.getName());

    protected HashSet<BlockPosition> _positions = new HashSet<BlockPosition>();

    /**
     * Default constructor for an empty selection
     */
    public BlockSelection() {

    }

    /**
     * Constructor useful to take the Set<BlockPosition> you get back via keySet() on Maps, etc
     *
     * @param positions The initial BlockPositions this selection should contain
     */
    public BlockSelection(Set<BlockPosition> positions) {
        _positions = new HashSet<BlockPosition>(positions);
    }

    /**
     * Simple check for being empty
     *
     * @return boolean empty or not
     *         //TODO: See if there's anywhere this should be used yet?
     */
    public boolean isEmpty() {
        return _positions.isEmpty();
    }

    /**
     * Forward the position addition to the internal Set
     *
     * @param pos BlockPosition to add to this selection
     * @return true if this set did not already contain the position
     */
    public boolean add(BlockPosition pos) {
        return _positions.add(pos);
    }

    /**
     * Returns the Set containing the selection of positions
     *
     * @return The positions in a Set
     */
    public Set<BlockPosition> positions() {
        return _positions;
    }

    /**
     * Calculates the highest X (right) value of any position in this BlockSelection, or Integer.MIN_VALUE if empty
     *
     * @return highest X or Integer.MIN_VALUE if empty TODO: Make this throw exceptions instead!
     */
    public int calcMaxX() {
        int xMax = Integer.MIN_VALUE;
        for (BlockPosition pos : _positions) {
            if (pos.x > xMax) {
                xMax = pos.x;
            }
        }
        return xMax;
    }

    /**
     * Calculates the lowest X (left) value of any position in this BlockSelection, or Integer.MAX_VALUE if empty
     *
     * @return lowest X or Integer.MAX_VALUE if empty
     */
    public int calcMinX() {
        int xMin = Integer.MAX_VALUE;
        for (BlockPosition pos : _positions) {
            if (pos.x < xMin) {
                xMin = pos.x;
            }
        }
        return xMin;
    }

    /**
     * Returns the total width (x) of the BlockSelection's widest point (horizontally measured, not diagonally)
     *
     * @return int holding calculated width or -1 if there are no elements
     */
    public int calcWidth() {
        if (_positions.isEmpty())
            return -1;

        // Calculate the total width + 1 (a one-block wide construct would otherwise calculate to 0)
        return Math.abs(calcMaxX() - calcMinX()) + 1;
    }

    /**
     * Calculates the highest Y value of any position in this BlockSelection, or Integer.MIN_VALUE if empty
     *
     * @return highest Y or Integer.MIN_VALUE if empty
     */
    public int calcMaxY() {
        int yMax = Integer.MIN_VALUE;
        for (BlockPosition pos : _positions) {
            if (pos.y > yMax) {
                yMax = pos.y;
            }
        }
        return yMax;
    }

    /**
     * Calculates the lowest Y value of any position in this BlockSelection, or Integer.MAX_VALUE if empty
     *
     * @return lowest Y or Integer.MAX_VALUE if empty
     */
    public int calcMinY() {
        int yMin = Integer.MAX_VALUE;
        for (BlockPosition pos : _positions) {
            if (pos.y < yMin) {
                yMin = pos.y;
            }
        }
        return yMin;
    }

    /**
     * Returns the total height (y) from the BlockSelection's highest point to the lowest (vertically measured, not diagonally)
     *
     * @return int holding calculated height or -1 if there are no elements
     */
    public int calcHeight() {
        if (_positions.isEmpty())
            return -1;

        // Calculate the total height + 1 (a one-block tall construct would otherwise calculate to 0)
        return Math.abs(calcMaxY() - calcMinY()) + 1;
    }

    /**
     * Calculates the highest Z value of any position in this BlockSelection, or Integer.MIN_VALUE if empty
     *
     * @return highest Z or Integer.MIN_VALUE if empty
     */
    public int calcMaxZ() {
        int zMax = Integer.MIN_VALUE;
        for (BlockPosition pos : _positions) {
            if (pos.z > zMax) {
                zMax = pos.z;
            }
        }
        return zMax;
    }

    /**
     * Calculates the lowest Z value of any position in this BlockSelection, or Integer.MAX_VALUE if empty
     *
     * @return lowest Z or Integer.MAX_VALUE if empty
     */
    public int calcMinZ() {
        int zMin = Integer.MAX_VALUE;
        for (BlockPosition pos : _positions) {
            if (pos.z < zMin) {
                zMin = pos.z;
            }
        }
        return zMin;
    }

    /**
     * Returns the total depth (z) of the BlockSelection's closest point to the deepest (horizontally measured, not diagonally)
     *
     * @return int holding calculated depth or -1 if there are no elements
     */
    public int calcDepth() {
        if (_positions.isEmpty())
            return -1;

        // Calculate the total depth + 1 (a one-block deep construct would otherwise calculate to 0)
        return Math.abs(calcMaxZ() - calcMinZ()) + 1;
    }

    /**
     * Compare this BlockSelection's positions vs a given BlockSelection and see if there is any overlap at all
     *
     * @param otherSelection the other selection of positions
     * @return boolean for overlap or not
     */
    public boolean overlaps(BlockSelection otherSelection) {
        if (!possiblyOverlaps(otherSelection)) {
            return false;
        }

        // If we're still here then the BlockSelections possibly overlap, but we'll need to test per position to be sure (long hard way to test)
        for (BlockPosition myPos : _positions) {
            for (BlockPosition targetPos : otherSelection.positions()) {
                if (myPos.equals(targetPos)) {
                    logger.log(Level.INFO, "Selections overlap at " + myPos + ", maybe more, returning true");
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Compare this BlockSelection's positions vs a given BlockSelection and see if this one entirely contains the other
     *
     * @param otherSelection the other selection of positions
     * @return boolean for full containment or not
     */
    public boolean contains(BlockSelection otherSelection) {
        // TODO: Not sure this makes performance sense here? Since the slightest lack of containment means we know the answer
        if (!possiblyOverlaps(otherSelection)) {
            return false;
        }

        // If we're still here then the BlockSelections possibly overlap, but we'll need to test for full containment
        for (BlockPosition otherPos : otherSelection.positions()) {
            boolean contained = false;
            for (BlockPosition myPos : _positions) {
                if (otherPos.equals(myPos)) {
                    contained = true;
                    break;
                }
            }

            if (!contained) {
                logger.log(Level.INFO, "Position " + otherPos + "was not contained in this selection, returning false");
                return false;
            }
        }

        return true;
    }

    /**
     * Checks to see if the given BlockSelection can possibly overlap this one, which is figured out by checking bounds
     *
     * @param otherSelection The other BlockSelection to check with
     * @return boolean for possible overlap or not
     */
    public boolean possiblyOverlaps(BlockSelection otherSelection) {
        // See if the entirety of the BlockSelections are completely out of bounds versus each other, nice short way to test
        if (calcMinX() > otherSelection.calcMaxX() || otherSelection.calcMinX() > calcMaxX()) {
            logger.log(Level.INFO, "Selections have no overlap at all along X axis, returning false");
            return false;
        }

        if (calcMinY() > otherSelection.calcMaxY() || otherSelection.calcMinY() > calcMaxY()) {
            logger.log(Level.INFO, "Selections have no overlap at all along Y axis, returning false");
            return false;
        }

        if (calcMinZ() > otherSelection.calcMaxZ() || otherSelection.calcMinZ() > calcMaxZ()) {
            logger.log(Level.INFO, "Selections have no overlap at all along Z axis, returning false");
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String result = "[";
        for (BlockPosition pos : _positions) {
            result += "[" + pos + "],";
        }
        return result.substring(0, result.length() - 1) + "]";
    }
}