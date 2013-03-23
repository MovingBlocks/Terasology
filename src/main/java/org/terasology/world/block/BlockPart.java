/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

import org.terasology.math.Side;

import java.util.EnumMap;

/**
 * @author Immortius
 */
public enum BlockPart {
    TOP(Side.TOP),
    LEFT(Side.LEFT),
    RIGHT(Side.RIGHT),
    FRONT(Side.FRONT),
    BACK(Side.BACK),
    BOTTOM(Side.BOTTOM),
    CENTER;

    private static EnumMap<Side, BlockPart> sideMap;
    private static BlockPart[] sides;

    private Side side;

    private BlockPart() {
    }

    private BlockPart(Side side) {
        this.side = side;
    }

    static {
        sideMap = new EnumMap<Side, BlockPart>(Side.class);
        sideMap.put(Side.BOTTOM, BOTTOM);
        sideMap.put(Side.RIGHT, RIGHT);
        sideMap.put(Side.LEFT, LEFT);
        sideMap.put(Side.BACK, BACK);
        sideMap.put(Side.FRONT, FRONT);
        sideMap.put(Side.TOP, TOP);

        sides = new BlockPart[]{TOP, LEFT, RIGHT, FRONT, BACK, BOTTOM};
    }

    public static BlockPart fromSide(Side side) {
        return sideMap.get(side);
    }

    /**
     * @return The horizontal sides, for iteration
     */
    public static BlockPart[] sideValues() {
        return sides;
    }

    public boolean isSide() {
        return side != null;
    }

    public Side getSide() {
        return side;
    }
}
