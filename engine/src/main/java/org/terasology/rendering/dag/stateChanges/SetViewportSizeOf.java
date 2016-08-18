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
import org.terasology.rendering.opengl.BaseFBOsManager;
import org.terasology.rendering.opengl.DefaultDynamicFBOs;
import org.terasology.rendering.opengl.FBOManagerSubscriber;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.SetViewportSizeOfTask;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

/**
 * TODO: Add javadocs
 */
public final class SetViewportSizeOf implements FBOManagerSubscriber, StateChange {
    private static SetViewportSizeOf defaultInstance = new SetViewportSizeOf(DefaultDynamicFBOs.READ_ONLY_GBUFFER.getName());
    private static DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private BaseFBOsManager frameBuffersManager;
    private SetViewportSizeOfTask task;
    private ResourceUrn fboName;

    public SetViewportSizeOf(ResourceUrn fboName, BaseFBOsManager frameBuffersManager) {
        this.frameBuffersManager = frameBuffersManager;
        this.fboName = fboName;
    }

    private SetViewportSizeOf(ResourceUrn fboName) {
        this.fboName = fboName;
    }

    public static void setDisplayResolutionDependentFBOs(DisplayResolutionDependentFBOs displayResolutionDependentFBOs) {
        SetViewportSizeOf.displayResolutionDependentFBOs = displayResolutionDependentFBOs;
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            if (isTheDefaultInstance()) {
                frameBuffersManager = displayResolutionDependentFBOs;
            }
            task = new SetViewportSizeOfTask(fboName);
            frameBuffersManager.subscribe(this);
            update();
        }

        return task;
    }

    @Override
    public boolean isEqualTo(StateChange stateChange) {
        if (stateChange instanceof SetViewportSizeOf) {
            return this.fboName.equals(((SetViewportSizeOf) stateChange).getFboName());
        }
        return false;
    }

    @Override
    public boolean isTheDefaultInstance() {
        return this == defaultInstance;
    }

    @Override
    public void update() {
        FBO fbo = frameBuffersManager.get(fboName);
        task.setDimensions(fbo.width(), fbo.height());
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return String.format("%21s: %s", this.getClass().getSimpleName(), fboName);
    }

    public ResourceUrn getFboName() {
        return fboName;
    }
}
