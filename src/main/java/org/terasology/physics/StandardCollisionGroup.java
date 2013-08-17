/*
 * Copyright 2013 Moving Blocks
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
 * @author Immortius
 */
public enum StandardCollisionGroup implements CollisionGroup {
    // JAVA7: Use binary literals
    DEFAULT((short) 0b0000001),
    STATIC((short) 0b0000010),
    KINEMATIC((short) 0b0000100),
    DEBRIS((short) 0b0001000),
    SENSOR((short) 0b0010000),
    CHARACTER((short) 0b0100000),
    WORLD((short) 0b1000000),
    ALL((short) 0b1111111);

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
