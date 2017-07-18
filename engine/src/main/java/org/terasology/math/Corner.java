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
package org.terasology.math;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.naming.Name;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public enum Corner {

    BOTTOM_LEFT_BACK(Rotation.none()),
    BOTTOM_LEFT_FRONT(Rotation.rotate(Yaw.CLOCKWISE_270)),
    BOTTOM_RIGHT_BACK(Rotation.rotate(Yaw.CLOCKWISE_90)),
    BOTTOM_RIGHT_FRONT(Rotation.rotate(Yaw.CLOCKWISE_180)),

    TOP_RIGHT_BACK(Rotation.rotate(Roll.CLOCKWISE_180)),
    TOP_RIGHT_FRONT(Rotation.rotate(Yaw.CLOCKWISE_270, Pitch.CLOCKWISE_180)),
    TOP_LEFT_FRONT(Rotation.rotate(Pitch.CLOCKWISE_180)),
    TOP_LEFT_BACK(Rotation.rotate(Yaw.CLOCKWISE_90, Pitch.CLOCKWISE_180)),


    RIGHT_BACK_BOTTOM(Rotation.rotate(Roll.CLOCKWISE_90)),
    RIGHT_FRONT_TOP(Rotation.rotate(Pitch.CLOCKWISE_180, Roll.CLOCKWISE_90)),
    RIGHT_BACK_TOP(Rotation.rotate(Yaw.CLOCKWISE_90, Pitch.CLOCKWISE_270)),
    RIGHT_FRONT_BOTTOM(Rotation.rotate(Pitch.CLOCKWISE_90, Roll.CLOCKWISE_90)),

    LEFT_BACK_TOP(Rotation.rotate(Roll.CLOCKWISE_270)),
    LEFT_FRONT_BOTTOM(Rotation.rotate(Yaw.CLOCKWISE_180, Roll.CLOCKWISE_90)),
    LEFT_BACK_BOTTOM(Rotation.rotate(Pitch.CLOCKWISE_90, Roll.CLOCKWISE_270)),
    LEFT_FRONT_TOP(Rotation.rotate(Yaw.CLOCKWISE_270, Pitch.CLOCKWISE_270)),

    BACK_BOTTOM_RIGHT(Rotation.rotate(Yaw.CLOCKWISE_180, Pitch.CLOCKWISE_90)),
    BACK_TOP_RIGHT(Rotation.rotate(Yaw.CLOCKWISE_90, Roll.CLOCKWISE_270)),
    BACK_BOTTOM_LEFT(Rotation.rotate(Yaw.CLOCKWISE_270, Roll.CLOCKWISE_90)),
    BACK_TOP_LEFT(Rotation.rotate(Pitch.CLOCKWISE_270)),

    FRONT_LEFT_BOTTOM(Rotation.rotate(Pitch.CLOCKWISE_90)),
    FRONT_RIGHT_BOTTOM(Rotation.rotate(Yaw.CLOCKWISE_90, Roll.CLOCKWISE_90)),
    FRONT_LEFT_TOP(Rotation.rotate(Yaw.CLOCKWISE_270, Roll.CLOCKWISE_270)),
    FRONT_RIGHT_TOP(Rotation.rotate(Yaw.CLOCKWISE_180, Pitch.CLOCKWISE_270)),;

    static Logger logger = LoggerFactory.getLogger(Corner.class);
    private final Name name;
    private final Rotation rotationFromBottomLeftBack;

    private Corner(Rotation rotationFromBottomLeftBack) {
        this.name = new Name(name());
        this.rotationFromBottomLeftBack = rotationFromBottomLeftBack;
    }

    public Name getName() {
        return name;
    }

    public Rotation getRotationFromBottomLeftBack() {
        return rotationFromBottomLeftBack;
    }


    public static Corner of(Name name) {
        Corner corner = nameCornerMap.get(name);
        if (corner == null) {
            throw new IllegalArgumentException("Invalid name " + name.toString());
        }
        return corner;
    }

    public static Corner ofOrNull(Name name) {
        return nameCornerMap.get(name);
    }

    private static Map<Name, Corner> nameCornerMap;

    static {
        nameCornerMap = new HashMap<Name, Corner>();
        for (Corner c : values()) {
            nameCornerMap.put(c.name, c);
        }
    }

    public static Set<Corner> symmetricSubset() {
        return symmetricSubset;
    }

    private static Set<Corner> symmetricSubset = Collections.unmodifiableSet(EnumSet.range(Corner.BOTTOM_LEFT_BACK, Corner.TOP_LEFT_BACK));

    public Corner getSymmetricEquivalent() {
        return symmetricEquivalent.get(this);
    }

    private static EnumMap<Corner, Corner> symmetricEquivalent = new EnumMap<>(Corner.class);

    static {
        List<String> sides = Arrays.stream(Side.values()).map( s -> s.name().toLowerCase()).collect(Collectors.toList());
        Map<Integer, Corner> equiv = new HashMap<>();
        for (Corner c : values()) {
            String name = c.name().toLowerCase();
            int v = 0;
            for (int i = 0; i < sides.size(); ++i) {
                if (name.contains(sides.get(i))) {
                    v += 1 << i;
                }
            }
            Corner e = equiv.computeIfAbsent(v, k -> c);
            symmetricEquivalent.put(c, e);
        }
    }

    public static Corner forSides(Side first, Side second, Side third) {
        for (Corner c : values()) {
            String upper = c.name().toUpperCase();
            if (!upper.startsWith(first.name())) {
                continue;
            }
            if (!upper.contains(second.name())) {
                continue;
            }
            if (!upper.contains(third.name())) {
                continue;
            }
            return c;
        }
        return BOTTOM_RIGHT_BACK;
    }


}
