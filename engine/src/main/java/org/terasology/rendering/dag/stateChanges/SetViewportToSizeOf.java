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
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * TODO: Add javadocs
 */
public final class SetViewportToSizeOf implements FBOManagerSubscriber, StateChange {
    private static SetViewportToSizeOf defaultInstance;

    private BaseFBOsManager fboManager;
    private SetViewportToSizeOfTask task;
    private ResourceUrn fboName;

    public SetViewportToSizeOf(ResourceUrn fboName, BaseFBOsManager frameBuffersManager) {
        this.fboManager = frameBuffersManager;
        this.fboName = fboName;
    }

    @Override
    public StateChange getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new SetViewportToSizeOf(READONLY_GBUFFER, CoreRegistry.get(DisplayResolutionDependentFBOs.class));
        }
        return defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new SetViewportToSizeOfTask(fboName);
            fboManager.subscribe(this);
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

        FBO fbo = getFbo();
        FBO otherFbo = other.getFbo();

        return fbo.width() == otherFbo.width() && fbo.height() == otherFbo.height();
    }

    @Override
    public void update() {
        FBO fbo = getFbo();

        task.setDimensions(fbo.width(), fbo.height());
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return String.format("%30s: %s", this.getClass().getSimpleName(), fboName);
    }

    private FBO getFbo() {
        return fboManager.get(fboName);
    }

    private final class SetViewportToSizeOfTask implements RenderPipelineTask {
        private int width;
        private int height;
        private ResourceUrn fboName;

        private SetViewportToSizeOfTask(ResourceUrn fboName) {
            this.fboName = fboName;
        }

        private void setDimensions(int w, int h) {
            this.width = w;
            this.height = h;
        }

        @Override
        public void execute() {
            glViewport(0, 0, width, height);
        }

        @Override
        public String toString() {
            return String.format("%30s: %s (%sx%s)", this.getClass().getSimpleName(), fboName, width, height);
        }
    }
}
