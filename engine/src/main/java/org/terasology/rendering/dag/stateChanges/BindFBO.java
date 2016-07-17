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


import org.terasology.rendering.dag.AbstractStateChange;
import org.terasology.rendering.dag.FBOManagerSubscriber;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.BindFBOTask;
import org.terasology.rendering.opengl.FrameBuffersManager;

/**
 * TODO: Add javadocs
 */
public final class BindFBO extends AbstractStateChange<String> implements FBOManagerSubscriber {
    private static final Integer DEFAULT_FRAME_BUFFER_ID = 0;
    private static final String DEFAULT_FRAME_BUFFER_NAME = "display";
    private static BindFBO defaultInstance = new BindFBO();

    private Integer fboId;
    private BindFBOTask task;
    private FrameBuffersManager frameBuffersManager;

    public BindFBO(String fboName, FrameBuffersManager frameBuffersManager) {
        super(fboName);
        fboId = frameBuffersManager.getFBO(this.getValue()).fboId;
        frameBuffersManager.subscribe(this);
        this.frameBuffersManager = frameBuffersManager;
    }

    private BindFBO() {
        super(DEFAULT_FRAME_BUFFER_NAME);
        fboId = DEFAULT_FRAME_BUFFER_ID;
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        task = new BindFBOTask(fboId);
        return task;
    }

    @Override
    public void update() {
        fboId = frameBuffersManager.getFBO(this.getValue()).fboId;
        task.setFboToBind(fboId);
    }
}
