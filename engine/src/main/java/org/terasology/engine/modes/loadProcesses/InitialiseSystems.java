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

package org.terasology.engine.modes.loadProcesses;

import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.network.NetworkSystem;
import org.terasology.world.BlockEntityRegistry;

/**
 */
public class InitialiseSystems extends SingleStepLoadProcess {

    private final Context context;

    public InitialiseSystems(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Initialising Systems...";
    }

    @Override
    public boolean step() {
        EngineEntityManager entityManager = (EngineEntityManager) context.get(EntityManager.class);
        EventLibrary eventLibrary = context.get(EventLibrary.class);
        BlockEntityRegistry blockEntityRegistry = context.get(BlockEntityRegistry.class);

        context.get(NetworkSystem.class).connectToEntitySystem(entityManager, eventLibrary, blockEntityRegistry);
        ComponentSystemManager csm = context.get(ComponentSystemManager.class);
        csm.initialise();

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

}
