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
public class StandardBatchPropagator implements BatchPropagator {

    private static final byte NO_VALUE = 0;

    private PropagationRules rules;
    private PropagatorWorldView world;

    private Set<Vector3i>[] reduceQueues;
    private Set<Vector3i>[] increaseQueues;

    private Map<Side, Vector3i> chunkEdgeDeltas = Maps.newEnumMap(Side.class);

    public StandardBatchPropagator(PropagationRules rules, PropagatorWorldView world) {
        this.world = world;
        this.rules = rules;

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

        increaseQueues = new Set[rules.getMaxValue()];
        reduceQueues = new Set[rules.getMaxValue()];
        for (int i = 0; i < rules.getMaxValue(); ++i) {
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

        processReduction();
        processIncrease();
        cleanUp();
    }

    private void reviewChange(BlockChange blockChange) {
        byte newValue = rules.getFixedValue(blockChange.getTo(), blockChange.getPosition());
        byte existingValue = world.getValueAt(blockChange.getPosition());
        if (newValue > existingValue) {
            increase(blockChange.getPosition(), newValue);
        }

        byte oldValue = rules.getFixedValue(blockChange.getFrom(), blockChange.getPosition());
        if (newValue < oldValue) {
            reduce(blockChange.getPosition(), oldValue);
        }

        for (Side side : Side.values()) {
            PropagationComparison comparison = rules.comparePropagation(blockChange.getTo(), blockChange.getFrom(), side);
            if (comparison.isRestricting() && existingValue > 0) {
                reduce(blockChange.getPosition(), existingValue);
                Vector3i adjPos = side.getAdjacentPos(blockChange.getPosition());
                byte adjValue = world.getValueAt(adjPos);
                if (adjValue == rules.propagateValue(existingValue, side, blockChange.getFrom())) {
                    reduce(adjPos, adjValue);
                }
            } else if (comparison.isPermitting()) {
                if (existingValue > 0) {
                    queueSpreadValue(blockChange.getPosition(), existingValue);
                }
                Vector3i adjPos = side.getAdjacentPos(blockChange.getPosition());
                byte adjValue = world.getValueAt(adjPos);
                if (adjValue != PropagatorWorldView.UNAVAILABLE) {
                    queueSpreadValue(adjPos, adjValue);
                }
            }
        }
    }

    private void processReduction() {
        int depth = 0;
        while (depth < rules.getMaxValue()) {
            byte oldValue = (byte) (rules.getMaxValue() - depth);
            Set<Vector3i> toProcess = reduceQueues[depth];
            if (!toProcess.isEmpty()) {
                reduceQueues[depth] = Sets.newLinkedHashSetWithExpectedSize(toProcess.size());

                for (Vector3i pos : toProcess) {
                    purge(pos, oldValue);
                }
                if (toProcess.isEmpty()) {
                    depth++;
                }
            } else {
                depth++;
            }
        }
    }

    private void purge(Vector3i pos, byte oldValue) {
        Block block = world.getBlockAt(pos);
        increaseQueues[rules.getMaxValue() - oldValue].remove(pos);
        byte fixedValue = rules.getFixedValue(block, pos);
        if (fixedValue > 0) {
            increase(pos, fixedValue);
        } else {
            world.setValueAt(pos, NO_VALUE);
        }

        for (Side side : Side.values()) {
            byte expectedValue = rules.propagateValue(oldValue, side, block);
            Vector3i adjPos = side.getAdjacentPos(pos);
            if (rules.canSpreadOutOf(block, side)) {
                byte adjValue = world.getValueAt(adjPos);
                if (adjValue == expectedValue) {
                    Block adjBlock = world.getBlockAt(adjPos);
                    if (rules.canSpreadInto(adjBlock, side.reverse())) {
                        reduce(adjPos, expectedValue);
                    }
                } else if (adjValue > 0) {
                    queueSpreadValue(adjPos, adjValue);
                }
            }
        }
    }

    private void processIncrease() {
        int depth = 0;
        while (depth < rules.getMaxValue() - 1) {
            byte value = (byte) (rules.getMaxValue() - depth);
            Set<Vector3i> toProcess = increaseQueues[depth];
            if (!toProcess.isEmpty()) {
                increaseQueues[depth] = Sets.newLinkedHashSetWithExpectedSize(toProcess.size());

                for (Vector3i pos : toProcess) {
                    push(pos, value);
                }
                if (increaseQueues[depth].isEmpty()) {
                    depth++;
                }
            } else {
                depth++;
            }
        }
    }

    private void push(Vector3i pos, byte value) {
        Block block = world.getBlockAt(pos);
        for (Side side : Side.values()) {
            byte spreadValue = rules.propagateValue(value, side, block);
            Vector3i adjPos = side.getAdjacentPos(pos);
            if (rules.canSpreadOutOf(block, side)) {
                byte adjValue = world.getValueAt(adjPos);
                if (adjValue < spreadValue && adjValue != PropagatorWorldView.UNAVAILABLE) {
                    Block adjBlock = world.getBlockAt(adjPos);
                    if (rules.canSpreadInto(adjBlock, side.reverse())) {
                        increase(adjPos, spreadValue);
                    }
                }
            }
        }
    }

    private void cleanUp() {
        for (Set<Vector3i> queue : increaseQueues) {
            queue.clear();
        }
    }

    private void increase(Vector3i position, byte value) {
        world.setValueAt(position, value);
        queueSpreadValue(position, value);
    }

    private void queueSpreadValue(Vector3i position, byte value) {
        if (value > 1) {
            increaseQueues[rules.getMaxValue() - value].add(position);
        }
    }

    private void reduce(Vector3i position, byte oldValue) {
        if (oldValue > 0) {
            reduceQueues[rules.getMaxValue() - oldValue].add(position);
        }
    }

    @Override
    public void propagateBetween(ChunkImpl chunk, ChunkImpl adjChunk, Side side) {
        propagateSide(chunk, adjChunk, side);
        propagateSide(adjChunk, chunk, side.reverse());
    }

    private void propagateSide(ChunkImpl chunk, ChunkImpl adjChunk, Side side) {
        Region3i edgeRegion = TeraMath.getEdgeRegion(Region3i.createFromMinAndSize(Vector3i.zero(), ChunkConstants.CHUNK_SIZE), side);

        int edgeSize = edgeRegion.size().x * edgeRegion.size().y * edgeRegion.size().z;
        int[] depth = new int[edgeSize];

        Vector3i adjPos = new Vector3i();
        for (Vector3i pos : edgeRegion) {
            int depthIndex = getIndexFor(pos, side, edgeRegion);
            adjPos.set(pos);
            adjPos.add(chunkEdgeDeltas.get(side));
            Block lastBlock = chunk.getBlock(pos);
            byte expectedValue = rules.propagateValue(rules.getValue(chunk, pos), side, lastBlock);

            int depthCounter = 0;
            while (expectedValue > 0 && rules.canSpreadOutOf(lastBlock, side)) {
                Block currentBlock = adjChunk.getBlock(adjPos);
                if (rules.canSpreadInto(currentBlock, side.reverse()) && expectedValue > rules.getValue(adjChunk, adjPos)) {
                    lastBlock = currentBlock;
                    rules.setValue(adjChunk, adjPos, expectedValue);
                    adjPos.add(side.getVector3i());
                    depthCounter++;
                    expectedValue = rules.propagateValue(expectedValue, side, lastBlock);
                } else {
                    break;
                }
            }
            depth[depthIndex] = depthCounter;
        }
        for (Vector3i pos : edgeRegion) {
            int depthIndex = getIndexFor(pos, side, edgeRegion);
            int adjacentDepth = depth[depthIndex];

            for (Side adj : side.tangents()) {
                adjPos.set(pos);
                adjPos.add(adj.getVector3i());
                if (!ChunkConstants.CHUNK_REGION.encompasses(adjPos)) {
                    adjacentDepth = 0;
                    break;
                } else {
                    adjacentDepth = Math.min(adjacentDepth, depth[getIndexFor(adjPos, side, edgeRegion)]);
                }
            }

            for (int i = adjacentDepth; i < depth[depthIndex] - 1; ++i) {
                adjPos.set(side.getVector3i());
                adjPos.mult(i + 1);
                adjPos.add(pos);
                adjPos.add(chunkEdgeDeltas.get(side));
                if (!ChunkConstants.CHUNK_REGION.encompasses(adjPos)) {
                    break;
                }
                byte value = rules.getValue(adjChunk, adjPos);
                if (value > 1) {
                    queueSpreadValue(adjChunk.getBlockWorldPos(adjPos), value);
                }
            }
        }
    }

    private int getIndexFor(Vector3i pos, Side side, Region3i region) {
        switch (side) {
            case TOP:
            case BOTTOM:
                return pos.x + region.size().x * pos.z;
            case LEFT:
            case RIGHT:
                return pos.y + region.size().y * pos.z;
            default:
                return pos.x + region.size().x * pos.y;
        }
    }

    @Override
    public void propagateFrom(Vector3i pos, Block block) {
        queueSpreadValue(pos, rules.getFixedValue(block, pos));
    }

    @Override
    public void propagateFrom(Vector3i pos, byte value) {
        queueSpreadValue(pos, value);
    }

    @Override
    public void regenerate(Vector3i pos, byte value) {
        reduce(pos, value);
    }
}
