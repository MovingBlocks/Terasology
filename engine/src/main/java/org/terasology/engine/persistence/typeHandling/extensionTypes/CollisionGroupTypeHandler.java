// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.extensionTypes;

import org.terasology.engine.physics.CollisionGroup;
import org.terasology.engine.physics.CollisionGroupManager;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

/**
 */
public class CollisionGroupTypeHandler extends StringRepresentationTypeHandler<CollisionGroup> {

    private CollisionGroupManager groupManager;

    public CollisionGroupTypeHandler(CollisionGroupManager groupManager) {
        this.groupManager = groupManager;
    }

    @Override
    public String getAsString(CollisionGroup item) {
        if (item == null) {
            return "";
        }
        return item.getName();
    }

    @Override
    public CollisionGroup getFromString(String representation) {
        return groupManager.getCollisionGroup(representation);
    }

}
