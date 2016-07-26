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
import org.terasology.rendering.dag.tasks.SetRenderBufferMaskTask;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;

/**
 * TODO: Add javadocs
 */
public final class SetRenderBufferMask implements FBOManagerSubscriber, StateChange {
    private static final String DEFAULT_FBO = "sceneOpaque";
    private static final boolean DEFAULT_COLOR_MASK = true;
    private static final boolean DEFAULT_NORMAL_MASK = true;
    private static final boolean DEFAULT_LIGHT_BUFFER_MASK = true;

    private static SetRenderBufferMask defaultInstance = new SetRenderBufferMask();

    private String fboName;
    private FBO fbo;
    private FrameBuffersManager frameBuffersManager;
    private boolean color;
    private boolean normal;
    private boolean lightBuffer;
    private SetRenderBufferMaskTask task;

    public SetRenderBufferMask(String fboName, FrameBuffersManager frameBuffersManager,
                               boolean color, boolean normal, boolean lightBuffer) {
        this.fboName = fboName;
        this.frameBuffersManager = frameBuffersManager;
        this.color = color;
        this.normal = normal;
        this.lightBuffer = lightBuffer;
    }

    private SetRenderBufferMask() {
        this.fboName = DEFAULT_FBO;
        color = DEFAULT_COLOR_MASK;
        normal = DEFAULT_NORMAL_MASK;
        lightBuffer = DEFAULT_LIGHT_BUFFER_MASK;
    }

    @Override
    public StateChange getDefaultInstance() {
        if (defaultInstance.frameBuffersManager == null) {
            defaultInstance.frameBuffersManager = frameBuffersManager;
            defaultInstance.fbo = frameBuffersManager.getFBO(fboName);
        }
        return defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new SetRenderBufferMaskTask(fbo, fboName, color, normal, lightBuffer);
            frameBuffersManager.subscribe(this);
        } else {
            update();
        }
        return task;
    }

    @Override
    public boolean isEqualTo(StateChange stateChange) {
        if (stateChange instanceof SetRenderBufferMask) {
            SetRenderBufferMask change = (SetRenderBufferMask) stateChange;
            return change.fboName.equals(this.fboName)
                    && change.color == this.color
                    && change.normal == this.normal
                    && change.lightBuffer == this.lightBuffer;
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
        task.setFbo(fbo);
    }

    @Override
    public String toString() {
        return String.format("%21s: %s(%s, %s, %s)", this.getClass().getSimpleName(), fboName, color, normal, lightBuffer);
    }
}
