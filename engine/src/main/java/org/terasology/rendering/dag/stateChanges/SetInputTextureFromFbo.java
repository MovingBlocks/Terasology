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
import org.terasology.rendering.opengl.BaseFBOsManager;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOManagerSubscriber;

import java.util.Objects;
import java.util.logging.Logger;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.terasology.rendering.dag.AbstractNode.getMaterial;

// TODO: split this class into two - one for opengl's global state change and one for the specific material state change.

/**
 * This state change implementation sets a texture attached to an FBO as the input for a material.
 */
public class SetInputTextureFromFbo implements StateChange, FBOManagerSubscriber {
    // depthStencilRboId is a possible FBO attachment but is not covered by a case here
    // as it wouldn't work with the glBindTexture(TEXTURE_2D, ...) call.
    public enum FboTexturesTypes {
        ColorTexture,
        DepthStencilTexture,
        NormalsTexture,
        LightAccumulationTexture,
    }

    private static final Logger logger = Logger.getLogger("SetInputTextureFromFbo");

    private SetInputTextureFromFbo defaultInstance;

    private int textureSlot;
    private ResourceUrn fboUrn;
    private FBO inputFbo;
    private FboTexturesTypes textureType;
    private BaseFBOsManager fboManager;
    private ResourceUrn materialUrn;
    private String shaderParameterName;
    private Material material;
    private int textureId;

    /**
     * Constructs an instance of this class, according to the parameters provided.
     *
     * Instances of this class can be added to the list of desired state changes of a node, to set an FBO-attached
     * texture as input to the enabled material.
     *
     * @param textureSlot an integer representing the number to add to GL_TEXTURE0 to identify a texture unit on the GPU.
     * @param fboUrn an URN identifying an FBO.
     * @param textureType one of the types available through the FboTextureType enum.
     * @param fboManager the BaseFBOsManager instance that will send change notifications via the update() method of this class.
     * @param materialUrn an URN identifying a Material instance.
     * @param shaderParameterName the name of a variable in the shader program used to sample the texture.
     */
    public SetInputTextureFromFbo(int textureSlot, ResourceUrn fboUrn, FboTexturesTypes textureType, BaseFBOsManager fboManager,
                                  ResourceUrn materialUrn, String shaderParameterName) {
        this.textureSlot = textureSlot;
        this.textureType = textureType;
        this.fboUrn = fboUrn;
        this.fboManager = fboManager;
        this.materialUrn = materialUrn;
        this.shaderParameterName = shaderParameterName;

        this.inputFbo = fboManager.get(fboUrn);
        this.material = getMaterial(materialUrn);

        update(); // Cheeky way to initialise textureId
        fboManager.subscribe(this);
    }

    private SetInputTextureFromFbo(int textureSlot, ResourceUrn materialUrn, String shaderParameterName) {
        this.textureSlot = textureSlot;
        this.materialUrn = materialUrn;
        this.shaderParameterName = shaderParameterName;

        this.material = getMaterial(materialUrn);

        defaultInstance = this;
    }

    @Override
    public StateChange getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new SetInputTextureFromFbo(this.textureSlot, materialUrn, this.shaderParameterName);
        }
        return defaultInstance;
    }

    private int fetchTextureId() {
        // TODO: make checks to verify the FBOs has the requested buffer;
        if (inputFbo != null) {
            switch (textureType) {
                case ColorTexture:
                    return inputFbo.colorBufferTextureId;

                case DepthStencilTexture:
                    return inputFbo.depthStencilTextureId;

                case NormalsTexture:
                    return inputFbo.normalsBufferTextureId;

                case LightAccumulationTexture:
                    return inputFbo.lightBufferTextureId;
            }
        } else {
            logger.warning("FBOs manager has no record of an FBO named " + this.fboUrn.toString());
        }

        return 0;
    }

    /**
     * Normally called by the FBO manager provided, when the FBOs are regenerated.
     *
     * This method refreshes the task's reference to the FBO attachment, so that they are always up to date.
     */
    @Override
    public void update() {
        inputFbo = fboManager.get(fboUrn);
        textureId = fetchTextureId();
    }

    @Override
    public String toString() {
        if (this != defaultInstance) {
            return String.format("%30s: slot %s, fbo %s, textureType %s, fboManager %s, material %s, parameter '%s'", this.getClass().getSimpleName(),
                    textureSlot, fboUrn.toString(), textureType.name(), fboManager.toString(), materialUrn.toString(), shaderParameterName);
        } else {
            return String.format("%30s: slot %s, textureId 0, material %s, parameter '%s'", this.getClass().getSimpleName(),
                    textureSlot, materialUrn.toString(), shaderParameterName);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(textureSlot, fboUrn, textureType, fboManager, materialUrn, shaderParameterName);
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof SetInputTextureFromFbo)
                && this.textureSlot == ((SetInputTextureFromFbo) other).textureSlot
                && this.fboUrn == ((SetInputTextureFromFbo) other).fboUrn
                && this.textureType == ((SetInputTextureFromFbo) other).textureType
                && this.fboManager == ((SetInputTextureFromFbo) other).fboManager
                && this.materialUrn.equals(((SetInputTextureFromFbo) other).materialUrn)
                && this.shaderParameterName.equals(((SetInputTextureFromFbo) other).shaderParameterName);
    }

    /**
     * Activates the texture unit GL_TEXTURE0 + textureSlot, binds the GL_TEXTURE_2D identified by textureId to it
     * and sets the material provided on construction to sample the texture via the parameterName also provided on
     * construction.
     */
    @Override
    public void process() {
        glActiveTexture(GL_TEXTURE0 + textureSlot);
        glBindTexture(GL_TEXTURE_2D, textureId);
        material.setInt(shaderParameterName, textureSlot, true);
    }
}
