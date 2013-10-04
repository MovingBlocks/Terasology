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
package org.terasology.world.lighting;

import com.google.common.collect.Sets;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;

import java.util.Arrays;
import java.util.Set;

/**
 * Lighting propagator that works on a set of changed blocks
 *
 * @author Immortius
 */
public class BulkLightPropagator {

    private static final int QUEUE_DEPTH = 15;
    private static final byte NO_LUMINANCE = 0;

    private LightingWorldView world;

    private Set<Vector3i>[] dimQueues;
    private Set<Vector3i>[] brightenQueues;

    public BulkLightPropagator(LightingWorldView world) {
        this.world = world;

        brightenQueues = new Set[QUEUE_DEPTH];
        dimQueues = new Set[QUEUE_DEPTH];
        for (int i = 0; i < QUEUE_DEPTH; ++i) {
            brightenQueues[i] = Sets.newLinkedHashSet();
            dimQueues[i] = Sets.newLinkedHashSet();
        }

    }

    public void process(BlockChange... changes) {
        process(Arrays.asList(changes));
    }

    public void process(Iterable<BlockChange> blockChanges) {
        for (BlockChange blockChange : blockChanges) {
            reviewChange(blockChange);
        }

        processDim();
        processBrighten();
        cleanUp();
    }

    private void reviewChange(BlockChange blockChange) {
        byte newBlockLuminance = blockChange.getTo().getLuminance();
        byte existingLuminance = world.getLuminanceAt(blockChange.getPosition());
        if (newBlockLuminance > existingLuminance) {
            brighten(blockChange.getPosition(), newBlockLuminance);
        }

        byte oldBlockLuminance = blockChange.getFrom().getLuminance();
        if (newBlockLuminance < oldBlockLuminance) {
            dim(blockChange.getPosition(), oldBlockLuminance);
        }

        for (Side side : Side.values()) {
            PropagationComparison comparison = LightingUtil.compareLightingPropagation(blockChange.getTo(), blockChange.getFrom(), side);
            if (comparison.isRestricting() && existingLuminance > 0) {
                dim(blockChange.getPosition(), existingLuminance);
                Vector3i adjPos = new Vector3i(blockChange.getPosition());
                adjPos.add(side.getVector3i());
                if (world.isInBounds(adjPos)) {
                    byte adjLuminance = world.getLuminanceAt(adjPos);
                    if (adjLuminance == existingLuminance - 1) {
                        dim(adjPos, adjLuminance);
                    }
                }
            } else if (comparison.isPermitting()) {
                if (existingLuminance > 0) {
                    queueSpreadLight(blockChange.getPosition(), existingLuminance);
                }
                Vector3i adjPos = new Vector3i(blockChange.getPosition());
                adjPos.add(side.getVector3i());
                if (world.isInBounds(adjPos)) {
                    queueSpreadLight(adjPos, world.getLuminanceAt(adjPos));
                }
            }
        }
    }

    private void processDim() {
        int depth = 0;
        while (depth < QUEUE_DEPTH) {
            byte oldLuminance = (byte) (15 - depth);
            Set<Vector3i> toProcess = dimQueues[depth];
            dimQueues[depth] = Sets.newLinkedHashSetWithExpectedSize(toProcess.size());

            for (Vector3i pos : toProcess) {
                purge(pos, oldLuminance);
            }
            if (dimQueues[depth].isEmpty()) {
                depth++;
            }
        }
    }

    private void purge(Vector3i pos, byte oldLuminance) {
        Block block = world.getBlockAt(pos);
        brightenQueues[15 - oldLuminance].remove(pos);
        if (block.getLuminance() > 0) {
            brighten(pos, block.getLuminance());
        } else {
            world.setLuminanceAt(pos, NO_LUMINANCE);
        }

        byte expectedLuminance = (byte) (oldLuminance - 1);
        for (Side side : Side.values()) {
            Vector3i adjPos = new Vector3i(pos);
            adjPos.add(side.getVector3i());
            if (world.isInBounds(adjPos) && LightingUtil.canSpreadLightOutOf(block, side)) {
                byte adjLuminance = world.getLuminanceAt(adjPos);
                if (adjLuminance == expectedLuminance) {
                    Block adjBlock = world.getBlockAt(adjPos);
                    if (LightingUtil.canSpreadLightInto(adjBlock, side.reverse())) {
                        dim(adjPos, expectedLuminance);
                    }
                } else if (adjLuminance > 0) {
                    queueSpreadLight(adjPos, adjLuminance);
                }
            }
        }
    }

    private void processBrighten() {
        int depth = 0;
        while (depth < QUEUE_DEPTH - 1) {
            byte luminance = (byte) (15 - depth);
            Set<Vector3i> toProcess = brightenQueues[depth];
            brightenQueues[depth] = Sets.newLinkedHashSetWithExpectedSize(toProcess.size());

            for (Vector3i pos : toProcess) {
                push(pos, luminance);
            }
            if (brightenQueues[depth].isEmpty()) {
                depth++;
            }
        }
    }

    private void push(Vector3i pos, byte luminance) {
        byte spreadLuminance = (byte) (luminance - 1);
        Block block = world.getBlockAt(pos);
        for (Side side : Side.values()) {
            Vector3i adjPos = new Vector3i(pos);
            adjPos.add(side.getVector3i());
            if (world.isInBounds(adjPos) && LightingUtil.canSpreadLightOutOf(block, side)) {
                byte adjLuminance = world.getLuminanceAt(adjPos);
                if (adjLuminance < spreadLuminance) {
                    Block adjBlock = world.getBlockAt(adjPos);
                    if (LightingUtil.canSpreadLightInto(adjBlock, side.reverse())) {
                        brighten(adjPos, spreadLuminance);
                    }
                }
            }
        }
    }

    private void cleanUp() {
        for (Set<Vector3i> queue : brightenQueues) {
            queue.clear();
        }
    }

    private void brighten(Vector3i position, byte luminance) {
        world.setLuminanceAt(position, luminance);
        queueSpreadLight(position, luminance);
    }

    private void queueSpreadLight(Vector3i position, byte luminance) {
        if (luminance > 1) {
            brightenQueues[15 - luminance].add(position);
        }
    }

    private void dim(Vector3i position, byte oldLuminance) {
        if (oldLuminance > 0) {
            dimQueues[15 - oldLuminance].add(position);
        }
    }

}
