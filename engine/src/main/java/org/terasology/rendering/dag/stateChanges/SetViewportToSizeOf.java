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

import org.terasology.assets.ResourceUrn;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.opengl.BaseFBOsManager;
import org.terasology.rendering.opengl.FBOManagerSubscriber;
import java.util.Objects;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.SetViewportToSizeOfTask;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.SCENE_OPAQUE;

/**
 * TODO: Add javadocs
 */
public final class SetViewportToSizeOf implements FBOManagerSubscriber, StateChange {
    private static SetViewportToSizeOf defaultInstance;

    private BaseFBOsManager frameBuffersManager;
    private SetViewportToSizeOfTask task;
    private ResourceUrn fboName;

    public SetViewportToSizeOf(ResourceUrn fboName, BaseFBOsManager frameBuffersManager) {
        this.frameBuffersManager = frameBuffersManager;
        this.fboName = fboName;
    }

    @Override
    public StateChange getDefaultInstance() {
        // VAMPCAT : TODO: Try initializing while declaring instead of doing it here
        if (defaultInstance == null)
            defaultInstance = new SetViewportToSizeOf(SCENE_OPAQUE, CoreRegistry.get(DisplayResolutionDependentFBOs.class));
        return defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new SetViewportToSizeOfTask(fboName);
            frameBuffersManager.subscribe(this);
            update();
        }

        return task;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFbo().width(), getFbo().height());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SetViewportToSizeOf))
            return false;

        SetViewportToSizeOf other = (SetViewportToSizeOf) obj;

        return getFbo().width() == other.getFbo().width() && getFbo().height() == other.getFbo().height();
    }

    @Override
    public void update() {
        task.setDimensions(getFbo().width(), getFbo().height());
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return String.format("%30s: %s", this.getClass().getSimpleName(), fboName);
    }

    private FBO getFbo() {
        //VAMPCAT : Maybe store the fbo in the class, instead of fetching every time?
        return frameBuffersManager.get(fboName);
    }
}
