/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.world.block.shapes;

import org.terasology.naming.Name;

import java.util.Optional;

/**
 * Placement information for block shapes: placementType, symmetry and archetype
 */
public class BlockShapePlacement {

    private final PlacementType type;
    private final Symmetry symmetry;
    private final Name archetype;

    private BlockShapePlacement(PlacementType type, Symmetry symmetry, Name archetype) {
        this.type = type;
        this.symmetry = symmetry;
        this.archetype = archetype;
    }

    public PlacementType getType() {
        return type;
    }

    public Symmetry getSymmetry() {
        return symmetry;
    }

    public Name getArchetype() {
        return archetype;
    }

    public static BlockShapePlacement of(Optional<PlacementType> type, Optional<Symmetry> symmetry, Optional<String> archetype) {
        PlacementType placementType = type.orElse(PlacementType.CUBE);
        Name archetypeName = new Name(archetype.orElseGet(placementType::getArchetype));
        return new BlockShapePlacement(placementType, symmetry.orElse(Symmetry.SYMMETRIC), archetypeName);
    }

    public static BlockShapePlacement defaultFor(BlockShapeData shape) {
        PlacementType type = null;
        if (!shape.isYawSymmetric()) {
            type = PlacementType.HORIZONTAL_SIDE;
        } else {
            type = PlacementType.CUBE;
        }
        return new BlockShapePlacement(type, Symmetry.SYMMETRIC, new Name(type.getArchetype()));
    }

    public enum PlacementType {
        CUBE("none"),
        SIDE("FRONT"),
        EDGE("BOTTOM_BACK"),
        CORNER("BOTTOM_LEFT_BACK"),
        HORIZONTAL_SIDE("FRONT");

        private final String archetype;

        private PlacementType(String archetype) {
            this.archetype = archetype;
        }

        public String getArchetype() {
            return archetype;
        }
    }

    //TODO define different symmetries correctly
    public enum Symmetry {

        NONE,
        SYMMETRIC;

        public boolean hasEdgeSymmetry() {
            return this == SYMMETRIC;
        }

        public boolean hasSideSymmetry() {
            return this == SYMMETRIC;
        }

        public boolean hasCornerSymmetry() {
            return this == SYMMETRIC;
        }
    }


}
