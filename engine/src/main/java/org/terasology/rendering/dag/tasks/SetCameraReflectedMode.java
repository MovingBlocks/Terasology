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
 * TODO
 */
public class SetCameraReflectedMode implements RenderPipelineTask {

    private Camera camera;
    private boolean reflected;

    public SetCameraReflectedMode(Camera camera, boolean reflected) {
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
