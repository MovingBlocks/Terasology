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

// TODO: implement bobbing via multiple cameras and different steady/bobbing attachment points
/**
 * Instances of this class set the ModelView and Projection matrices so that
 * the scene can be seen through the camera provided on construction.
 *
 * Differently from the LookThroughNormalizedTask, the view from the given
 * camera will bob up and down if bobbing is enabled.
 *
 * WARNING: RenderPipelineTasks are not meant for direct instantiation and manipulation.
 * Modules or other parts of the engine should take advantage of them through classes
 * inheriting from StateChange.
 */
public class LookThroughTask implements RenderPipelineTask {

    private Camera camera;

    /**
     * Constructs an instance of this class initialized with the given camera.
     *
     * @param camera an instance implementing the Camera interface
     */
    public LookThroughTask(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void execute() {
        camera.lookThrough();
    }

    @Override
    public String toString() {
        return String.format("%30s: %s", this.getClass().getSimpleName(), camera.toString());
    }
}
