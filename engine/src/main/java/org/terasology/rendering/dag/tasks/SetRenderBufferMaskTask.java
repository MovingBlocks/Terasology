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

import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT;
import org.lwjgl.opengl.GL20;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.opengl.FBO;

/**
 * TODO: Add javadocs
 */
public final class SetRenderBufferMaskTask implements RenderPipelineTask {

    private FBO fbo;
    private String fboName;
    private boolean color;
    private boolean normal;
    private boolean lightBuffer;

    public SetRenderBufferMaskTask(FBO fbo, String fboName, boolean color, boolean normal, boolean lightBuffer) {
        this.fboName = fboName;
        this.fbo = fbo;
        this.color = color;
        this.normal = normal;
        this.lightBuffer = lightBuffer;
    }

    @Override
    public void execute() {
        if (fbo == null) {
            return;
        }

        int attachmentId = 0;

        IntBuffer bufferIds = BufferUtils.createIntBuffer(3);

        if (fbo.colorBufferTextureId != 0) {
            if (color) {
                bufferIds.put(GL_COLOR_ATTACHMENT0_EXT + attachmentId);
            }
            attachmentId++;
        }
        if (fbo.normalsBufferTextureId != 0) {
            if (normal) {
                bufferIds.put(GL_COLOR_ATTACHMENT0_EXT + attachmentId);
            }
            attachmentId++;
        }
        if (fbo.lightBufferTextureId != 0) {
            if (lightBuffer) {
                bufferIds.put(GL_COLOR_ATTACHMENT0_EXT + attachmentId);
            }
        }

        bufferIds.flip();

        GL20.glDrawBuffers(bufferIds);
    }

    public void setFbo(FBO fbo) {
        this.fbo = fbo;
    }

    @Override
    public String toString() {
        return String.format("%21s: %s(%s, %s, %s)", this.getClass().getSimpleName(), fboName, color, normal, lightBuffer);
    }
}
