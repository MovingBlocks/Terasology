// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.chunks.ChunkProvider;

/**
 * Quick and dirty load step to create a dummy world entity on remote clients
 * TODO: The World Entity should be replicated, and the replicated entity linked.
 * TODO: Further from that each world will have a world entity, and it should drive the creation of the world classes in the first place.
 */
public class CreateRemoteWorldEntity extends SingleStepLoadProcess {

    private final Context context;
    private EntityManager entityManager;
    private ChunkProvider chunkProvider;

    public CreateRemoteWorldEntity(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Linking world";
    }

    @Override
    public void begin() {
        entityManager = context.get(EntityManager.class);
        chunkProvider = context.get(ChunkProvider.class);
    }

    @Override
    public boolean step() {
        EntityRef worldEntity = entityManager.create();
        worldEntity.addComponent(new WorldComponent());
        chunkProvider.setWorldEntity(worldEntity);
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
