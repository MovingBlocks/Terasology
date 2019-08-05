/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.dag.stateChanges;

import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.opengl.SwappableFBO;

/**
 * This StateChange is used to swap the lastUpdated and the stale FBOs in the gBufferPair.
 *
 * This is useful for Nodes that want to do rendering operations requiring the latest content stored in the
 * gBufferPair, to be used as inputs for a shader.
 * For instance, once a node has read from the lastUpdatedGBuffer and written to the staleGBuffer,
 * it will probably want to swap the buffers so that the "stale" gBuffer becomes the "lastUpdated"
 * one and the next node can rely on it.
 * There can be exceptions though, as consecutive nodes might all write to the lastUpdatedGuffer, requiring no swaps.
 *
 * Note that this StateChange is special because unlike all the other StateChanges that modify the state of the
 * system during the execution of the task list, this StateChange does its job on construction and therefore
 * during the _generation_ of the task list.
 *
 * This is done by causing a change in the return value of gBufferPair.get*Buffer().
 * As a consequence this StateChange -must- be added after all calls to gBuferPair.get*Buffer() in the Node.
 *
 * Note that theoretically a very similar result could have been achieved with a method available to all rendering
 * nodes. However, a state change adds an item to the task list. This way when the task list is printed out for
 * debugging it's easy to see where the swap occurs.
 */
public class SwapGBuffers implements StateChange {
    private static StateChange defaultInstance;

    public SwapGBuffers(SwappableFBO gBufferPair) {
        gBufferPair.swap();
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return SwapGBuffers.class.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%30s", this.getClass().getSimpleName());
    }

    @Override
    public void process() { }
}
