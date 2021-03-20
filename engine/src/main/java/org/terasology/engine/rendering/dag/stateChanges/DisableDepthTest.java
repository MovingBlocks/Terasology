// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import org.terasology.engine.rendering.dag.StateChange;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;

/**
 * Disables OpenGL depth testing.
 *
 * Notice that OpenGL has depth testing disabled by default. Terasology however enables it by default as depth
 * testing is used in many nodes. It's important then to use this StateChange if a node requires the OpenGL default.
 */
public final class DisableDepthTest extends DisableStateParameter {
    private static StateChange defaultInstance = new EnableDepthTest();

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new DisableDepthTest());
     */
    public DisableDepthTest() {
        super(GL_DEPTH_TEST);
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    private static class EnableDepthTest extends EnableStateParameter {
        EnableDepthTest() {
            super(GL_DEPTH_TEST);
        }

        @Override
        public StateChange getDefaultInstance() {
            return this;
        }
    }
}
