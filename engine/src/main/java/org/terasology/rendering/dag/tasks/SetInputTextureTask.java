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

import org.terasology.assets.ResourceUrn;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.RenderPipelineTask;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.terasology.rendering.dag.AbstractNode.getMaterial;

/**
 * Instances of this class bind a texture to a texture unit. The integer identifying
 * the texture unit is then passed to a shader program using the material/parameter
 * pair provided on construction. See the source of the execute() method for the
 * nitty gritty details.
 *
 * WARNING: RenderPipelineTasks are not meant for direct instantiation and manipulation.
 * Modules or other parts of the engine should take advantage of them through classes
 * inheriting from StateChange.
 */
public class SetInputTextureTask implements RenderPipelineTask {

    private final int textureSlot;
    private final int textureId;
    private final Material material;
    private final String materialParameter;

    public SetInputTextureTask(int textureSlot, int textureId, ResourceUrn materialURN, String materialParameter) {
        this.textureSlot = textureSlot;
        this.textureId = textureId;
        this.material = getMaterial(materialURN);
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
