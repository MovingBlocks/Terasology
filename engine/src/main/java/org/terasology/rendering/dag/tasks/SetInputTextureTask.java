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

import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.RenderPipelineTask;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

/**
 * TODO
 */
public class SetInputTextureTask implements RenderPipelineTask {

    private final int textureSlot;
    private final int textureId;
    private final Material material;
    private final String materialParameter;

    public SetInputTextureTask(int textureSlot, int textureId, Material material, String materialParameter) {
        this.textureSlot = textureSlot;
        this.textureId = textureId;
        this.material = material;
        this.materialParameter = materialParameter;
    }

    @Override
    public void execute() {
        glActiveTexture(GL_TEXTURE0 + textureSlot);
        glBindTexture(GL_TEXTURE_2D, textureId);
        material.setInt(materialParameter, textureSlot, true);
    }

    @Override
    public String toString() {
        return String.format("%30s: slot %s, texture %s, material %s, parameter %s", this.getClass().getSimpleName(),
                textureSlot, textureId, material.getUrn().toString(), materialParameter);
    }

}
