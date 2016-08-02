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
import org.terasology.rendering.dag.tasks.BindFBOTask;
import org.terasology.rendering.opengl.FrameBuffersManager;

/**
 * TODO: Add javadocs
 */
public final class BindFBO implements FBOManagerSubscriber, StateChange {
    private static final Integer DEFAULT_FRAME_BUFFER_ID = 0;
    // TODO: add necessary checks for ensuring generating FBO with the name "display" is not possible.
    private static final String DEFAULT_FRAME_BUFFER_NAME = "display";
    private static FrameBuffersManager frameBuffersManager;
    private static BindFBO defaultInstance = new BindFBO(DEFAULT_FRAME_BUFFER_NAME);
    private String fboName;
    private BindFBOTask task;

    public BindFBO(String fboName) {
        this.fboName = fboName;
    }

    public String getFboName() {
        return fboName;
    }

    public static void setFrameBuffersManager(FrameBuffersManager frameBuffersManager) {
        BindFBO.frameBuffersManager = frameBuffersManager;
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public boolean isTheDefaultInstance() {
        return this == defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            // Subscription is only needed if fboID is different than default frame buffer id.
            if (!fboName.equals(DEFAULT_FRAME_BUFFER_NAME)) {
                task = new BindFBOTask(frameBuffersManager.getFBO(fboName).fboId, fboName);
                frameBuffersManager.subscribe(this);
            } else {
                task = new BindFBOTask(DEFAULT_FRAME_BUFFER_ID, DEFAULT_FRAME_BUFFER_NAME);
            }
        } else {
            if (!fboName.equals(DEFAULT_FRAME_BUFFER_NAME)) {
                update();
            }
        }
        return task;
    }

    @Override
    public boolean isEqualTo(StateChange stateChange) {
        if (stateChange instanceof BindFBO) {
            return this.fboName.equals(((BindFBO) stateChange).getFboName());
        }
        return false;
    }

    @Override
    public void update() {
        task.setFboId(frameBuffersManager.getFBO(fboName).fboId);
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return String.format("%21s: %s", this.getClass().getSimpleName(), fboName);
    }
}
