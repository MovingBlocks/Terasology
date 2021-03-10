// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import com.google.common.collect.ImmutableMap;
import org.terasology.engine.rendering.dag.StateChange;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_DST_ALPHA;
import static org.lwjgl.opengl.GL11.GL_DST_COLOR;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_DST_ALPHA;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_DST_COLOR;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA_SATURATE;
import static org.lwjgl.opengl.GL11.GL_SRC_COLOR;
import static org.lwjgl.opengl.GL11.GL_ZERO;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL14.GL_CONSTANT_ALPHA;
import static org.lwjgl.opengl.GL14.GL_CONSTANT_COLOR;
import static org.lwjgl.opengl.GL14.GL_ONE_MINUS_CONSTANT_ALPHA;
import static org.lwjgl.opengl.GL14.GL_ONE_MINUS_CONSTANT_COLOR;

/**
 * Sets the blend function factors used by OpenGL.
 *
 * The OpenGL defaults are: source factor GL_ONE, destination factor GL_ZERO.
 *
 * See https://www.khronos.org/opengl/wiki/Blending for details.
 * Also see http://www.andersriggelsen.dk/glblendfunc.php to experiment with different factors.
 */
public class SetBlendFunction implements StateChange {
    private static final ImmutableMap<Integer, String> OGL_TO_STRING =
            ImmutableMap.<Integer, String>builder()
                .put(GL_ZERO, "GL_ZERO")
                .put(GL_ONE, "GL_ONE")
                .put(GL_SRC_COLOR, "GL_SRC_COLOR")
                .put(GL_ONE_MINUS_SRC_COLOR, "GL_ONE_MINUS_SRC_COLOR")
                .put(GL_DST_COLOR, "GL_DST_COLOR")
                .put(GL_ONE_MINUS_DST_COLOR, "GL_ONE_MINUS_DST_COLOR")
                .put(GL_SRC_ALPHA, "GL_SRC_ALPHA")
                .put(GL_ONE_MINUS_SRC_ALPHA, "GL_ONE_MINUS_SRC_ALPHA")
                .put(GL_DST_ALPHA, "GL_DST_ALPHA")
                .put(GL_ONE_MINUS_DST_ALPHA, "GL_ONE_MINUS_DST_ALPHA")
                .put(GL_CONSTANT_COLOR, "GL_CONSTANT_COLOR")
                .put(GL_ONE_MINUS_CONSTANT_COLOR, "GL_ONE_MINUS_CONSTANT_COLOR")
                .put(GL_CONSTANT_ALPHA, "GL_CONSTANT_ALPHA")
                .put(GL_ONE_MINUS_CONSTANT_ALPHA, "GL_ONE_MINUS_CONSTANT_ALPHA")
                .put(GL_SRC_ALPHA_SATURATE, "GL_SRC_ALPHA_SATURATE").build();

    private static SetBlendFunction defaultInstance = new SetBlendFunction(GL_ONE, GL_ZERO);

    private int sourceFactor;
    private int destinationFactor;

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new SetBlendFunction(GL_SRC_COLOR, GL_ONE_MINUS_DST_COLOR));
     *
     * @param sourceFactor An integer representing one of the possible blend factors known to OpenGL,
     *                      i.e. GL_ONE, GL_SRC_COLOR, etc...
     * @param destinationFactor An integer representing one of the possible blend factors known to OpenGL,
     *                      i.e. GL_ZERO, GL_DST_COLOR, etc...
     */
    public SetBlendFunction(int sourceFactor, int destinationFactor) {
        this.sourceFactor = sourceFactor;
        this.destinationFactor = destinationFactor;
    }

    /**
     * Returns a StateChange instance useful to reset the blend function back to OpenGL
     * default: source factor GL_ONE, destination factor GL_ZERO.
     *
     * @return the default instance of SetBlendFunction, cast as a StateChange instance.
     */
    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceFactor, destinationFactor);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetBlendFunction) && (this.sourceFactor == ((SetBlendFunction) obj).sourceFactor)
                                                 && (this.destinationFactor == ((SetBlendFunction) obj).destinationFactor);
    }

    @Override
    public String toString() {
        return String.format("%30s: %s, %s", this.getClass().getSimpleName(), OGL_TO_STRING.get(sourceFactor), OGL_TO_STRING.get(destinationFactor));
    }

    @Override
    public void process() {
        glBlendFunc(sourceFactor, destinationFactor);
    }
}
