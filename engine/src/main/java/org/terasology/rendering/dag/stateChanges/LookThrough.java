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

import com.google.common.base.Objects;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.LookThroughDefaultCameraTask;
import org.terasology.rendering.dag.tasks.LookThroughTask;

/**
 * TODO
 */
public class LookThrough implements StateChange {

    private static LookThrough defaultInstance = new LookThrough();

    private Camera camera;
    private RenderPipelineTask task;

    public LookThrough(Camera camera) {
        this.camera = camera;
    }

    private LookThrough() {

    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            if (camera != null) {
                task = new LookThroughTask(camera);
            } else {
                task = new LookThroughDefaultCameraTask();
            }
        }
        return task;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(camera);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LookThrough) && camera == ((LookThrough) obj).camera;
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
