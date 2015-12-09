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
package org.terasology.world.propagation.light;

import org.terasology.math.Side;
import org.terasology.world.block.Block;
import org.terasology.world.propagation.PropagationComparison;
import org.terasology.world.propagation.PropagationRules;

/**
 */
public abstract class CommonLightPropagationRules implements PropagationRules {

    @Override
    public PropagationComparison comparePropagation(Block newBlock, Block oldBlock, Side side) {
        if (newBlock.isTranslucent() && oldBlock.isTranslucent()) {
            return PropagationComparison.IDENTICAL;
        } else if (newBlock.isTranslucent()) {
            if (oldBlock.isFullSide(side)) {
                return PropagationComparison.MORE_PERMISSIVE;
            }
        } else if (oldBlock.isTranslucent()) {
            if (newBlock.isFullSide(side)) {
                return PropagationComparison.MORE_RESTRICTED;
            }
        } else {
            boolean newBlocked = newBlock.isFullSide(side);
            boolean oldBlocked = oldBlock.isFullSide(side);
            if (newBlocked && !oldBlocked) {
                return PropagationComparison.MORE_RESTRICTED;
            }
            if (oldBlocked && !newBlocked) {
                return PropagationComparison.MORE_PERMISSIVE;
            }
        }
        return PropagationComparison.IDENTICAL;
    }

    @Override
    public boolean canSpreadOutOf(Block block, Side side) {
        return block.getLuminance() > 0 || block.isTranslucent() || !block.isFullSide(side);
    }

    @Override
    public boolean canSpreadInto(Block block, Side side) {
        return block.isTranslucent() || !block.isFullSide(side);
    }
}
