/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.engine.modes.loadProcesses;

import org.terasology.context.Context;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.world.WorldComponent;
import org.terasology.world.chunks.ChunkProvider;

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
