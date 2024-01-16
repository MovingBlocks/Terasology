// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation.light;

import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.propagation.PropagationComparison;
import org.terasology.engine.world.propagation.PropagationRules;

/**
 * Defines a set of common rules for how light should propagate
 */
public abstract class CommonLightPropagationRules implements PropagationRules {

    /**
     * Light is more permissive if the block is changed to be translucent or has an open side,
     * otherwise is less permissive or identical.
     * <p>
     * {@inheritDoc}
     */
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

    /**
     * Light can spread out of a block if
     * - it has luminance (ie, glows),
     * - it is translucent
     * - or the side isn't full
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean canSpreadOutOf(Block block, Side side) {
        return block.getLuminance() > 0 || block.isTranslucent() || !block.isFullSide(side);
    }

    /**
     * Light can spread into a block if
     * - it is translucent,
     * - or the side isn't ful
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean canSpreadInto(Block block, Side side) {
        return block.isTranslucent() || !block.isFullSide(side);
    }
}
