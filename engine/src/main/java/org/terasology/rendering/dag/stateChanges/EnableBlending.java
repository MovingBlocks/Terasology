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

import org.terasology.rendering.dag.StateChange;

import static org.lwjgl.opengl.GL11.GL_BLEND;

/**
 * Enables OpenGL blending.
 * Blending can be used to render transparent objects on top of those already rendered in a buffer or to make a composite of different images.
 *
 * See also StateChange implementation SetBlendFunction to set the source and destination factors used by the blending process.
 *
 * By default both OpenGL and Terasology keep blending disabled as it is a relatively expensive operation.
 */
public final class EnableBlending extends EnableStateParameter {
    private static StateChange defaultInstance = new DisableBlending();

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new EnableBlending());
     */
    public EnableBlending() {
        super(GL_BLEND);
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    private static final class DisableBlending extends DisableStateParameter {
        DisableBlending() {
            super(GL_BLEND);
        }

        @Override
        public StateChange getDefaultInstance() {
            return this;
        }
    }
}
