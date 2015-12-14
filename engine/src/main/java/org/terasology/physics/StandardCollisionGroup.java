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

package org.terasology.physics;

import java.util.Locale;

/**
 */
public enum StandardCollisionGroup implements CollisionGroup {
    DEFAULT((short) 0b00000001),
    STATIC((short) 0b00000010),
    KINEMATIC((short) 0b00000100),
    DEBRIS((short) 0b00001000),
    SENSOR((short) 0b00010000),
    CHARACTER((short) 0b00100000),
    WORLD((short) 0b01000000),
    LIQUID((short) 0b10000000),
    ALL((short) 0b11111111);

    private short flag;

    private StandardCollisionGroup(short flag) {
        this.flag = flag;
    }

    @Override
    public short getFlag() {
        return flag;
    }

    @Override
    public String getName() {
        return "engine:" + toString().toLowerCase(Locale.ENGLISH);
    }
}
