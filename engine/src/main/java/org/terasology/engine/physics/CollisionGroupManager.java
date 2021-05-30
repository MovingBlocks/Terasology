// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

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

    public Collection<CollisionGroup> getAllGroups() {
        return Collections.unmodifiableCollection(collisionGroupMap.values());
    }
}
