/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.game.modes.loadProcesses;

import java.util.Iterator;

import org.terasology.game.modes.LoadProcess;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.management.BlockManager;

/**
 * @author Immortius
 */
public class CacheBlocks implements LoadProcess {

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
        return !blockFamilyIterator.hasNext();
    }

    @Override
    public int begin() {
        blockFamilyIterator = BlockManager.getInstance().listRegisteredBlockFamilies().iterator();
        return BlockManager.getInstance().registeredBlockFamiliesCount();
    }
}
