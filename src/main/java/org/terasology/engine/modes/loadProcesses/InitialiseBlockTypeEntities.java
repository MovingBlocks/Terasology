/*
 * Copyright 2013 Moving Blocks
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

import org.terasology.engine.CoreRegistry;
import org.terasology.engine.modes.LoadProcess;
import org.terasology.entitySystem.EntityManager;
import org.terasology.world.block.typeEntity.BlockTypeEntityGenerator;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.block.management.BlockManagerImpl;

/**
 * @author Immortius
 */
public class InitialiseBlockTypeEntities implements LoadProcess {

    @Override
    public String getMessage() {
        return "Initialising Block Type Entities";
    }

    @Override
    public boolean step() {
        BlockManagerImpl blockManager = (BlockManagerImpl) CoreRegistry.get(BlockManager.class);
        blockManager.subscribe(new BlockTypeEntityGenerator(CoreRegistry.get(EntityManager.class), blockManager));
        return true;
    }

    @Override
    public int begin() {
        return 1;
    }
}
