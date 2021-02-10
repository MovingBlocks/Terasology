/*
 * Copyright 2016 MovingBlocks
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

import static org.lwjgl.opengl.GL11.glDepthMask;

/**
 * Disables OpenGL's writing to the depth buffer.
 *
 * This can be useful when rendering semi-transparent objects, as the meaning of the depth value of a fragment
 * associated with a semi-transparent object is ambiguous and therefore has to be chosen arbitrarily:
 * should it be the object's distance from the near plane or should it be the first thing behind it?
 */
public final class DisableDepthWriting implements StateChange {
    private static StateChange defaultInstance = new EnableDepthWriting();

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof DisableDepthWriting);
    }

    @Override
    public int hashCode() {
        return DisableDepthWriting.class.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%30s", this.getClass().getSimpleName());
    }

    @Override
    public void process() {
        glDepthMask(false);
    }

    private static final class EnableDepthWriting implements StateChange {
        @Override
        public StateChange getDefaultInstance() {
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof EnableDepthWriting);
        }

        @Override
        public int hashCode() {
            return EnableDepthWriting.class.hashCode();
        }

        @Override
        public String toString() {
            return String.format("%30s", this.getClass().getSimpleName());
        }

        @Override
        public void process() {
            glDepthMask(true);
        }
    }
}
