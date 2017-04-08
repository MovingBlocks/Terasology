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
    private final ResourceUrn materialUrn;
    private final String materialParameter;
    private Material material;

    private SetInputTexture defaultInstance;

    /**
     * Constructs an instance of SetInputTexture initialized with the given objects.
     *
     * Instances of this class bind a texture to a texture unit. The integer identifying
     * the texture unit is then passed to a shader program using the material/parameter
     * pair provided on construction. See the source of the process() method for the
     * nitty gritty details.
     *
     * @param textureSlot a 0-based integer. Notice that textureUnit = GL_TEXTURE0 + textureSlot. See OpenGL spects for maximum allowed values.
     * @param textureId an integer representing the opengl name of a texture. This is usually the return value of glGenTexture().
     * @param materialUrn a ResourceURN object uniquely identifying a Material asset.
     * @param materialParameter a String representing the variable within the shader holding the texture.
     */
    public SetInputTexture(int textureSlot, int textureId, ResourceUrn materialUrn, String materialParameter) {
        this.textureSlot = textureSlot;
        this.textureId = textureId;
        this.materialUrn = materialUrn;
        this.materialParameter = materialParameter;

        this.material = getMaterial(materialUrn);
    }

    private SetInputTexture(int textureSlot, ResourceUrn materialUrn, String materialParameter) {
        this.textureSlot = textureSlot;
        this.textureId = 0;
        this.materialUrn = materialUrn;
        this.materialParameter = materialParameter;

        this.material = getMaterial(materialUrn);

        defaultInstance = this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(textureSlot, textureId, materialUrn, materialParameter);
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof SetInputTexture)
                && this.textureSlot == ((SetInputTexture) other).textureSlot
                && this.textureId == ((SetInputTexture) other).textureId
                && this.materialUrn.equals(((SetInputTexture) other).materialUrn)
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
            defaultInstance = new SetInputTexture(textureSlot, materialUrn, materialParameter);
        }
        return defaultInstance;
    }

    @Override
    public String toString() {
        return String.format("%30s: slot %s, texture %s, material %s, parameter %s", this.getClass().getSimpleName(),
                textureSlot, textureId, material.getUrn().toString(), materialParameter);
    }

    @Override
    public void process() {
        glActiveTexture(GL_TEXTURE0 + textureSlot);
        glBindTexture(GL_TEXTURE_2D, textureId);
        material.setInt(materialParameter, textureSlot, true);
    }
}
