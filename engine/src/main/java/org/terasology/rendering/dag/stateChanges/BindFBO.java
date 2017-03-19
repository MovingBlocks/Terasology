/*
 * Copyright 2017 MovingBlocks
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
import org.terasology.rendering.opengl.FBOManagerSubscriber;
import com.google.common.base.Objects;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;

/**
 * TODO: Add javadocs
 */
public final class BindFBO implements FBOManagerSubscriber, StateChange {

    private static final Integer DEFAULT_FRAME_BUFFER_ID = 0;
    // TODO: add necessary checks for ensuring generating FBO with the name "display" is not possible.
    private static final ResourceUrn DEFAULT_FRAME_BUFFER_URN = new ResourceUrn("engine:display");

    private static BindFBO defaultInstance = new BindFBO(DEFAULT_FRAME_BUFFER_URN);

    private BindFBOTask task;
    private BaseFBOsManager fboManager;
    private ResourceUrn fboName;

    public BindFBO(ResourceUrn fboName, BaseFBOsManager fboManager) {
        this.fboManager = fboManager;
        this.fboName = fboName;
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
    public RenderPipelineTask generateTask() {
        if (task == null) {
            // Subscription is only needed if fboID is different than default frame buffer id.
            if (!fboName.equals(DEFAULT_FRAME_BUFFER_URN)) {
                task = new BindFBOTask(fboManager.get(fboName).fboId, fboName);
                fboManager.subscribe(this);
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
        task.setFboId(fboManager.get(fboName).fboId);
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return String.format("%30s: %s", this.getClass().getSimpleName(), fboName);
    }

    private final class BindFBOTask implements RenderPipelineTask {

        private int fboId;
        private final ResourceUrn fboName;

        private BindFBOTask(int fboId, ResourceUrn fboName) {
            this.fboId = fboId;
            this.fboName = fboName;
        }

        @Override
        public void execute() {
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboId);
        }

        private void setFboId(int fboId) {
            this.fboId = fboId;
        }

        @Override
        public String toString() {
            return String.format("%30s: %s (fboId:%s)", this.getClass().getSimpleName(), fboName, fboId);
        }
    }
}
