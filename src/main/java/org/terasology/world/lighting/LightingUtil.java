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

package org.terasology.world.lighting;

import org.terasology.math.Side;
import org.terasology.world.block.Block;
import org.terasology.world.propagation.PropagationComparison;

/**
 * Utility methods that drive the logic of light propagation
 */
public final class LightingUtil {
    private LightingUtil() {
    }

    public static boolean doesSunlightRetainsFullStrengthIn(Block block) {
        return block.isTranslucent() && !block.isLiquid();
    }

    public static boolean canSpreadLightOutOf(Block fromBlock, Side direction) {
        return fromBlock.getLuminance() > 0 || fromBlock.isTranslucent() || !fromBlock.isFullSide(direction);
    }

    public static boolean canSpreadLightInto(Block toBlock, Side direction) {
        return toBlock.isTranslucent() || !toBlock.isFullSide(direction);
    }

    /**
     * @param newBlock
     * @param oldBlock
     * @return The propagation of lighting by newBlock compared to oldBlock
     */
    public static PropagationComparison compareLightingPropagation(Block newBlock, Block oldBlock) {
        if (newBlock.isTranslucent() && oldBlock.isTranslucent()) {
            return PropagationComparison.IDENTICAL;
        } else if (newBlock.isTranslucent()) {
            for (Side side : Side.values()) {
                if (oldBlock.isFullSide(side)) {
                    return PropagationComparison.MORE_PERMISSIVE;
                }
            }
        } else if (oldBlock.isTranslucent()) {
            for (Side side : Side.values()) {
                if (newBlock.isFullSide(side)) {
                    return PropagationComparison.MORE_RESTRICTED;
                }
            }
        } else {
            boolean permit = false;
            for (Side side : Side.values()) {
                boolean newBlocked = newBlock.isFullSide(side);
                boolean oldBlocked = oldBlock.isFullSide(side);
                if (newBlocked && !oldBlocked) {
                    return PropagationComparison.MORE_RESTRICTED;
                }
                permit = oldBlocked && !newBlocked;
            }
            if (permit) {
                return PropagationComparison.MORE_PERMISSIVE;
            }
        }
        return PropagationComparison.IDENTICAL;
    }

}
