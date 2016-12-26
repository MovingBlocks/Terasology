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

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.DisableStateParameterTask;
import org.terasology.rendering.dag.tasks.EnableStateParameterTask;

/**
 * Instances of this class enable OpenGL's Face Culling, i.e. to render only the inside faces of the skysphere.
 *
 * Notice that Terasology's by default enables face culling. However, the rendering engine disables it again
 * every frame to be consistent with OpenGL's defaults. This is debatable and might change in the future.
 *
 * Also see SetFacesToCull, which allows deviation from OpenGL's default of culling only the GL_BACK faces.
 */
public final class EnableFaceCulling extends SetStateParameter {
    private static final int PARAMETER = GL_CULL_FACE;
    private static final String PARAMETER_NAME = "GL_CULL_FACE";
    private static StateChange defaultInstance = new EnableFaceCulling(false);
    private static RenderPipelineTask enablingTask;
    private static RenderPipelineTask disablingTask;

    /**
     * Constructs an instance of this StateChange. This is can be used in a node's initialise() method in
     * the form:
     *
     * addDesiredStateChange(new EnableFaceCulling());
     *
     * This trigger the inclusion of an EnableStateParameterTask instance and a DisableStateParameterTask instance
     * in the rendering task list, each instance enabling/disabling respectively the GL_CULL_FACE mode. The
     * two task instance frame the execution of a node's process() method unless they are deemed redundant,
     * i.e. because the upstream or downstream node also enables face culling.
     *
     * See StateChange implementation SetFacesToCull to change from OpenGL's default of culling only the GL_BACK faces.
     */
    public EnableFaceCulling() {
        this(true);
    }

    private EnableFaceCulling(boolean enabled) {
        super(GL_CULL_FACE, enabled);
        disablingTask = new DisableStateParameterTask(PARAMETER, PARAMETER_NAME);
        enablingTask = new EnableStateParameterTask(PARAMETER, PARAMETER_NAME);
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
        return this.equals(defaultInstance);
    }

}
