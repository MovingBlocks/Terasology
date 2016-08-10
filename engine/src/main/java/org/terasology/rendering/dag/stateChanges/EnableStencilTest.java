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

import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.DisableStateParameterTask;
import org.terasology.rendering.dag.tasks.EnableStateParameterTask;

/**
 * TODO: Add javadocs
 */
public class EnableStencilTest extends SetStateParameter {
    private static final int PARAMETER = GL_STENCIL_TEST;
    private static StateChange defaultInstance = new EnableStencilTest(false);
    private static RenderPipelineTask enablingTask;
    private static RenderPipelineTask disablingTask;

    public EnableStencilTest() {
        this(true);
    }

    private EnableStencilTest(boolean enabled) {
        super(enabled);
        disablingTask = new DisableStateParameterTask(PARAMETER);
        enablingTask = new EnableStateParameterTask(PARAMETER);
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    protected RenderPipelineTask getDisablingTask() {
        return disablingTask;
    }

    @Override
    protected RenderPipelineTask getEnablingTask() {
        return enablingTask;
    }

    @Override
    public boolean isTheDefaultInstance() {
        return this == defaultInstance;
    }

    @Override
    public String toString() {
        return String.format("%s%s", this.getClass().getSimpleName(), super.toString());
    }
}
