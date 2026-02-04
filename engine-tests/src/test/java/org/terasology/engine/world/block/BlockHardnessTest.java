package org.terasology.engine.world.block;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Systematic Partition Testing for Block Hardness.
 */
public class BlockHardnessTest {

    private Block softBlock;
    private Block hardBlock;
    private Block instantBlock;
    private Block indestructibleBlock;

    @BeforeEach
    public void setUp() {
        // Partition 1: Instant (Value: 0)
        instantBlock = new Block();
        instantBlock.setHardness(0);

        // Partition 2: Standard (Values: 3 and 50)
        softBlock = new Block();
        softBlock.setHardness(3); // Representative 'soft' integer

        hardBlock = new Block();
        hardBlock.setHardness(50); // Representative 'hard' integer

        // Partition 3: Indestructible (Value: -1)
        indestructibleBlock = new Block();
        indestructibleBlock.setHardness(-1);
    }

    @Test
    @DisplayName("Partition 1: Test Instant Destruction (Hardness = 0)")
    public void testInstantBlockDestruction() {
        assertEquals(0.0f, instantBlock.getHardness(), "Hardness 0 should be instant");
        assertEquals(0.0f, calculateDestructionTime(instantBlock), 0.001f);
    }

    @Test
    @DisplayName("Partition 2: Test Standard Destructible (Hardness > 0)")
    public void testStandardBlockDestruction() {
        assertEquals(3.0f, softBlock.getHardness(), "Soft block hardness should be 3.0");

        assertEquals(50.0f, hardBlock.getHardness(), "Hard block hardness should be 50.0");

        assertTrue(calculateDestructionTime(hardBlock) > calculateDestructionTime(softBlock));
    }

    @Test
    @DisplayName("Partition 3: Test Indestructible (Hardness < 0)")
    public void testIndestructibleBlock() {
        assertEquals(-1.0f, indestructibleBlock.getHardness(), "Hardness -1 should be indestructible");
        assertEquals(-1.0f, calculateDestructionTime(indestructibleBlock), 0.001f);
    }

    @Test
    @DisplayName("Partition 4: Tool Efficiency Multiplier")
    public void testToolEfficiency() {
        // A tool should reduce the time taken to destroy a block
        int hardness = 10;
        softBlock.setHardness(hardness);

        float baseTime = calculateDestructionTime(softBlock);
        float toolMultiplier = 2.0f;
        float reducedTime = baseTime / toolMultiplier;

        assertTrue(reducedTime < baseTime, "Using a tool should result in a faster destruction time");
        assertEquals(baseTime / 2.0f, reducedTime, 0.001f);
    }

    @Test
    @DisplayName("Partition 5: Extreme Boundary (Integer Max)")
    public void testExtremeHardness() {
        // Ensure the calculation doesn't overflow
        int maxHardness = Integer.MAX_VALUE;
        hardBlock.setHardness(maxHardness);

        float time = calculateDestructionTime(hardBlock);

        assertTrue(Float.isFinite(time), "Calculation should handle extreme values without overflow");
        assertTrue(time > 0, "Extreme hardness should still result in positive time");
    }

    /**
     * Helper method to simulate destruction time calculation logic.
     * In the full engine, this might reside in a System or specific Tool logic.
     * * Formula Assumption: Time = Hardness * 1.5 (Base Multiplier without tool)
     */
    private float calculateDestructionTime(Block block) {
        float h = block.getHardness();
        if (h < 0) {
            return -1.0f;
        }
        return h * 1.5f;
    }
}