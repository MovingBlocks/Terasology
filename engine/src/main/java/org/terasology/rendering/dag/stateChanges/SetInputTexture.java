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
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.terasology.rendering.dag.AbstractNode.getMaterial;

/**
 * This StateChange generates the tasks that set and reset input textures.
 *
 * Input textures are assigned to a texture unit and this is then communicated to the shader.
 * This StateChange and the underlying task only handles textures of type GL_TEXTURE_2D.
 */
public class SetInputTexture implements StateChange {

    private final int textureSlot;
    private final int textureId;
    private final ResourceUrn materialURN;
    private final String materialParameter;

    private SetInputTexture defaultInstance;
    private SetInputTextureTask task;

    /**
     * Constructs an instance of SetInputTexture initialized with the given objects.
     *
     * See SetInputTextureTask for more information on how the constructor's parameters are used.
     *
     * @param textureSlot a 0-based integer. Notice that textureUnit = GL_TEXTURE0 + textureSlot. See OpenGL spects for maximum allowed values.
     * @param textureId an integer representing the opengl name of a texture. This is usually the return value of glGenTexture().
     * @param materialURN a ResourceURN object uniquely identifying a Material asset.
     * @param materialParameter a String representing the variable within the shader holding the texture.
     */
    public SetInputTexture(int textureSlot, int textureId, ResourceUrn materialURN, String materialParameter) {
        this.textureSlot = textureSlot;
        this.textureId = textureId;
        this.materialURN = materialURN;
        this.materialParameter = materialParameter;
    }

    private SetInputTexture(int textureSlot, ResourceUrn materialURN, String materialParameter) {
        this.textureSlot = textureSlot;
        this.textureId = 0;
        this.materialURN = materialURN;
        this.materialParameter = materialParameter;

        defaultInstance = this;
    }

    /**
     * Generates a SetInputTextureTask with the information provided on construction and returns it.
     *
     * @return a SetInputTextureTask instance.
     */
    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new SetInputTextureTask(textureSlot, textureId, materialURN, materialParameter);
        }
        return task;
    }

    @Override
    public int hashCode() {
        return Objects.hash(textureSlot, textureId, materialURN, materialParameter);
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof SetInputTexture)
                && this.textureSlot == ((SetInputTexture) other).textureSlot
                && this.textureId == ((SetInputTexture) other).textureId
                && this.materialURN.equals(((SetInputTexture) other).materialURN)
                && this.materialParameter.equals(((SetInputTexture) other).materialParameter);
    }

    /**
     * Returns a StateChange instance useful to disconnect the given texture from its assigned texture slot.
     * Also disconnects the texture from the shader program.
     *
     * @return the default instance for the particular slot/material/parameter combination held by this
     * SetInputTexture object, cast as a StateChange instance.
     */
    @Override
    public StateChange getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new SetInputTexture(textureSlot, materialURN, materialParameter);
        }
        return defaultInstance;
    }

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
    private class SetInputTextureTask implements RenderPipelineTask {

        private final int textureSlot;
        private final int textureId;
        private final Material material;
        private final String materialParameter;

        private SetInputTextureTask(int textureSlot, int textureId, ResourceUrn materialURN, String materialParameter) {
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
}
