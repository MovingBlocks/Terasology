/*
 * Copyright 2017 MovingBlocks
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

import static org.lwjgl.opengl.GL11.GL_BLEND;
import org.terasology.rendering.dag.StateChange;

/**
 * Enables blending in the OpenGL pipeline.
 * Can be used to render transparent objects or to make a composite of different images.
 *
 * Note that SetBlendFunction can be used to set the source and destination factors used by the blending process.
 *
 * OpenGL Default: DisableBlending
 * Terasology Default: DisableBlending
 *
 * Type: Set
 * Corresponding Reset: DisableBlending
 */
public final class EnableBlending extends EnableStateParameter {
    private static StateChange defaultInstance = new DisableBlending();

    /**
     * Constructs an instance of this StateChange. This is can be used in a node's initialise() method in
     * the form:
     *
     * addDesiredStateChange(new EnableBlending());
     *
     * This trigger the inclusion of an EnableStateParameterTask instance and a DisableStateParameterTask instance
     * in the rendering task list, each instance enabling/disabling respectively the GL_BLEND mode. The
     * two task instance frame the execution of a node's process() method unless they are deemed redundant,
     * i.e. because the upstream or downstream node also enables blending.
     *
     * See also StateChange implementation SetBlendFunction to set the source and destination factors
     * used by the blending process.
     */
    public EnableBlending() {
        super(GL_BLEND);
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }
}
