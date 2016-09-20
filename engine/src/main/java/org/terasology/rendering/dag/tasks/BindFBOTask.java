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
package org.terasology.rendering.dag.tasks;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import org.terasology.assets.ResourceUrn;
import org.terasology.rendering.dag.RenderPipelineTask;

/**
 * TODO: Add javadocs
 */
public final class BindFBOTask implements RenderPipelineTask {

    private int fboId;
    private final ResourceUrn fboName;

    public BindFBOTask(int fboId, ResourceUrn fboName) {
        this.fboId = fboId;
        this.fboName = fboName;
    }

    @Override
    public void execute() {
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboId);
    }

    public void setFboId(int fboId) {
        this.fboId = fboId;
    }

    @Override
    public String toString() {
        return String.format("%30s: %s (fboId:%s)", this.getClass().getSimpleName(), fboName, fboId);
    }
}
