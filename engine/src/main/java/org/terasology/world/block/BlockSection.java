/*
 * Copyright 2019 MovingBlocks
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

import java.util.ArrayList;
import java.util.List;

/**
 * BlockSection is a replacement for BlockPart, which allows for the declaration
 * of arbitrary components of shapes to be rendered whenever any valid side is visible.
 */
public class BlockSection {

    /**
     * getBit converts each valid facing into a specific bit within a byte.
     * @param side A valid block side direction.
     * @return The bitwise position within a byte that represents the side.
     */
    public static int getBit(Side side) {
        switch (side) {
            case FRONT: return 0;
            case BACK: return 1;
            case TOP: return 2;
            case BOTTOM: return 3;
            case LEFT: return 4;
            case RIGHT: return 5;
        }
        return 0;
    }

    /**
     * Determine whether the given face is enabled in the given byte.
     * @param key The byte representing the section's face data.
     * @param side The specific side to check.
     * @return True if the section should render at that side.
     */
    public static boolean isSide(byte key, Side side) {
        return ((byte)(key >> getBit(side)) & 1) == 1;
    }

    /**
     * Creates a byte key representing the given sides.
     * @param sides The sides to be enabled.
     * @return The byte key.
     */
    public static byte key(Side... sides) {
        byte result = 0;
        for (Side side : sides) {
            result |= 1 << getBit(side);
        }
        return result;
    }

    /**
     * Creates a byte key representing the sides which match the provided string.
     * @param string A directional word, such as "left", "right", or "sides".
     * @return The byte key for the proper faces to be enabled.
     */
    public static byte key(String string) {
        return key(fromWord(string));
    }

    /**
     * Creates a list of all valid sides represented by the given key.
     * It is generally recommended to instead use isSide(key, side)
     * @param key The byte key representing a section.
     * @return A List containing all sides that are enabled.
     */
    public static List<Side> fromKey(byte key) {
        List<Side> result = new ArrayList<Side>();
        for (Side side : Side.values()) {
            if (isSide(key, side)) result.add(side);
        }
        return result;
    }

    /**
     * Returns an array of sides represented by a given string.
     * @param name A directional phrase, such as "up", "left", or "sides".
     * @return The array of all sides represented by the phrase.
     */
    public static Side[] fromWord(String name) {
        name = name.toLowerCase();
        switch(name) {
            case "left": return new Side[]{Side.LEFT};
            case "right": return new Side[]{Side.RIGHT};
            case "up":
            case "top": return new Side[]{Side.TOP};
            case "down":
            case "bottom": return new Side[]{Side.BOTTOM};
            case "forward":
            case "forwards":
            case "front": return new Side[]{Side.FRONT};
            case "backward":
            case "backwards":
            case "back": return new Side[]{Side.BACK};
            case "all":
            case "center":
            case "any": return Side.values();
            case "sides": return new Side[]{Side.FRONT, Side.BACK, Side.LEFT, Side.RIGHT};
        }
        return new Side[0];
    }
}
