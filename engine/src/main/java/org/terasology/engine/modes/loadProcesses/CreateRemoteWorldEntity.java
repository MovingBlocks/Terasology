// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.registry.In;
import org.terasology.world.WorldComponent;
import org.terasology.world.chunks.ChunkProvider;

/**
 * Quick and dirty load step to create a dummy world entity on remote clients
 * TODO: The World Entity should be replicated, and the replicated entity linked.
 * TODO: Further from that each world will have a world entity, and it should drive the creation of the world classes in the first place.
 */
@ExpectedCost(1)
public class CreateRemoteWorldEntity extends SingleStepLoadProcess {

    @In
    private EntityManager entityManager;
    @In
    private ChunkProvider chunkProvider;

    @Override
    public String getMessage() {
        return "Linking world";
    }

    @Override
    public boolean step() {
        EntityRef worldEntity = entityManager.create();
        worldEntity.addComponent(new WorldComponent());
        chunkProvider.setWorldEntity(worldEntity);
        return true;
    }
}
