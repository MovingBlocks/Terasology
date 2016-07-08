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
package org.terasology.rendering.dag;

import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;

/**
 *
 */
public class WireframeTask implements PipelineTask { // TODO: separate this into SetWireframeOnTask and SetWireframeOffTask?

    private final boolean enabled;

    public WireframeTask(StateChange<Boolean> stateChange) {
        this.enabled = stateChange.getValue();
    }

    @Override
    public void execute() {
        // TODO: "if(wireframeIsEnabledInRenderingDebugConfig) {" here?

        if (enabled) {
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        } else {
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }
    }
}
