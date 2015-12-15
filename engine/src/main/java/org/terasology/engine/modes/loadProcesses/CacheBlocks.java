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
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;

import java.util.Iterator;

/**
 */
public class CacheBlocks extends StepBasedLoadProcess {

    private final Context context;
    private Iterator<BlockFamily> blockFamilyIterator;

    public CacheBlocks(Context context) {
       this.context = context;
    }

    @Override
    public String getMessage() {
        return "Caching Blocks...";
    }

    @Override
    public boolean step() {
        if (blockFamilyIterator.hasNext()) {
            BlockFamily family = blockFamilyIterator.next();
            family.getArchetypeBlock().getMeshGenerator();
            stepDone();
        }
        return !blockFamilyIterator.hasNext();
    }

    @Override
    public void begin() {
        BlockManager blockManager = context.get(BlockManager.class);
        blockFamilyIterator = blockManager.listRegisteredBlockFamilies().iterator();
        setTotalSteps(blockManager.getBlockFamilyCount());
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
