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

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import org.terasology.rendering.dag.StateChange;

/**
 * Instances of this class disable OpenGL's depth test, allowing the processing of fragments
 * that would normally fail the test and would therefore be discarded.
 *
 * Notice that by default OpenGL has depth testing disabled but in Terasology's defaults it is enabled,
 * as depth testing is used by many nodes. It's important then to use this StateChange so that each
 * node requiring no depth testing disables it and re-enables it after the process() method has been
 * executed.
 */
public final class DisableDepthTest extends EnableStateParameter {
    private static StateChange defaultInstance = new EnableDepthTest();

    /**
     * Constructs an instance of this StateChange. This is can be used in a node's initialise() method in
     * the form:
     *
     * addDesiredStateChange(new DisableDepthTest());
     *
     * This trigger the inclusion of a DisableStateParameterTask instance and an EnableStateParameterTask instance
     * in the rendering task list, each instance disabling/enabling respectively the GL_DEPTH_TEST mode. The
     * two task instance frame the execution of a node's process() method unless they are deemed redundant,
     * i.e. because the upstream or downstream node also disables the depth testing.
     */
    public DisableDepthTest() {
        super(GL_DEPTH_TEST);
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }
}
