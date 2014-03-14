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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.internal.ChunkImpl;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * Batch propagator that works on a set of changed blocks
 *
 * @author Immortius
 */
public class SunlightRegenBatchPropagator implements BatchPropagator {

    private PropagationRules regenRules;
    private PropagatorWorldView regenWorld;
    private PropagatorWorldView sunlightWorld;
    private BatchPropagator sunlightPropagator;

    private Set<Vector3i>[] reduceQueues;
    private Set<Vector3i>[] increaseQueues;

    private Map<Side, Vector3i> chunkEdgeDeltas = Maps.newEnumMap(Side.class);

    public SunlightRegenBatchPropagator(PropagationRules regenRules, PropagatorWorldView regenWorld, BatchPropagator sunlightPropagator, PropagatorWorldView sunlightWorld) {
        this.regenRules = regenRules;
        this.regenWorld = regenWorld;
        this.sunlightPropagator = sunlightPropagator;
        this.sunlightWorld = sunlightWorld;

        for (Side side : Side.values()) {
            Vector3i delta = new Vector3i(side.getVector3i());
            if (delta.x < 0) {
                delta.x += ChunkConstants.SIZE_X;
            } else if (delta.x > 0) {
                delta.x -= ChunkConstants.SIZE_X;
            }
            if (delta.y < 0) {
                delta.y += ChunkConstants.SIZE_Y;
            } else if (delta.y > 0) {
                delta.y -= ChunkConstants.SIZE_Y;
            }
            if (delta.z < 0) {
                delta.z += ChunkConstants.SIZE_Z;
            } else if (delta.z > 0) {
                delta.z -= ChunkConstants.SIZE_Z;
            }
            chunkEdgeDeltas.put(side, delta);
        }

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

            for (Vector3i pos : toProcess) {
                purge(pos);
            }
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
    public void propagateBetween(ChunkImpl chunk, ChunkImpl adjChunk, Side side) {
        if (!side.isVertical()) {
            return;
        }
        Region3i edgeRegion = TeraMath.getEdgeRegion(Region3i.createFromMinAndSize(Vector3i.zero(), ChunkConstants.CHUNK_SIZE), side);
        Vector3i adjPos = new Vector3i();
        for (Vector3i pos : edgeRegion) {
            adjPos.set(pos);
            adjPos.add(chunkEdgeDeltas.get(side));

            Block block = chunk.getBlock(pos);
            byte value = regenRules.getValue(chunk, pos);
            Block adjBlock = adjChunk.getBlock(adjPos);
            byte adjValue = regenRules.getValue(adjChunk, adjPos);

            if (side == Side.BOTTOM) {
                byte expectedAdjValue = regenRules.propagateValue(value, side, block);
                if (regenRules.canSpreadOutOf(block, side) && regenRules.canSpreadInto(adjBlock, side.reverse()) && adjValue < expectedAdjValue) {
                    regenRules.setValue(adjChunk, adjPos, expectedAdjValue);
                    if (expectedAdjValue - ChunkConstants.SUNLIGHT_REGEN_THRESHOLD > 0) {
                        adjChunk.setSunlight(adjPos, (byte) (expectedAdjValue - ChunkConstants.SUNLIGHT_REGEN_THRESHOLD));
                    }
                    queueSpreadRegen(adjChunk.getBlockWorldPos(adjPos), expectedAdjValue);
                }
            } else {
                byte expectedValue = regenRules.propagateValue(adjValue, side.reverse(), adjBlock);
                if (regenRules.canSpreadInto(block, side) && regenRules.canSpreadOutOf(adjBlock, side.reverse()) && value < expectedValue) {
                    regenRules.setValue(chunk, pos, expectedValue);
                    if (expectedValue - ChunkConstants.SUNLIGHT_REGEN_THRESHOLD > 0) {
                        chunk.setSunlight(pos, (byte) (expectedValue - ChunkConstants.SUNLIGHT_REGEN_THRESHOLD));
                    }
                    queueSpreadRegen(chunk.getBlockWorldPos(pos), expectedValue);
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
