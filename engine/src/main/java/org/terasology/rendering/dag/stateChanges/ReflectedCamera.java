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
import org.terasology.rendering.dag.tasks.SetCameraReflectedModeTask;

/**
 * Instances of this state change generate the tasks that set or reset the reflected flag of a given camera.
 *
 * Warning: instances of this class -must- be added to the list of desired state changes in a node
 * -before- any instance of LookThrough and LookThroughNormalized.
 */
public class ReflectedCamera implements StateChange {

    private ReflectedCamera defaultInstance;

    private Camera camera;
    private boolean reflected;

    private RenderPipelineTask task;

    /**
     * Constructs an instance of this class initialized with a given Camera instance.
     *
     * @param camera An instance implementing the Camera interface.
     */
    public ReflectedCamera(Camera camera) {
        this(camera, true);
        defaultInstance = new ReflectedCamera(camera, false);
    }

    private ReflectedCamera(Camera camera, boolean reflected) {
        this.camera = camera;
        this.reflected = reflected;
    }

    /**
     * @return a RenderPipelineTask configured to set or reset the reflection flag of the camera given on construction.
     */
    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new SetCameraReflectedModeTask(camera, reflected);
        }
        return task;
    }

    @Override
    public int hashCode() {
        return Objects.hash(camera, reflected);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ReflectedCamera) && this.camera == ((ReflectedCamera) obj).camera
                                                     && (this.reflected == ((ReflectedCamera) obj).reflected);
    }

    /**
     * Returns an instance of this class configured to generate a task resetting
     * the reflected flag of the camera provided on construction.
     *
     * @return the default instance of ReflectedCamera
     */
    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public boolean isTheDefaultInstance() {
        return this.equals(defaultInstance);
    }

    @Override
    public String toString() {
        return String.format("%30s: %s for %s", this.getClass().getSimpleName(), reflected ? "true" : "false", camera.toString());
    }
}
