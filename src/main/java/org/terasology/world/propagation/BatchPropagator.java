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

import com.google.common.collect.Sets;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Batch propagator that works on a set of changed blocks
 *
 * @author Immortius
 */
public class BatchPropagator {

    private static final byte NO_VALUE = 0;

    private PropagationRules rules;
    private PropagatorWorldView world;

    private Set<Vector3i>[] reduceQueues;
    private Set<Vector3i>[] increaseQueues;

    public BatchPropagator(PropagationRules rules, PropagatorWorldView world) {
        this.world = world;
        this.rules = rules;

        increaseQueues = new Set[rules.getMaxValue()];
        reduceQueues = new Set[rules.getMaxValue()];
        for (int i = 0; i < rules.getMaxValue(); ++i) {
            increaseQueues[i] = Sets.newLinkedHashSet();
            reduceQueues[i] = Sets.newLinkedHashSet();
        }

    }

    public void process(BlockChange... changes) {
        process(Arrays.asList(changes));
    }

    public void process(Iterable<BlockChange> blockChanges) {
        for (BlockChange blockChange : blockChanges) {
            reviewChange(blockChange);
        }

        processReduction();
        processIncrease();
        cleanUp();
    }

    public void propagateFrom(List<? extends Iterable<Vector3i>> fromRegions) {
        for (Iterable<Vector3i> region : fromRegions) {
            for (Vector3i pos : region) {
                byte val = world.getValueAt(pos);
                if (val > 0) {
                    queueSpreadValue(pos, val);
                }
            }
        }

        processIncrease();
        cleanUp();
    }

    private void reviewChange(BlockChange blockChange) {
        byte newValue = rules.getBlockValue(blockChange.getTo());
        byte existingValue = world.getValueAt(blockChange.getPosition());
        if (newValue > existingValue) {
            increase(blockChange.getPosition(), newValue);
        }

        byte oldValue = rules.getBlockValue(blockChange.getFrom());
        if (newValue < oldValue) {
            reduce(blockChange.getPosition(), oldValue);
        }

        for (Side side : Side.values()) {
            PropagationComparison comparison = rules.comparePropagation(blockChange.getTo(), blockChange.getFrom(), side);
            if (comparison.isRestricting() && existingValue > 0) {
                reduce(blockChange.getPosition(), existingValue);
                Vector3i adjPos = side.getAdjacentPos(blockChange.getPosition());
                if (world.isInBounds(adjPos)) {
                    byte adjValue = world.getValueAt(adjPos);
                    if (adjValue == rules.propagateValue(existingValue, side)) {
                        reduce(adjPos, adjValue);
                    }
                }
            } else if (comparison.isPermitting()) {
                if (existingValue > 0) {
                    queueSpreadValue(blockChange.getPosition(), existingValue);
                }
                Vector3i adjPos = side.getAdjacentPos(blockChange.getPosition());
                if (world.isInBounds(adjPos)) {
                    queueSpreadValue(adjPos, world.getValueAt(adjPos));
                }
            }
        }
    }

    private void processReduction() {
        int depth = 0;
        while (depth < rules.getMaxValue()) {
            byte oldValue = (byte) (rules.getMaxValue() - depth);
            Set<Vector3i> toProcess = reduceQueues[depth];
            reduceQueues[depth] = Sets.newLinkedHashSetWithExpectedSize(toProcess.size());

            for (Vector3i pos : toProcess) {
                purge(pos, oldValue);
            }
            if (reduceQueues[depth].isEmpty()) {
                depth++;
            }
        }
    }

    private void purge(Vector3i pos, byte oldValue) {
        Block block = world.getBlockAt(pos);
        increaseQueues[rules.getMaxValue() - oldValue].remove(pos);
        if (rules.getBlockValue(block) > 0) {
            increase(pos, rules.getBlockValue(block));
        } else {
            world.setValueAt(pos, NO_VALUE);
        }

        for (Side side : Side.values()) {
            byte expectedValue = rules.propagateValue(oldValue, side);
            Vector3i adjPos = side.getAdjacentPos(pos);
            if (world.isInBounds(adjPos) && rules.canSpreadOutOf(block, side)) {
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
            increaseQueues[depth] = Sets.newLinkedHashSetWithExpectedSize(toProcess.size());

            for (Vector3i pos : toProcess) {
                push(pos, value);
            }
            if (increaseQueues[depth].isEmpty()) {
                depth++;
            }
        }
    }

    private void push(Vector3i pos, byte value) {
        Block block = world.getBlockAt(pos);
        for (Side side : Side.values()) {
            byte spreadValue = rules.propagateValue(value, side);
            Vector3i adjPos = side.getAdjacentPos(pos);
            if (world.isInBounds(adjPos) && rules.canSpreadOutOf(block, side)) {
                byte adjValue = world.getValueAt(adjPos);
                if (adjValue < spreadValue) {
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

}
