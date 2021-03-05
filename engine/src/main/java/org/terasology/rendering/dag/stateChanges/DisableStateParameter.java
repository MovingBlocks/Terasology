// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import org.terasology.engine.rendering.dag.StateChange;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.glDisable;

/**
 * TODO: Add javadocs
 */
abstract class DisableStateParameter implements StateChange {
    private int glParameter;

    DisableStateParameter(int glParameter) {
        this.glParameter = glParameter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(glParameter);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof DisableStateParameter) && (this.glParameter == ((DisableStateParameter) obj).glParameter);
    }

    @Override
    public String toString() {
        return String.format("%30s", this.getClass().getSimpleName());
    }

    @Override
    public void process() {
        glDisable(glParameter);
    }
}
