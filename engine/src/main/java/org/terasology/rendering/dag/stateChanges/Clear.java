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

import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.ClearTask;

/**
 * TODO: Add javadocs
 * TODO: A defensive programming suggestion: eliminate stopping reduction of redundant Clear state changes for not
 * TODO: changing the final image. (New visual artifacts might be introduced because of this.)
 * TODO: Another suggestion: Moving clear inside an FBO, since it's an FBO State Change ?
 */
public class Clear implements StateChange {
    private final boolean colorBufferMask;

    private final boolean depthBufferMask;
    private final boolean stencilBufferMask;
    private ClearTask task;

    public Clear(boolean colorBufferMask, boolean depthBufferMask, boolean stencilBufferMask) {
        this.colorBufferMask = colorBufferMask;
        this.depthBufferMask = depthBufferMask;
        this.stencilBufferMask = stencilBufferMask;
    }

    public boolean isColorBufferMask() {
        return colorBufferMask;
    }

    public boolean isDepthBufferMask() {
        return depthBufferMask;
    }

    public boolean isStencilBufferMask() {
        return stencilBufferMask;
    }

    @Override
    public StateChange getDefaultInstance() {
        return null;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new ClearTask(colorBufferMask, depthBufferMask, stencilBufferMask);
        }

        return task;
    }

    @Override
    public boolean isEqualTo(StateChange stateChange) {
        if (stateChange instanceof Clear) {
            Clear instance = (Clear) stateChange;
            return (colorBufferMask == instance.isColorBufferMask())
                    && (depthBufferMask == instance.isDepthBufferMask())
                    && (stencilBufferMask == instance.isStencilBufferMask());
        }

        return false;
    }

    @Override
    public boolean isTheDefaultInstance() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("%21s(%s, %s, %s)",
                this.getClass().getSimpleName(),
                colorBufferMask,
                depthBufferMask,
                stencilBufferMask);
    }
}
