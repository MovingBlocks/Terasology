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
        PlacementType placementType = type.orElse(PlacementType.Cube);
        Name archetypeName = new Name(archetype.orElseGet(() -> placementType.getArchetype()));
        return new BlockShapePlacement(placementType, symmetry.orElse(Symmetry.Symmetric), archetypeName);
    }

    public static BlockShapePlacement defaultFor(BlockShapeData shape) {
        PlacementType type = null;
        if (!shape.isYawSymmetric()) {
            type = PlacementType.HorizontalSide;
        } else {
            type = PlacementType.Cube;
        }
        return new BlockShapePlacement(type, Symmetry.Symmetric, new Name(type.getArchetype()));
    }

    public enum PlacementType {
        Cube("none"),
        Side("FRONT"),
        Edge("BottomBack"),
        Corner(""), //fixme
        HorizontalSide("FRONT");

        private final String archetype;

        private PlacementType(String archetype) {
            this.archetype = archetype;
        }

        public String getArchetype() {
            return archetype;
        }
    }

    //FIXME define different symmetries correctly
    public enum Symmetry {

        None,
        Symmetric;

        public boolean hasEdgeSymmetry() {
            return this == Symmetric;
        }

    }


}
