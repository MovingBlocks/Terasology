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

import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.RenderPipelineTask;

/**
 * Instances of this task set and unset the reflected flag of a camera.
 *
 * A reflected camera is helpful to render a reflected scene, which can then be used to render reflective surfaces.
 */
public class SetCameraReflectedModeTask implements RenderPipelineTask {

    private Camera camera;
    private boolean reflected;

    /**
     * Constructs an instance of this class, setting or resetting the reflected flag on the given camera.
     *
     * @param camera an instance implementing the Camera interface
     * @param reflected a boolean determining if the camera should be set to reflected or not.
     */
    public SetCameraReflectedModeTask(Camera camera, boolean reflected) {
        this.camera = camera;
        this.reflected = reflected;
    }

    @Override
    public void execute() {
        camera.setReflected(reflected);
    }

    @Override
    public String toString() {
        return String.format("%30s: %s for %s", this.getClass().getSimpleName(), reflected ? "true" : "false", camera.toString());
    }

}
