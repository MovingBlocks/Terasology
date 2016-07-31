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
import org.terasology.rendering.dag.tasks.EnableCapabilityTask;

/**
 * TODO: Add javadocs
 */
public class EnableCapability implements StateChange {
    private int capability;
    private EnableCapabilityTask task;

    public EnableCapability(int capability) {
        this.capability = capability;
    }

    public int getCapability() {
        return capability;
    }

    @Override
    public StateChange getDefaultInstance() {
        return null;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new EnableCapabilityTask(capability);
        }
        return task;
    }

    @Override
    public boolean isEqualTo(StateChange stateChange) {
        if (stateChange instanceof EnableCapability) {
            return capability == ((EnableCapability) stateChange).getCapability();
        }
        return false;
    }

    @Override
    public boolean isTheDefaultInstance() {
        return false;
    }
}
