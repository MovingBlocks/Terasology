// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import org.terasology.engine.rendering.dag.StateChange;

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
