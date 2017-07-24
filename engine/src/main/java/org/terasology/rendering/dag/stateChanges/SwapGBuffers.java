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
 * This StateChange is used to swap the readOnly and writeOnly buffers in the gBufferPair.
 * This is useful for Nodes that want to update the value of the gBuffer, by passing it's contents through a shader.
 *
 * Note that this StateChange is special because unlike all the other StateChanges that modify the state of the
 * system during the execution of the task list, this StateChange does it's job by indirectly changing what data gets
 * sent to future StateChanges, by causing a change in the return value of gBufferPair.get*Buffer().
 *
 * Due to this, this StateChange -has- to be added before any calls to gBuferPair.get*Buffer() in the Node.
 */
public class SwapGBuffers implements StateChange {
    private static StateChange defaultInstance = new EmptyStateChange();

    public SwapGBuffers(SwappableFBO gBufferPair) {
        gBufferPair.swap();
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    // TODO: Add .hashCode()

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public String toString() {
        return String.format("%30s", this.getClass().getSimpleName());
    }

    @Override
    public void process() { }

    private static final class EmptyStateChange implements StateChange {
        @Override
        public StateChange getDefaultInstance() {
            return this;
        }

        // TODO: Add .hashCode()

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof EmptyStateChange);
        }

        @Override
        public String toString() {
            return "Empty State Change";
        }

        @Override
        public void process() { }
    }
}
