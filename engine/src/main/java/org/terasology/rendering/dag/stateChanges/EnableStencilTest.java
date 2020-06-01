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

import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;

/**
 * Enables OpenGL's stencil testing.
 *
 * This can potentially be used in a variety of advanced computer graphics tricks such as stenciled shadows.
 */
public final class EnableStencilTest extends EnableStateParameter {
    private static StateChange defaultInstance = new DisableStencilTest();

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new EnableStencilTest());
     */
    public EnableStencilTest() {
        super(GL_STENCIL_TEST);
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    private static final class DisableStencilTest extends DisableStateParameter {
        DisableStencilTest() {
            super(GL_STENCIL_TEST);
        }

        @Override
        public StateChange getDefaultInstance() {
            return this;
        }
    }
}
