// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import org.terasology.engine.rendering.dag.StateChange;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;

/**
 * This StateChange resets the ModelView and Projection matrices to identity matrices, OpenGL's default.
 *
 * This StateChange is used to reset the effects of LookThrough or LookThroughNormalized.
 */
@Deprecated
class LookThroughDefault implements StateChange {
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LookThroughDefault);
    }

    @Override
    public int hashCode() {
        return LookThroughDefault.class.hashCode();
    }

    @Override
    public StateChange getDefaultInstance() {
        return this;
    }

    @Override
    public String toString() {
        return String.format("%30s", this.getClass().getSimpleName());
    }

    @Override
    public void process() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }
}
