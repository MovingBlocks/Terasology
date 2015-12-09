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

import com.google.common.collect.Maps;

import java.util.Locale;
import java.util.Map;

/**
 */
public class CollisionGroupManager {

    private Map<String, CollisionGroup> collisionGroupMap = Maps.newHashMap();

    public CollisionGroupManager() {
        resetGroups();
    }

    public CollisionGroup getCollisionGroup(String name) {
        return collisionGroupMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    public void resetGroups() {
        collisionGroupMap.clear();
        for (CollisionGroup group : StandardCollisionGroup.values()) {
            collisionGroupMap.put(group.getName().toLowerCase(Locale.ENGLISH), group);
        }
    }
}
