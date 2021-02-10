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

import org.terasology.rendering.dag.StateChange;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;

/**
 * Disables OpenGL depth testing.
 *
 * Notice that OpenGL has depth testing disabled by default. Terasology however enables it by default as depth
 * testing is used in many nodes. It's important then to use this StateChange if a node requires the OpenGL default.
 */
public final class DisableDepthTest extends DisableStateParameter {
    private static StateChange defaultInstance = new EnableDepthTest();

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new DisableDepthTest());
     */
    public DisableDepthTest() {
        super(GL_DEPTH_TEST);
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    private static class EnableDepthTest extends EnableStateParameter {
        EnableDepthTest() {
            super(GL_DEPTH_TEST);
        }

        @Override
        public StateChange getDefaultInstance() {
            return this;
        }
    }
}
