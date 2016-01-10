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
package org.terasology.world.propagation;

import com.google.common.collect.Sets;
import org.terasology.math.ChunkMath;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.ChunkConstants;
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

    private Set<Vector3i>[] reduceQueues;
    private Set<Vector3i>[] increaseQueues;

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
        if (comparison.isPermitting()) {
            byte existingValue = regenWorld.getValueAt(blockChange.getPosition());
            queueSpreadRegen(blockChange.getPosition(), existingValue);
        } else if (comparison.isRestricting()) {
            Vector3i adjPos = Side.BOTTOM.getAdjacentPos(blockChange.getPosition());
            byte existingValue = regenWorld.getValueAt(adjPos);
            reduce(adjPos, existingValue);
        }
    }

    private void reviewChangeToTop(BlockChange blockChange) {
        PropagationComparison comparison = regenRules.comparePropagation(blockChange.getTo(), blockChange.getFrom(), Side.TOP);
        if (comparison.isPermitting()) {
            Vector3i adjPos = Side.TOP.getAdjacentPos(blockChange.getPosition());
            byte adjValue = regenWorld.getValueAt(adjPos);
            if (adjValue != PropagatorWorldView.UNAVAILABLE) {
                queueSpreadRegen(adjPos, adjValue);
            }
        } else if (comparison.isRestricting()) {
            byte existingValue = regenWorld.getValueAt(blockChange.getPosition());
            reduce(blockChange.getPosition(), existingValue);
        }
    }

    private void queueSpreadRegen(Vector3i position, byte value) {
        increaseQueues[value].add(position);
    }

    private void processRegenReduction() {
        for (byte depth = 0; depth <= regenRules.getMaxValue(); depth++) {
            Set<Vector3i> toProcess = reduceQueues[depth];

            toProcess.forEach(this::purge);
            toProcess.clear();
        }
    }

    private void purge(Vector3i pos) {
        int expectedValue = regenWorld.getValueAt(pos);
        if (expectedValue != 0) {
            Vector3i position = new Vector3i(pos);
            for (byte i = 0; i <= ChunkConstants.MAX_SUNLIGHT_REGEN; ++i) {
                if (regenWorld.getValueAt(position) == expectedValue) {
                    regenWorld.setValueAt(position, i);
                    if (expectedValue - ChunkConstants.SUNLIGHT_REGEN_THRESHOLD > 0) {
                        sunlightPropagator.regenerate(new Vector3i(position), (byte) (expectedValue - ChunkConstants.SUNLIGHT_REGEN_THRESHOLD));
                    }
                } else {
                    break;
                }
                position.y--;
                if (expectedValue < ChunkConstants.MAX_SUNLIGHT_REGEN) {
                    expectedValue++;
                }
            }
        }
    }

    private void processRegenIncrease() {
        for (byte depth = regenRules.getMaxValue(); depth >= 0; depth--) {
            Set<Vector3i> toProcess = increaseQueues[depth];

            for (Vector3i pos : toProcess) {
                push(pos, depth);
            }
            toProcess.clear();
        }
    }

    private void push(Vector3i pos, byte value) {
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
                    byte sunlightValue = (byte) (regenValue - ChunkConstants.SUNLIGHT_REGEN_THRESHOLD);
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

    private void reduce(Vector3i position, byte oldValue) {
        if (oldValue > 0) {
            reduceQueues[oldValue].add(position);
        }
    }

    @Override
    public void propagateBetween(LitChunk chunk, LitChunk adjChunk, Side side, boolean propagateExternal) {
        if (side == Side.BOTTOM) {
            int[] depth = new int[ChunkConstants.SIZE_X * ChunkConstants.SIZE_Z];
            int[] startingRegen = new int[depth.length];
            propagateSweep(chunk, adjChunk, depth, startingRegen);

            int[] adjDepths = new int[depth.length];
            ChunkMath.populateMinAdjacent2D(depth, adjDepths, ChunkConstants.SIZE_X, ChunkConstants.SIZE_Z, !propagateExternal);
            if (propagateExternal) {
                for (int z = 0; z < ChunkConstants.SIZE_Z; ++z) {
                    adjDepths[z * ChunkConstants.SIZE_X] = 0;
                    adjDepths[ChunkConstants.SIZE_X - 1 + z * ChunkConstants.SIZE_X] = 0;
                }
                for (int x = 0; x < ChunkConstants.SIZE_X; ++x) {
                    adjDepths[x] = 0;
                    adjDepths[x + ChunkConstants.SIZE_X * (ChunkConstants.SIZE_Z - 1)] = 0;
                }
            }

            int[] adjStartingRegen = new int[depth.length];
            ChunkMath.populateMinAdjacent2D(startingRegen, adjStartingRegen, ChunkConstants.SIZE_X, ChunkConstants.SIZE_Z, true);

            markForPropagation(adjChunk, depth, startingRegen, adjDepths, adjStartingRegen);
        }
    }

    private void markForPropagation(LitChunk toChunk, int[] depth, int[] startingRegen, int[] adjDepths, int[] adjStartingRegen) {
        Vector3i pos = new Vector3i();
        for (int z = 0; z < ChunkConstants.SIZE_Z; ++z) {
            for (int x = 0; x < ChunkConstants.SIZE_X; ++x) {
                int depthIndex = x + ChunkConstants.SIZE_X * z;
                int start = startingRegen[depthIndex];
                int adjStart = adjStartingRegen[depthIndex];
                if (start - adjStart > 1) {
                    int initialDepth = Math.max(ChunkConstants.SUNLIGHT_REGEN_THRESHOLD - start, 0);
                    int finalDepth = depth[depthIndex];

                    int strength = Math.min(start + initialDepth - ChunkConstants.SUNLIGHT_REGEN_THRESHOLD + 1, ChunkConstants.MAX_SUNLIGHT);

                    for (int i = initialDepth; i <= finalDepth; ++i) {
                        sunlightPropagator.propagateFrom(toChunk.chunkToWorldPosition(x, ChunkConstants.SIZE_Y - i - 1, z),
                                (byte) (strength));
                        if (strength < ChunkConstants.MAX_SUNLIGHT) {
                            strength++;
                        }
                    }
                } else {
                    int initialDepth = Math.max(adjDepths[depthIndex], ChunkConstants.SUNLIGHT_REGEN_THRESHOLD - start);
                    byte strength = (byte) Math.min(ChunkConstants.MAX_SUNLIGHT, start + initialDepth - ChunkConstants.SUNLIGHT_REGEN_THRESHOLD + 1);
                    for (int i = initialDepth; i <= depth[depthIndex]; ++i) {
                        sunlightPropagator.propagateFrom(toChunk.chunkToWorldPosition(x, ChunkConstants.SIZE_Y - i - 1, z), strength);
                        if (strength < ChunkConstants.MAX_SUNLIGHT) {
                            strength++;
                        }
                        pos.y--;
                    }
                }

            }
        }
    }

    private void propagateSweep(LitChunk fromChunk, LitChunk toChunk, int[] depth, int[] startingRegen) {
        Vector3i pos = new Vector3i();
        for (int z = 0; z < ChunkConstants.SIZE_Z; ++z) {
            for (int x = 0; x < ChunkConstants.SIZE_X; ++x) {
                int depthIndex = x + ChunkConstants.SIZE_X * z;
                startingRegen[depthIndex] = regenRules.getValue(fromChunk, new Vector3i(x, 0, z));
                byte expectedValue = (byte) Math.min(startingRegen[depthIndex] + 1, ChunkConstants.MAX_SUNLIGHT_REGEN);
                Block fromBlock = fromChunk.getBlock(x, 0, z);
                Block toBlock = toChunk.getBlock(x, ChunkConstants.SIZE_Y - 1, z);
                if (!(regenRules.canSpreadOutOf(fromBlock, Side.BOTTOM) && regenRules.canSpreadInto(toBlock, Side.TOP))) {
                    continue;
                }
                byte predictedValue = 0;
                pos.set(x, ChunkConstants.SIZE_Y - 1, z);

                int currentValue = regenRules.getValue(toChunk, pos);
                while (currentValue == predictedValue && expectedValue > currentValue) {
                    regenRules.setValue(toChunk, pos, expectedValue);
                    depth[depthIndex]++;
                    byte sunlight = (byte) (expectedValue - ChunkConstants.SUNLIGHT_REGEN_THRESHOLD);
                    if (sunlight > 0 && sunlight > toChunk.getSunlight(pos)) {
                        toChunk.setSunlight(pos, sunlight);
                    }
                    if (expectedValue < ChunkConstants.MAX_SUNLIGHT_REGEN) {
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
    public void propagateFrom(Vector3i pos, Block block) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void propagateFrom(Vector3i pos, byte value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void regenerate(Vector3i pos, byte value) {
        throw new UnsupportedOperationException();
    }

}
