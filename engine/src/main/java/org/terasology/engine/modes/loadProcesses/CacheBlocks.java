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

import org.terasology.engine.CoreRegistry;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;

import java.util.Iterator;

/**
 * @author Immortius
 */
public class CacheBlocks extends StepBasedLoadProcess {

    private Iterator<BlockFamily> blockFamilyIterator;

    @Override
    public String getMessage() {
        return "Caching Blocks...";
    }

    @Override
    public boolean step() {
        BlockFamily family = blockFamilyIterator.next();
        if (!family.getArchetypeBlock().isInvisible()) {
            family.getArchetypeBlock().getMesh();
        }
        stepDone();
        return !blockFamilyIterator.hasNext();
    }

    @Override
    public void begin() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        blockFamilyIterator = blockManager.listRegisteredBlockFamilies().iterator();
        setTotalSteps(blockManager.getBlockFamilyCount());
    }

}
