// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import org.terasology.engine.rendering.dag.StateChange;

import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;

/**
 * Enables OpenGL's stencil testing.
 *
 * This can potentially be used in a variety of advanced computer graphics tricks such as stenciled shadows.
 */
public final class EnableStencilTest extends EnableStateParameter {
    private static StateChange defaultInstance = new DisableStencilTest();

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new EnableStencilTest());
     */
    public EnableStencilTest() {
        super(GL_STENCIL_TEST);
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    private static final class DisableStencilTest extends DisableStateParameter {
        DisableStencilTest() {
            super(GL_STENCIL_TEST);
        }

        @Override
        public StateChange getDefaultInstance() {
            return this;
        }
    }
}
