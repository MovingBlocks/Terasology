// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics;

import java.util.Arrays;

public interface CollisionGroup {
    short getFlag();

    String getName();

    static short combineGroups(CollisionGroup... groups) {
        return combineGroups(Arrays.asList(groups));
    }

    static short combineGroups(Iterable<CollisionGroup> groups) {
        short flags = 0;
        for (CollisionGroup group : groups) {
            flags |= group.getFlag();
        }
        return flags;
    }


}
