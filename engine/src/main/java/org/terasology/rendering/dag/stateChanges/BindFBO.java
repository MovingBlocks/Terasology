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
import com.google.common.base.Objects;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.BindFBOTask;

/**
 * TODO: Add javadocs
 */
public final class BindFBO implements FBOManagerSubscriber, StateChange {
    private static final Integer DEFAULT_FRAME_BUFFER_ID = 0;
    // TODO: add necessary checks for ensuring generating FBO with the name "display" is not possible.
    private static final ResourceUrn DEFAULT_FRAME_BUFFER_URN = new ResourceUrn("engine:display");

    private static BindFBO defaultInstance = new BindFBO(DEFAULT_FRAME_BUFFER_URN);

    private BindFBOTask task;
    private BaseFBOsManager frameBuffersManager;
    private ResourceUrn fboName;

    public BindFBO(ResourceUrn fboName, BaseFBOsManager frameBuffersManager) {
        this.frameBuffersManager = frameBuffersManager;
        this.fboName = fboName;
    }

    public BindFBO(DefaultDynamicFBOs defaultDynamicFBO) {
        // TODO: consider removing this constructor whenever defaultDynamicFBOs are nameless
        this(defaultDynamicFBO.getName(), defaultDynamicFBO.getFrameBufferManager());
    }

    private BindFBO(ResourceUrn fboName) {
        this.fboName = fboName;
    }

    public ResourceUrn getFboName() {
        return fboName;
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public boolean isTheDefaultInstance() {
        return this.equals(defaultInstance);
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            // Subscription is only needed if fboID is different than default frame buffer id.
            if (!fboName.equals(DEFAULT_FRAME_BUFFER_URN)) {
                task = new BindFBOTask(frameBuffersManager.get(fboName).fboId, fboName);
                frameBuffersManager.subscribe(this);
            } else {
                task = new BindFBOTask(DEFAULT_FRAME_BUFFER_ID, DEFAULT_FRAME_BUFFER_URN);
            }
        } else {
            if (!fboName.equals(DEFAULT_FRAME_BUFFER_URN)) {
                update();
            }
        }
        return task;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fboName);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BindFBO) && fboName.equals(((BindFBO) obj).getFboName());
    }

    @Override
    public void update() {
        task.setFboId(frameBuffersManager.get(fboName).fboId);
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return String.format("%30s: %s", this.getClass().getSimpleName(), fboName);
    }
}
