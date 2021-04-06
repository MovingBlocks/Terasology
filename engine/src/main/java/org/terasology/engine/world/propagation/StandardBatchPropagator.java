// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Batch propagator that works on a set of changed blocks Works for a single given propagation ruleset
 */
public class StandardBatchPropagator implements BatchPropagator {

    private static final byte NO_VALUE = 0;

    private PropagationRules rules;
    private PropagatorWorldView world;
    private int scale;

    /* Queues are stored in reverse order. Ie, strongest light is 0. */
    private Set<Vector3ic>[] reduceQueues;
    private Set<Vector3ic>[] increaseQueues;

    private Map<Side, Vector3ic> chunkEdgeDeltas = Maps.newEnumMap(Side.class);

    public StandardBatchPropagator(PropagationRules rules, PropagatorWorldView world) {
        this(rules, world, 1);
    }

    public StandardBatchPropagator(PropagationRules rules, PropagatorWorldView world, int scale) {
        this.world = world;
        this.rules = rules;
        this.scale = scale;

        for (Side side : Side.getAllSides()) {
            Vector3i delta = new Vector3i(side.direction());
            if (delta.x < 0) {
                delta.x += Chunks.SIZE_X;
            } else if (delta.x > 0) {
                delta.x -= Chunks.SIZE_X;
            }
            if (delta.y < 0) {
                delta.y += Chunks.SIZE_Y;
            } else if (delta.y > 0) {
                delta.y -= Chunks.SIZE_Y;
            }
            if (delta.z < 0) {
                delta.z += Chunks.SIZE_Z;
            } else if (delta.z > 0) {
                delta.z -= Chunks.SIZE_Z;
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

    /**
     * Handles a single block being changed to a different type.
     *
     * @param blockChange The change that was made
     */
    private void reviewChange(BlockChange blockChange) {
        Vector3ic blockChangePosition = blockChange.getPosition();
        byte newValue = rules.getFixedValue(blockChange.getTo(), blockChangePosition);
        byte existingValue = world.getValueAt(blockChangePosition);

        /* Handle if the block has an higher fixed value */
        if (newValue > existingValue) {
            increase(blockChangePosition, newValue);
        }

        /* Handle if the block has a lower fixed value */
        byte oldValue = rules.getFixedValue(blockChange.getFrom(), blockChangePosition);
        if (newValue < oldValue) {
            reduce(blockChangePosition, oldValue);
        }

        /* Process propagation out to other blocks */
        Vector3i adjPos = new Vector3i();
        for (Side side : Side.getAllSides()) {
            PropagationComparison comparison = rules.comparePropagation(blockChange.getTo(), blockChange.getFrom(),
                    side);

            if (comparison.isRestricting() && existingValue > 0) {
                /* If the propagation of the new value is going to be lower/reduced */
                reduce(blockChangePosition, existingValue);
                side.getAdjacentPos(blockChangePosition, adjPos);
                byte adjValue = world.getValueAt(adjPos);
                if (adjValue == rules.propagateValue(existingValue, side, blockChange.getFrom(), scale)) {
                    reduce(adjPos, adjValue);
                }

            } else if (comparison.isPermitting()) {
                /* If the propagation of the new value is going to be more allowing */
                if (existingValue > 0) {
                    /* Spread this potentially higher value out */
                    queueSpreadValue(blockChangePosition, existingValue);
                }
                /* Spread it out to the block on the side */
                side.getAdjacentPos(blockChangePosition, adjPos);
                byte adjValue = world.getValueAt(adjPos);
                if (adjValue != PropagatorWorldView.UNAVAILABLE) {
                    queueSpreadValue(adjPos, adjValue);
                }
            }
        }
    }

    /**
     * Reset a position to only it's fixed values
     *
     * @param pos The position to reset
     * @param oldValue The value present before reset
     */
    private void purge(Vector3ic pos, byte oldValue) {
        increaseQueues[rules.getMaxValue() - oldValue].remove(pos);

        /* Clear the value and re-propagate it if it's a positive value */
        Block block = world.getBlockAt(pos);
        byte fixedValue = rules.getFixedValue(block, pos);
        if (fixedValue > 0) {
            increase(pos, fixedValue);
        } else {
            world.setValueAt(pos, NO_VALUE);
        }

        Vector3i adjPos = new Vector3i();
        for (Side side : Side.getAllSides()) {
            /* Handle this value being reset to the default by updating sides as needed */
            byte expectedValue = rules.propagateValue(oldValue, side, block, scale);
            if (rules.canSpreadOutOf(block, side)) {
                side.getAdjacentPos(pos, adjPos);
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

    /**
     * Process all reducing propagation requests This is done from the largest value through the smallest.
     */
    private void processReduction() {
        for (int depth = 0; depth < rules.getMaxValue(); depth++) {
            byte oldValue = (byte) (rules.getMaxValue() - depth);

            while (!reduceQueues[depth].isEmpty()) {
                Set<Vector3ic> toProcess = reduceQueues[depth];
                reduceQueues[depth] = Sets.newLinkedHashSetWithExpectedSize(toProcess.size());

                /* This step will add any new reductions to to the `reduceQueues` set */
                for (Vector3ic pos : toProcess) {
                    purge(pos, oldValue);
                }
            }
        }
    }

    /**
     * Process all increasing propagation requests This is done from the strongest through to the weakest.
     */
    private void processIncrease() {
        for (int depth = 0; depth < rules.getMaxValue() - 1; depth++) {
            byte value = (byte) (rules.getMaxValue() - depth);

            while (!increaseQueues[depth].isEmpty()) {
                Set<Vector3ic> toProcess = increaseQueues[depth];
                increaseQueues[depth] = Sets.newLinkedHashSetWithExpectedSize(toProcess.size());

                /* This step will add any new values to `increaseQueues` */
                for (Vector3ic pos : toProcess) {
                    push(pos, value);
                }
            }
        }

    }

    /**
     * Propagates a value from a position out into all adjacent blocks.
     * <p>
     * If the value spreading into a block is larger than the current value there, set it and queue it for propagating
     * again If the value is smaller than the current value, do nothing
     *
     * @param pos The initial position
     * @param value The value to propagate
     */
    private void push(Vector3ic pos, byte value) {
        Block block = world.getBlockAt(pos);
        Vector3i adjPos = new Vector3i();
        for (Side side : Side.getAllSides()) {
            byte propagatedValue = rules.propagateValue(value, side, block, scale);

            if (rules.canSpreadOutOf(block, side)) {
                side.getAdjacentPos(pos, adjPos);
                byte adjValue = world.getValueAt(adjPos);

                if (adjValue < propagatedValue && adjValue != PropagatorWorldView.UNAVAILABLE) {
                    Block adjBlock = world.getBlockAt(adjPos);

                    if (rules.canSpreadInto(adjBlock, side.reverse())) {
                        increase(adjPos, propagatedValue);
                    }
                }
            }
        }
    }

    /**
     * Set the value at a position to a new value. This should be larger than the prior value
     * <p>
     * Queues up this new higher value to be propagated out
     *
     * @param position The position to set at
     * @param value The value to set the position to
     */
    private void increase(Vector3ic position, byte value) {
        world.setValueAt(position, value);
        queueSpreadValue(position, value);
    }

    /**
     * Set the value at the position as having been lowered to a smaller value
     *
     * @param position The position to set at
     * @param oldValue The original value at the position
     */
    private void reduce(Vector3ic position, byte oldValue) {
        if (oldValue > 0) {
            reduceQueues[rules.getMaxValue() - oldValue].add(new Vector3i(position));
        }
    }

    /**
     * Queues up a propagation from a given position. Propagation is placed into a queue for the given level.
     *
     * @param position The position to propagate form
     * @param value The value to propagate out
     */
    private void queueSpreadValue(Vector3ic position, byte value) {
        if (value > 1) {
            increaseQueues[rules.getMaxValue() - value].add(new Vector3i(position));
        }
    }

    /**
     * Clears all the queues and cleans up the object
     */
    private void cleanUp() {
        for (Set<Vector3ic> queue : increaseQueues) {
            queue.clear();
        }
    }

    @Override
    public void propagateBetween(Chunk chunk, Chunk adjChunk, Side side, boolean propagateExternal) {
        Function<Vector3ic, Integer> indexProvider = createIndexProvider(side);

        BlockRegion edgeRegion = new BlockRegion(0, 0, 0)
                .setSize(Chunks.SIZE_X, Chunks.SIZE_Y, Chunks.SIZE_Z);
        edgeRegion.face(side, edgeRegion);

        int[] depth = new int[edgeRegion.volume()];

        propagateSide(chunk, adjChunk, side, indexProvider, edgeRegion, depth);
        propagateDepth(adjChunk, side, propagateExternal, indexProvider, edgeRegion, depth);
    }

    private void propagateDepth(Chunk adjChunk, Side side, boolean propagateExternal,
                                Function<Vector3ic, Integer> indexProvider,
                                BlockRegion edgeRegion, int[] depths) {
        Vector3i adjPos = new Vector3i();

        int[] adjDepth = new int[depths.length];
        int dimA = (side == Side.LEFT || side == Side.RIGHT) ? Chunks.SIZE_Y : Chunks.SIZE_X;
        int dimB = (side == Side.FRONT || side == Side.BACK) ? Chunks.SIZE_Y : Chunks.SIZE_Z;
        BatchPropagator.populateMinAdjacent2D(depths, adjDepth, dimA, dimB, !propagateExternal);

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

        for (Vector3ic pos : edgeRegion) {
            int depthIndex = indexProvider.apply(pos);
            int adjacentDepth = adjDepth[depthIndex];
            for (int i = adjacentDepth; i < depths[depthIndex]; ++i) {
                adjPos.set(side.direction());
                adjPos.mul(i + 1);
                adjPos.add(pos);
                adjPos.add(chunkEdgeDeltas.get(side));
                byte value = rules.getValue(adjChunk, adjPos);
                if (value > 1) {
                    queueSpreadValue(adjChunk.chunkToWorldPosition(adjPos, new Vector3i()), value);
                }
            }
        }
    }

    private void propagateSide(Chunk chunk, Chunk adjChunk, Side side, Function<Vector3ic, Integer> indexProvider,
                               BlockRegionc edgeRegion, int[] depths) {
        Vector3i adjPos = new Vector3i();
        for (Vector3ic pos : edgeRegion) {
            byte expectedValue = (byte) (rules.getValue(chunk, pos) - 1);
            if (expectedValue < 1) {
                continue;
            }

            pos.add(chunkEdgeDeltas.get(side), adjPos);

            int depthIndex = indexProvider.apply(pos);
            int depth = 0;
            Block lastBlock = chunk.getBlock(pos);
            byte adjValue = rules.getValue(adjChunk, adjPos);
            while (expectedValue > adjValue && adjValue != PropagatorWorldView.UNAVAILABLE && rules.canSpreadOutOf(lastBlock, side)) {
                lastBlock = adjChunk.getBlock(adjPos);
                if (rules.canSpreadInto(lastBlock, side.reverse())) {
                    rules.setValue(adjChunk, adjPos, expectedValue);
                    adjPos.add(side.direction());
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

    /**
     * Get the index provider appropriate for that side
     *
     * @param side The side to get the provider for
     * @return The provider for that side
     */
    private Function<Vector3ic, Integer> createIndexProvider(Side side) {
        switch (side) {
            case TOP:
            case BOTTOM:
                return pos -> pos.x() + Chunks.SIZE_X * pos.z();
            case LEFT:
            case RIGHT:
                return pos -> pos.y() + Chunks.SIZE_Y * pos.z();
            default:
                return pos -> pos.x() + Chunks.SIZE_X * pos.y();
        }
    }

    @Override
    public void propagateFrom(Vector3ic pos, Block block) {
        queueSpreadValue(pos, rules.getFixedValue(block, pos));
    }

    @Override
    public void propagateFrom(Vector3ic pos, byte value) {
        queueSpreadValue(pos, value);
    }

    @Override
    public void regenerate(Vector3ic pos, byte value) {
        reduce(pos, value);
    }
}
