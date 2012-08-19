package org.terasology.world.lighting;

import org.terasology.math.Side;
import org.terasology.world.block.Block;

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
            return PropagationComparison.IDENTICAL;
        } else if (oldBlock.isTranslucent()) {
            for (Side side : Side.values()) {
                if (newBlock.isFullSide(side)) {
                    return PropagationComparison.MORE_RESTRICTED;
                }
            }
            return PropagationComparison.IDENTICAL;
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
            return PropagationComparison.IDENTICAL;
        }
    }
}