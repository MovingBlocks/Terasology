// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import org.lwjgl.opengl.GL11;
import org.terasology.engine.rendering.dag.StateChange;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;

/**
 * TODO: Add javadocs
 */
public final class SetWireframe implements StateChange {

    private static SetWireframe defaultInstance = new SetWireframe(false);

    private boolean enabled;
    private int mode;

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new SetWireframe(true);
     *
     * @param enabled a boolean indicating whether the Wireframe should be enabled
     */
    public SetWireframe(boolean enabled) {
        this.enabled = enabled;
        this.mode = enabled ? GL_LINE : GL_FILL;
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(enabled);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetWireframe) && this.enabled == ((SetWireframe) obj).enabled;
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return String.format("%30s: %b", this.getClass().getSimpleName(), enabled);
    }

    @Override
    public void process() {
        GL11.glPolygonMode(GL_FRONT_AND_BACK, mode);
    }
}
