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

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.dag.StateChange;
import org.terasology.utilities.Assets;

import java.util.Objects;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.terasology.rendering.dag.AbstractNode.getMaterial;

// TODO: split this class into two - one for opengl's global state change and one for the specific material state change.

/**
 * Sets a texture asset as the input for a material.
 *
 * Input textures are assigned to a texture unit and this is then communicated to the shader.
 * This StateChange and the underlying task only handles textures of type GL_TEXTURE_2D.
 *
 * Instances of this class bind a texture to a texture unit. The integer identifying the texture unit is then
 * passed to a shader program using the material/parameter pair provided on construction. This allow for a
 * texture asset to be used by a shader program as an input.
 *
 * See the source of the process() method for the nitty gritty details.
 *
 * It is recommended to use one of the children classes (SetInputTexture2D / SetInputTexture3D) to make the code clearer.
 */
public class SetInputTexture implements StateChange {
    private final int textureType;
    private final int textureSlot;
    private final int textureId;
    private final ResourceUrn materialUrn;
    private final String materialParameter;
    private Material material;

    private SetInputTexture defaultInstance;

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new SetInputTexture(GL_TEXTURE_2D, 0, water.getId(), "engine:prog.chunk", "textureWater"));
     *
     * @param textureType an opengl constant, can be GL_TEXTURE_2D, GL_TEXTURE_3D and any other texture type listed in https://www.khronos.org/opengl/wiki/Texture#Theory     * @param textureSlot a 0-based integer. Notice that textureUnit = GL_TEXTURE0 + textureSlot. See OpenGL spects for maximum allowed values.
     * @param textureSlot a 0-based integer. Notice that textureUnit = GL_TEXTURE0 + textureSlot. See OpenGL spects for maximum allowed values.
     * @param textureId an integer representing the opengl name of a texture. This is usually the return value of glGenTexture().
     * @param materialUrn a ResourceURN object uniquely identifying a Material asset.
     * @param materialParameter a String representing the variable within the shader holding the texture.
     */
    protected SetInputTexture(int textureType, int textureSlot, int textureId, ResourceUrn materialUrn, String materialParameter) {
        this.textureType = textureType;
        this.textureSlot = textureSlot;
        this.textureId = textureId;
        this.materialUrn = materialUrn;
        this.materialParameter = materialParameter;

        this.material = getMaterial(materialUrn);

        // TODO: take advantage of Texture.subscribeToDisposal(Runnable) to reobtain the asset if necessary
    }

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new SetInputTexture(GL_TEXTURE_2D, 0, "engine:water", "engine:prog.chunk", "textureWater"));
     *
     * @param textureType an opengl constant, can be GL_TEXTURE_2D, GL_TEXTURE_3D and any other texture type listed in https://www.khronos.org/opengl/wiki/Texture#Theory     * @param textureSlot a 0-based integer. Notice that textureUnit = GL_TEXTURE0 + textureSlot. See OpenGL spects for maximum allowed values.
     * @param textureUrn a String identifying a loaded texture, whose id will then be used by this StateChange.
     * @param materialUrn a ResourceURN object uniquely identifying a Material asset.
     * @param materialParameter a String representing the variable within the shader holding the texture.
     */
    protected SetInputTexture(int textureType, int textureSlot, String textureUrn, ResourceUrn materialUrn, String materialParameter) {
        this.textureType = textureType;
        this.textureSlot = textureSlot;
        this.materialUrn = materialUrn;
        this.materialParameter = materialParameter;

        this.material = getMaterial(materialUrn);

        Optional<Texture> optionalTexture = Assets.getTexture(textureUrn);
        if (optionalTexture.isPresent()) {
            this.textureId = optionalTexture.get().getId();
        } else {
            this.textureId = 0;
            // TODO: Maybe throw some exception or use Logger.error()?
        }

        // TODO: take advantage of Texture.subscribeToDisposal(Runnable) to reobtain the asset if necessary
    }

    private SetInputTexture(int textureType, int textureSlot, ResourceUrn materialUrn, String materialParameter) {
        this.textureType = textureType;
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
            defaultInstance = new SetInputTexture(textureType, textureSlot, materialUrn, materialParameter);
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
        glBindTexture(textureType, textureId);
        material.setInt(materialParameter, textureSlot, true);
    }
}
