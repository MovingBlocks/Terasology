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
import org.lwjgl.opengl.GL11;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;

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
 * This StateChange generates the task that changes and resets the depth function during rendering.
 *
 * Notice that the function is reset to GL_LEQUAL (Terasology's default) rather than GL_LESS (OpenGL's default).
 */
public class SetDepthFunction implements StateChange {

    public static final ImmutableMap<Integer, String> OGL_TO_STRING =
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
    private RenderPipelineTask task;

    /**
     * Constructs an instance of SetDepthFunction initialised with the given
     * depth function.
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
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new SetDepthFunctionTask(depthFunction);
        }
        return task;
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

    /**
     * Instances of this class change the depth function used for the depth test while rendering.
     *
     * See glDepthFunct for more information.
     *
     * WARNING: RenderPipelineTasks are not meant for direct instantiation and manipulation.
     * Modules or other parts of the engine should take advantage of them through classes
     * inheriting from StateChange.
     */
    private class SetDepthFunctionTask implements RenderPipelineTask {
        private int depthFunction;

        /**
         * Constructs an instance of this class and initializes it with the given depth function.
         *
         * @param depthFunction An integer representing one of the depth functions known to OpenGL,
         *                      i.e. GL_LEQUAL (Terasology's default), GL_LESS (OpenGL default), etc.
         */
        private SetDepthFunctionTask(int depthFunction) {
            this.depthFunction = depthFunction;
        }

        @Override
        public void execute() {
            GL11.glDepthFunc(depthFunction);
        }

        @Override
        public String toString() {
            return String.format("%30s: %s", this.getClass().getSimpleName(), SetDepthFunction.OGL_TO_STRING.get(depthFunction));
        }
    }
}
