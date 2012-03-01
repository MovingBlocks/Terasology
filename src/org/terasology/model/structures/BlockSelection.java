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

import java.util.HashSet;
import java.util.Set;

/**
 * A selection of block positions, which may be relative (within a BlockCollection) or absolute (placed in a world)
 * Useful for tracking meta-block objects like Portals, Doors, Trees, etc
 * In other words simply a central wrapper around a Set of BlockPositions in case we change Set later or add utility
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class BlockSelection {

    private final HashSet<BlockPosition> _positions = new HashSet<BlockPosition>();

    /**
     * Forward the position addition to the internal Set
     * @param pos BlockPosition to add to this selection
     * @return true if this set did not already contain the position
     */
    public boolean add(BlockPosition pos) {
        return _positions.add(pos);
    }

    public Set<BlockPosition> getPositions() {
        return _positions;
    }

    public boolean overlaps(BlockSelection otherSelection) {
        // compare this selection's positions against those in the provided selection and see if there is _any_ overlap
        return false;
    }
}