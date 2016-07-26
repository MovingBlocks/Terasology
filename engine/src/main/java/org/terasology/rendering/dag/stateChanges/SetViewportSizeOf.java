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

import org.terasology.rendering.dag.FBOManagerSubscriber;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.SetViewportSizeOfTask;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;

/**
 * TODO: Add javadocs
 */
public final class SetViewportSizeOf implements FBOManagerSubscriber, StateChange {
    private static final String DEFAULT_FBO_NAME = "sceneOpaque";
    private static SetViewportSizeOf defaultInstance = new SetViewportSizeOf();
    private SetViewportSizeOfTask task;

    private FBO fbo;
    private String fboName;
    private FrameBuffersManager frameBuffersManager;

    public SetViewportSizeOf(String fboName, FrameBuffersManager frameBuffersManager) {
        this.fboName = fboName;
        this.frameBuffersManager = frameBuffersManager;
        fbo = frameBuffersManager.getFBO(fboName);
        // TODO: a very dirty approach :(, however defaultInstance requires a frameBuffer instance, any suggestions?
        if (defaultInstance.frameBuffersManager == null) {
            defaultInstance.frameBuffersManager = frameBuffersManager;
            defaultInstance.fbo = frameBuffersManager.getFBO(DEFAULT_FBO_NAME);
        }
    }

    public SetViewportSizeOf() {
        this.fboName = DEFAULT_FBO_NAME;
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new SetViewportSizeOfTask(fboName, fbo.width(), fbo.height());
            frameBuffersManager.subscribe(this);
        } else {
            update();
        }

        return task;
    }

    @Override
    public boolean isEqualTo(StateChange stateChange) {
        if (stateChange instanceof SetViewportSizeOf) {
            return this.fboName.equals(((SetViewportSizeOf) stateChange).fboName);
        }
        return false;
    }

    @Override
    public boolean isTheDefaultInstance() {
        return this == defaultInstance;
    }

    @Override
    public void update() {
        fbo = frameBuffersManager.getFBO(fboName);
        task.setDimensions(fbo.width(), fbo.height());
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return String.format("%21s: %s(%sx%s)", this.getClass().getSimpleName(), fboName, fbo.width(), fbo.height());
    }
}
