// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import org.terasology.gestalt.assets.ResourceUrn;

import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;

/**
 * Instances of this class bind a 3D texture to a texture unit. The integer identifying the texture unit is then
 * passed to a shader program using the material/parameter pair provided on construction. This allow for a
 * texture asset to be used by a shader program as an input.
 *
 * See the parent class SetInputTexture for more details.
 *
 * It is recommended to use this class instead of the more generic SetInputTexture class, to make the code clearer.

 */
public class SetInputTexture3D extends SetInputTexture {
    /**
     * Constructs an instance of this class, to be used in the constructor of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new SetInputTexture3D(0, water.getId(), "engine:prog.chunk", "textureWater"));
     *
     * @param textureSlot a 0-based integer. Notice that textureUnit = GL_TEXTURE0 + textureSlot. See OpenGL spects for maximum allowed values.
     * @param textureId an integer representing the opengl name of a texture. This is usually the return value of glGenTexture().
     * @param materialUrn a ResourceURN object uniquely identifying a Material asset.
     * @param materialParameter a String representing the variable within the shader holding the texture.
     */
    public SetInputTexture3D(int textureSlot, int textureId, ResourceUrn materialUrn, String materialParameter) {
        super(GL_TEXTURE_3D, textureSlot, textureId, materialUrn, materialParameter);
    }

    /**
     * Constructs an instance of this class, to be used in the constructor of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new SetInputTexture3D(0, "engine:water", "engine:prog.chunk", "textureWater"));
     *
     * @param textureSlot a 0-based integer. Notice that textureUnit = GL_TEXTURE0 + textureSlot. See OpenGL spects for maximum allowed values.
     * @param textureUrn a String identifying a loaded texture, whose id will then be used by this StateChange.
     * @param materialUrn a ResourceURN object uniquely identifying a Material asset.
     * @param materialParameter a String representing the variable within the shader holding the texture.
     */
    public SetInputTexture3D(int textureSlot, String textureUrn, ResourceUrn materialUrn, String materialParameter) {
        super(GL_TEXTURE_3D, textureSlot, textureUrn, materialUrn, materialParameter);
    }
}
