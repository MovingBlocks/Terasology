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

import org.terasology.engine.ComponentSystemManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;

/**
 * @author Immortius
 */
public class InitialiseSystems extends SingleStepLoadProcess {
    @Override
    public String getMessage() {
        return "Initialising Systems...";
    }

    @Override
    public boolean step() {
        EngineEntityManager entityManager = (EngineEntityManager) CoreRegistry.get(EntityManager.class);
        EntitySystemLibrary entitySystemLibrary = CoreRegistry.get(EntitySystemLibrary.class);
        BlockEntityRegistry blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);

        CoreRegistry.get(NetworkSystem.class).connectToEntitySystem(entityManager, entitySystemLibrary, blockEntityRegistry);
        ComponentSystemManager csm = CoreRegistry.get(ComponentSystemManager.class);
        csm.initialise();

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

}
