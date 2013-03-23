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

package org.terasology.physics;

import java.util.Locale;

/**
 * @author Immortius
 */
public enum StandardCollisionGroup implements CollisionGroup {
    // JAVA7: Use binary literals
    DEFAULT((short) 1),
    STATIC((short) 2),
    KINEMATIC((short) 4),
    DEBRIS((short) 8),
    SENSOR((short) 16),
    CHARACTER((short) 32),
    WORLD((short) 64),
    ALL((short) -1);

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
