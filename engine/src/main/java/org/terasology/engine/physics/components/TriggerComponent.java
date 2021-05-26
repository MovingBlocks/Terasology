// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.components;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.physics.CollisionGroup;
import org.terasology.engine.physics.StandardCollisionGroup;
import org.terasology.engine.world.block.ForceBlockActive;

import java.util.List;

@ForceBlockActive
public class TriggerComponent implements Component {
    @Replicate
    public CollisionGroup collisionGroup = StandardCollisionGroup.SENSOR;
    
    @Replicate
    public List<CollisionGroup> detectGroups = Lists.<CollisionGroup>newArrayList(StandardCollisionGroup.DEFAULT);
}
