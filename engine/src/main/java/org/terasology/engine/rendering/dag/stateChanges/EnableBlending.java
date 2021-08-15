// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import org.terasology.engine.rendering.dag.StateChange;

import static org.lwjgl.opengl.GL11.GL_BLEND;

/**
 * Enables OpenGL blending.
 * Blending can be used to render transparent objects on top of those already rendered in a buffer or to make a composite of different images.
 *
 * See also StateChange implementation SetBlendFunction to set the source and destination factors used by the blending process.
 *
 * By default both OpenGL and Terasology keep blending disabled as it is a relatively expensive operation.
 */
public final class EnableBlending extends EnableStateParameter {
    private static StateChange defaultInstance = new DisableBlending();

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new EnableBlending());
     */
    public EnableBlending() {
        super(GL_BLEND);
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    private static final class DisableBlending extends DisableStateParameter {
        DisableBlending() {
            super(GL_BLEND);
        }

        @Override
        public StateChange getDefaultInstance() {
            return this;
        }
    }
}
