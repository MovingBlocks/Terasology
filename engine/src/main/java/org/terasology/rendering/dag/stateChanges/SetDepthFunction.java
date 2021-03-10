// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import com.google.common.collect.ImmutableMap;
import org.lwjgl.opengl.GL11;
import org.terasology.engine.rendering.dag.StateChange;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_ALWAYS;
import static org.lwjgl.opengl.GL11.GL_EQUAL;
import static org.lwjgl.opengl.GL11.GL_GEQUAL;
import static org.lwjgl.opengl.GL11.GL_GREATER;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.GL_NEVER;
import static org.lwjgl.opengl.GL11.GL_NOTEQUAL;

/**
 * Sets the depth function during rendering.
 *
 * Notice that the function is reset to GL_LEQUAL (Terasology's default) rather than GL_LESS (OpenGL's default).
 */
public class SetDepthFunction implements StateChange {
    private static final ImmutableMap<Integer, String> OGL_TO_STRING =
            ImmutableMap.<Integer, String>builder()
                    .put(GL_NEVER, "GL_NEVER")
                    .put(GL_LESS, "GL_LESS")
                    .put(GL_EQUAL, "GL_EQUAL")
                    .put(GL_LEQUAL, "GL_LEQUAL")
                    .put(GL_GREATER, "GL_GREATER")
                    .put(GL_NOTEQUAL, "GL_NOTEQUAL")
                    .put(GL_GEQUAL, "GL_GEQUAL")
                    .put(GL_ALWAYS, "GL_ALWAYS").build();

    private static SetDepthFunction defaultInstance = new SetDepthFunction(GL_LEQUAL);

    private int depthFunction;

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new SetDepthFunction(GL_ALWAYS));
     *
     * @param depthFunction An integer representing one of the possible depth function known to OpenGL,
     *                      i.e. GL_LEQUAL, GL_ALWAYS, etc...
     */
    public SetDepthFunction(int depthFunction) {
        this.depthFunction = depthFunction;
    }

    /**
     * Returns a StateChange instance useful to reset the depth function back to Terasology's
     * default: GL_LEQUAL. Notice this is different than OpenGL's default: GL_LESS.
     *
     * @return the default instance of SetDepthFunction, cast as a StateChange instance.
     */
    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(depthFunction);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetDepthFunction) && (this.depthFunction == ((SetDepthFunction) obj).depthFunction);
    }

    @Override
    public String toString() {
        return String.format("%30s: %s", this.getClass().getSimpleName(), OGL_TO_STRING.get(depthFunction));
    }

    @Override
    public void process() {
        GL11.glDepthFunc(depthFunction);
    }
}
