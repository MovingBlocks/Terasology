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
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.LitChunk;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * Batch propagator that works on a set of changed blocks
 *
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
    public void propagateBetween(LitChunk chunk, LitChunk adjChunk, Side side, boolean propagateExternal) {
        IndexProvider indexProvider = createIndexProvider(side);

        Region3i edgeRegion = ChunkMath.getEdgeRegion(Region3i.createFromMinAndSize(Vector3i.zero(), ChunkConstants.CHUNK_SIZE), side);

        int edgeSize = edgeRegion.size().x * edgeRegion.size().y * edgeRegion.size().z;
        int[] depth = new int[edgeSize];

        propagateSide(chunk, adjChunk, side, indexProvider, edgeRegion, depth);
        propagateDepth(adjChunk, side, propagateExternal, indexProvider, edgeRegion, depth);
    }

    private void propagateDepth(LitChunk adjChunk, Side side, boolean propagateExternal, IndexProvider indexProvider, Region3i edgeRegion, int[] depths) {
        Vector3i adjPos = new Vector3i();

        int[] adjDepth = new int[depths.length];
        int dimA = (side == Side.LEFT || side == Side.RIGHT) ? ChunkConstants.SIZE_Y : ChunkConstants.SIZE_X;
        int dimB = (side == Side.FRONT || side == Side.BACK) ? ChunkConstants.SIZE_Y : ChunkConstants.SIZE_Z;
        ChunkMath.populateMinAdjacent2D(depths, adjDepth, dimA, dimB, !propagateExternal);

        if (propagateExternal) {
            for (int y = 0; y < dimB; ++y) {
                adjDepth[y * dimA] = 0;
                adjDepth[dimA - 1 + y * dimA] = 0;
            }
            for (int x = 0; x < dimA; ++x) {
                adjDepth[x] = 0;
                adjDepth[x + dimA * (dimB - 1)] = 0;
            }
        }

        for (Vector3i pos : edgeRegion) {
            int depthIndex = indexProvider.getIndexFor(pos);
            int adjacentDepth = adjDepth[depthIndex];
            for (int i = adjacentDepth; i < depths[depthIndex]; ++i) {
                adjPos.set(side.getVector3i());
                adjPos.mul(i + 1);
                adjPos.add(pos);
                adjPos.add(chunkEdgeDeltas.get(side));
                byte value = rules.getValue(adjChunk, adjPos);
                if (value > 1) {
                    queueSpreadValue(adjChunk.chunkToWorldPosition(adjPos), value);
                }
            }
        }
    }

    private void propagateSide(LitChunk chunk, LitChunk adjChunk, Side side, IndexProvider indexProvider, Region3i edgeRegion, int[] depths) {
        Vector3i adjPos = new Vector3i();
        for (int x = edgeRegion.minX(); x <= edgeRegion.maxX(); ++x) {
            for (int y = edgeRegion.minY(); y <= edgeRegion.maxY(); ++y) {
                for (int z = edgeRegion.minZ(); z <= edgeRegion.maxZ(); ++z) {

                    int depthIndex = indexProvider.getIndexFor(x, y, z);
                    adjPos.set(x, y, z);
                    adjPos.add(chunkEdgeDeltas.get(side));

                    byte expectedValue = (byte) (rules.getValue(chunk, x, y, z) - 1);
                    if (expectedValue < 1) {
                        continue;
                    }

                    int depth = 0;
                    Block lastBlock = chunk.getBlock(x, y, z);
                    byte adjValue = rules.getValue(adjChunk, adjPos);
                    while (expectedValue > adjValue && adjValue != PropagatorWorldView.UNAVAILABLE && rules.canSpreadOutOf(lastBlock, side)) {
                        lastBlock = adjChunk.getBlock(adjPos);
                        if (rules.canSpreadInto(lastBlock, side.reverse())) {
                            rules.setValue(adjChunk, adjPos, expectedValue);
                            adjPos.add(side.getVector3i());
                            depth++;
                            expectedValue--;
                            adjValue = rules.getValue(adjChunk, adjPos);
                        } else {
                            break;
                        }
                    }
                    depths[depthIndex] = depth;
                }
            }
        }
    }

    private IndexProvider createIndexProvider(Side side) {
        IndexProvider indexProvider;
        switch (side) {
            case TOP:
            case BOTTOM:
                indexProvider = new IndexProvider() {
                    @Override
                    public int getIndexFor(Vector3i pos) {
                        return pos.x + ChunkConstants.SIZE_X * pos.z;
                    }

                    @Override
                    public int getIndexFor(int x, int y, int z) {
                        return x + ChunkConstants.SIZE_X * z;
                    }
                };
                break;
            case LEFT:
            case RIGHT:
                indexProvider = new IndexProvider() {
                    @Override
                    public int getIndexFor(Vector3i pos) {
                        return pos.y + ChunkConstants.SIZE_Y * pos.z;
                    }

                    @Override
                    public int getIndexFor(int x, int y, int z) {
                        return y + ChunkConstants.SIZE_Y * z;
                    }
                };
                break;
            default:
                indexProvider = new IndexProvider() {

                    @Override
                    public int getIndexFor(Vector3i pos) {
                        return pos.x + ChunkConstants.SIZE_X * pos.y;
                    }

                    @Override
                    public int getIndexFor(int x, int y, int z) {
                        return x + ChunkConstants.SIZE_X * y;
                    }
                };
                break;
        }
        return indexProvider;
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

    private interface IndexProvider {
        int getIndexFor(Vector3i pos);

        int getIndexFor(int x, int y, int z);
    }
}
