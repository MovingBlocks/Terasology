// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import org.lwjgl.opengl.GL11;
import org.terasology.engine.rendering.dag.StateChange;

import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;

/**
 * TODO: Add javadocs
 */
public final class SetFacesToCull implements StateChange {
    private static Map<Integer, String> modeNameMap = ImmutableMap.of(GL_BACK, "GL_BACK",
            GL_FRONT, "GL_FRONT",
            GL_FRONT_AND_BACK, "GL_FRONT_AND_BACK");

    private static SetFacesToCull defaultInstance = new SetFacesToCull(GL_BACK);

    private int mode;

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new SetFacesToCull(GL_FRONT));
     *
     * @param mode An integer representing one of the possible modes known to OpenGL,
     *                      i.e. GL_BACK, GL_FRONT or GL_FRONT_AND_BACK.
     */
    public SetFacesToCull(int mode) {
        if (mode == GL_BACK || mode == GL_FRONT || mode == GL_FRONT_AND_BACK) {
            this.mode = mode;
        } else {
            throw new IllegalArgumentException("Mode must be GL_BACK, GL_FRONT or GL_FRONT_AND_BACK.");
        }
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mode);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetFacesToCull) && mode == ((SetFacesToCull) obj).mode;
    }

    @Override
    public String toString() {
        return String.format("%30s: %s", this.getClass().getSimpleName(), modeNameMap.get(mode));
    }

    @Override
    public void process() {
        GL11.glCullFace(mode);
    }
}
