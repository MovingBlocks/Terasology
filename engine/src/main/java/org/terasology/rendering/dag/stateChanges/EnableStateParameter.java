// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import org.terasology.engine.rendering.dag.StateChange;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.glEnable;

/**
 * TODO: Add javadocs
 */
abstract class EnableStateParameter implements StateChange {
    private int glParameter;

    EnableStateParameter(int glParameter) {
        this.glParameter = glParameter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(glParameter);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof EnableStateParameter) && (this.glParameter == ((EnableStateParameter) obj).glParameter);
    }

    @Override
    public String toString() {
        return String.format("%30s", this.getClass().getSimpleName());
    }

    @Override
    public void process() {
        glEnable(glParameter);
    }
}
