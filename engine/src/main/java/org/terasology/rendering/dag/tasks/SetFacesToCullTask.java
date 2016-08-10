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
package org.terasology.rendering.dag.tasks;

import org.lwjgl.opengl.GL11;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.stateChanges.SetFacesToCull;

/**
 * TODO: Add javadocs
 */
public final class SetFacesToCullTask implements RenderPipelineTask {
    private int mode;

    public SetFacesToCullTask(int mode) {
        this.mode = mode;
    }

    @Override
    public void execute() {
        GL11.glCullFace(mode);
    }

    @Override
    public String toString() {
        return String.format("%21s(%s)", this.getClass().getSimpleName(), SetFacesToCull.getModeName(mode));
    }
}
