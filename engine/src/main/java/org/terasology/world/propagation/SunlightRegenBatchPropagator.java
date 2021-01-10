// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.propagation;

import com.google.common.collect.Sets;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.math.JomlUtil;
import org.terasology.math.Side;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.chunks.LitChunk;

import java.util.Arrays;
import java.util.Set;

/**
 * Batch propagator that works on a set of changed blocks
 *
 */
public class SunlightRegenBatchPropagator implements BatchPropagator {

    private PropagationRules regenRules;
    private PropagatorWorldView regenWorld;
    private PropagatorWorldView sunlightWorld;
    private BatchPropagator sunlightPropagator;

    private Set<Vector3ic>[] reduceQueues;
    private Set<Vector3ic>[] increaseQueues;

    public SunlightRegenBatchPropagator(PropagationRules regenRules, PropagatorWorldView regenWorld, BatchPropagator sunlightPropagator, PropagatorWorldView sunlightWorld) {
        this.regenRules = regenRules;
        this.regenWorld = regenWorld;
        this.sunlightPropagator = sunlightPropagator;
        this.sunlightWorld = sunlightWorld;

        increaseQueues = new Set[regenRules.getMaxValue() + 1];
        reduceQueues = new Set[regenRules.getMaxValue() + 1];
        for (int i = 0; i < regenRules.getMaxValue() + 1; ++i) {
            increaseQueues[i] = Sets.newLinkedHashSet();
            reduceQueues[i] = Sets.newLinkedHashSet();
        }
    }

    @Override
    public void process(BlockChange... changes) {
        process(Arrays.asList(changes));
    }

    @Override
    public void process(Iterable<BlockChange> blockChanges) {
        for (BlockChange blockChange : blockChanges) {
            reviewChange(blockChange);
        }

        processRegenReduction();
        processRegenIncrease();
        cleanUp();
    }

    private void reviewChange(BlockChange blockChange) {
        reviewChangeToTop(blockChange);
        reviewChangeToBottom(blockChange);
    }

    private void reviewChangeToBottom(BlockChange blockChange) {
        PropagationComparison comparison = regenRules.comparePropagation(blockChange.getTo(), blockChange.getFrom(), Side.BOTTOM);
        Vector3ic blockChangePosition = blockChange.getPosition();
        if (comparison.isPermitting()) {
            byte existingValue = regenWorld.getValueAt(blockChangePosition);
            queueSpreadRegen(blockChangePosition, existingValue);
        } else if (comparison.isRestricting()) {
            Vector3i adjPos = Side.BOTTOM.getAdjacentPos(blockChangePosition, new Vector3i());
            byte existingValue = regenWorld.getValueAt(adjPos);
            reduce(adjPos, existingValue);
        }
    }

    private void reviewChangeToTop(BlockChange blockChange) {
        PropagationComparison comparison = regenRules.comparePropagation(blockChange.getTo(), blockChange.getFrom(), Side.TOP);
        Vector3ic blockChangePosition = blockChange.getPosition();
        if (comparison.isPermitting()) {
            Vector3i adjPos = Side.TOP.getAdjacentPos(blockChangePosition, new Vector3i());
            byte adjValue = regenWorld.getValueAt(adjPos);
            if (adjValue != PropagatorWorldView.UNAVAILABLE) {
                queueSpreadRegen(adjPos, adjValue);
            }
        } else if (comparison.isRestricting()) {
            byte existingValue = regenWorld.getValueAt(blockChangePosition);
            reduce(blockChangePosition, existingValue);
        }
    }

    private void queueSpreadRegen(Vector3ic position, byte value) {
        increaseQueues[value].add(position);
    }

    private void processRegenReduction() {
        for (byte depth = 0; depth <= regenRules.getMaxValue(); depth++) {
            Set<Vector3ic> toProcess = reduceQueues[depth];

            toProcess.forEach(this::purge);
            toProcess.clear();
        }
    }

    private void purge(Vector3ic pos) {
        int expectedValue = regenWorld.getValueAt(pos);
        if (expectedValue != 0) {
            Vector3i position = new Vector3i(pos);
            for (byte i = 0; i <= Chunks.MAX_SUNLIGHT_REGEN; ++i) {
                if (regenWorld.getValueAt(position) == expectedValue) {
                    regenWorld.setValueAt(position, i);
                    if (expectedValue - Chunks.SUNLIGHT_REGEN_THRESHOLD > 0) {
                        sunlightPropagator.regenerate(new Vector3i(position), (byte) (expectedValue - Chunks.SUNLIGHT_REGEN_THRESHOLD));
                    }
                } else {
                    break;
                }
                position.y--;
                if (expectedValue < Chunks.MAX_SUNLIGHT_REGEN) {
                    expectedValue++;
                }
            }
        }
    }

    private void processRegenIncrease() {
        for (byte depth = regenRules.getMaxValue(); depth >= 0; depth--) {
            Set<Vector3ic> toProcess = increaseQueues[depth];

            for (Vector3ic pos : toProcess) {
                push(pos, depth);
            }
            toProcess.clear();
        }
    }

    private void push(Vector3ic pos, byte value) {
        byte regenValue = value;
        Block block = regenWorld.getBlockAt(pos);
        Vector3i position = new Vector3i(pos);
        while (regenRules.canSpreadOutOf(block, Side.BOTTOM)) {
            regenValue = regenRules.propagateValue(regenValue, Side.BOTTOM, block);
            position.y -= 1;
            byte adjValue = regenWorld.getValueAt(position);
            if (adjValue < regenValue && adjValue != PropagatorWorldView.UNAVAILABLE) {
                block = regenWorld.getBlockAt(position);
                if (regenRules.canSpreadInto(block, Side.TOP)) {
                    regenWorld.setValueAt(position, regenValue);
                    reduceQueues[adjValue].remove(position);
                    byte sunlightValue = (byte) (regenValue - Chunks.SUNLIGHT_REGEN_THRESHOLD);
                    if (sunlightValue > 0) {
                        byte prevValue = sunlightWorld.getValueAt(position);
                        if (prevValue < sunlightValue) {
                            sunlightWorld.setValueAt(position, sunlightValue);
                            sunlightPropagator.propagateFrom(new Vector3i(position), sunlightValue);
                        }
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }

    private void cleanUp() {
    }

    private void reduce(Vector3ic position, byte oldValue) {
        if (oldValue > 0) {
            reduceQueues[oldValue].add(position);
        }
    }

    @Override
    public void propagateBetween(LitChunk chunk, LitChunk adjChunk, Side side, boolean propagateExternal) {
        if (side == Side.BOTTOM) {
            int[] depth = new int[Chunks.SIZE_X * Chunks.SIZE_Z];
            int[] startingRegen = new int[depth.length];
            propagateSweep(chunk, adjChunk, depth, startingRegen);

            int[] adjDepths = new int[depth.length];
            populateMinAdjacent2D(depth, adjDepths, Chunks.SIZE_X, Chunks.SIZE_Z, !propagateExternal);
            if (propagateExternal) {
                for (int z = 0; z < Chunks.SIZE_Z; ++z) {
                    adjDepths[z * Chunks.SIZE_X] = 0;
                    adjDepths[Chunks.SIZE_X - 1 + z * Chunks.SIZE_X] = 0;
                }
                for (int x = 0; x < Chunks.SIZE_X; ++x) {
                    adjDepths[x] = 0;
                    adjDepths[x + Chunks.SIZE_X * (Chunks.SIZE_Z - 1)] = 0;
                }
            }

            int[] adjStartingRegen = new int[depth.length];
            populateMinAdjacent2D(startingRegen, adjStartingRegen, Chunks.SIZE_X, Chunks.SIZE_Z, true);

            markForPropagation(adjChunk, depth, startingRegen, adjDepths, adjStartingRegen);
        }
    }

    private void markForPropagation(LitChunk toChunk, int[] depth, int[] startingRegen, int[] adjDepths, int[] adjStartingRegen) {
        Vector3i pos = new Vector3i();
        for (int z = 0; z < Chunks.SIZE_Z; ++z) {
            for (int x = 0; x < Chunks.SIZE_X; ++x) {
                int depthIndex = x + Chunks.SIZE_X * z;
                int start = startingRegen[depthIndex];
                int adjStart = adjStartingRegen[depthIndex];
                int initialDepth;
                if (start - adjStart > 1) {
                    initialDepth = Math.max(Chunks.SUNLIGHT_REGEN_THRESHOLD - start, 0);
                } else {
                    initialDepth = Math.max(Chunks.SUNLIGHT_REGEN_THRESHOLD - start, adjDepths[depthIndex]);
                }
                byte strength = (byte) Math.min(Chunks.MAX_SUNLIGHT, start + initialDepth - Chunks.SUNLIGHT_REGEN_THRESHOLD + 1);
                for (int i = initialDepth; i <= depth[depthIndex]; ++i) {
                    sunlightPropagator.propagateFrom(toChunk.chunkToWorldPosition(x, Chunks.SIZE_Y - i - 1, z, pos), strength);
                    if (strength < Chunks.MAX_SUNLIGHT) {
                        strength++;
                    }
                }
            }
        }
    }

    private void propagateSweep(LitChunk fromChunk, LitChunk toChunk, int[] depth, int[] startingRegen) {
        Vector3i pos = new Vector3i();
        for (int z = 0; z < Chunks.SIZE_Z; ++z) {
            for (int x = 0; x < Chunks.SIZE_X; ++x) {
                int depthIndex = x + Chunks.SIZE_X * z;
                startingRegen[depthIndex] = regenRules.getValue(fromChunk, new Vector3i(x, 0, z));
                byte expectedValue = (byte) Math.min(startingRegen[depthIndex] + 1, Chunks.MAX_SUNLIGHT_REGEN);
                Block fromBlock = fromChunk.getBlock(x, 0, z);
                Block toBlock = toChunk.getBlock(x, Chunks.SIZE_Y - 1, z);
                if (!(regenRules.canSpreadOutOf(fromBlock, Side.BOTTOM) && regenRules.canSpreadInto(toBlock, Side.TOP))) {
                    continue;
                }
                byte predictedValue = 0;
                pos.set(x, Chunks.SIZE_Y - 1, z);

                int currentValue = regenRules.getValue(toChunk, pos);
                while (currentValue == predictedValue && expectedValue > currentValue) {
                    regenRules.setValue(toChunk, pos, expectedValue);
                    depth[depthIndex]++;
                    byte sunlight = (byte) (expectedValue - Chunks.SUNLIGHT_REGEN_THRESHOLD);
                    if (sunlight > 0 && sunlight > toChunk.getSunlight(JomlUtil.from(pos))) {
                        toChunk.setSunlight(JomlUtil.from(pos), sunlight);
                    }
                    if (expectedValue < Chunks.MAX_SUNLIGHT_REGEN) {
                        expectedValue++;
                    }
                    predictedValue++;
                    pos.y--;
                    currentValue = regenRules.getValue(toChunk, pos);
                }
            }
        }
    }

    @Override
    public void propagateFrom(Vector3ic pos, Block block) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void propagateFrom(Vector3ic pos, byte value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void regenerate(Vector3ic pos, byte value) {
        throw new UnsupportedOperationException();
    }

}
