/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.dag.stateChanges;

import com.google.common.collect.ImmutableMap;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.SetBlendFunctionTask;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;

/**
 * This StateChange generates the tasks that change and reset the blend function factors.
 *
 * The OpenGL defaults are: source factor GL_ONE, destination factor GL_ZERO.
 */
public class SetBlendFunction implements StateChange {

    public static final ImmutableMap<Integer, String> OGL_TO_STRING =
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
    private RenderPipelineTask task;

    /**
     * Constructs an instance of SetBlendFunction initialised with the given blend function factors.
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
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new SetBlendFunctionTask(sourceFactor, destinationFactor);
        }
        return task;
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
    public boolean isTheDefaultInstance() {
        return this.equals(defaultInstance);
    }

    @Override
    public String toString() {
        return String.format("%30s: %s, %s", this.getClass().getSimpleName(), OGL_TO_STRING.get(sourceFactor), OGL_TO_STRING.get(destinationFactor));
    }

}
