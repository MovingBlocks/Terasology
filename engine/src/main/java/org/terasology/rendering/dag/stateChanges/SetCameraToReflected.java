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

import java.util.Objects;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.SetCameraReflectedMode;

/**
 * TODO
 */
public class SetCameraToReflected implements StateChange {

    private SetCameraToReflected defaultInstance;

    private Camera camera;
    private boolean reflected;

    private RenderPipelineTask task;

    public SetCameraToReflected(Camera camera) {
        this(camera, true);
        defaultInstance = new SetCameraToReflected(camera, false);
    }

    private SetCameraToReflected(Camera camera, boolean reflected) {
        this.camera = camera;
        this.reflected = reflected;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new SetCameraReflectedMode(camera, reflected);
        }
        return task;
    }

    @Override
    public int hashCode() {
        return Objects.hash(camera, reflected);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetCameraToReflected) && this.camera == ((SetCameraToReflected) obj).camera
                                                    && (this.reflected == ((SetCameraToReflected) obj).reflected);
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public boolean isTheDefaultInstance() {
        return this.equals(defaultInstance);
    }

}
