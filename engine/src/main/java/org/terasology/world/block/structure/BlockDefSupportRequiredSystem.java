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
package org.terasology.world.block.structure;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import java.util.Collections;
import java.util.Map;

@RegisterSystem
public class BlockDefSupportRequiredSystem extends BaseComponentSystem implements BlockStructuralSupport {
    @In
    private BlockStructuralSupportRegistry registry;

    @In
    private WorldProvider worldProvider;

    @Override
    public void preBegin() {
        registry.registerBlockStructuralSupport(this);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isSufficientlySupported(Vector3i location, Map<Vector3i, Block> blockOverrides) {
        final Block block = getBlockWithOverrides(location, blockOverrides);
        if (block.isSupportRequired()) {
            final Vector3i bottomLocation = Side.BOTTOM.getAdjacentPos(location);
            return !worldProvider.isBlockRelevant(bottomLocation)
                    || getBlockWithOverrides(bottomLocation, blockOverrides).isFullSide(Side.TOP);
        }
        return true;
    }

    @Override
    public boolean shouldBeRemovedDueToChange(Vector3i location, Side sideChanged) {
        return sideChanged == Side.BOTTOM && !isSufficientlySupported(location, Collections.<Vector3i, Block>emptyMap());
    }

    private Block getBlockWithOverrides(Vector3i location, Map<Vector3i, Block> blockOverrides) {
        final Block blockFromOverride = blockOverrides.get(location);
        if (blockFromOverride != null) {
            return blockFromOverride;
        }
        return worldProvider.getBlock(location);
    }
}
