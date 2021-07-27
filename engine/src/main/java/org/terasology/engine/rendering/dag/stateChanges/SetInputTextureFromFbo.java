// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.opengl.BaseFboManager;
import org.terasology.engine.rendering.opengl.FBO;
import org.terasology.engine.rendering.opengl.fbms.DisplayResolutionDependentFbo;
import org.terasology.engine.rendering.dag.StateChange;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.terasology.engine.rendering.dag.AbstractNode.getMaterial;

// TODO: split this class into two - one for opengl's global state change and one for the specific material state change.

/**
 * Sets a texture attached to an FBO as the input for a material.
 */
public class SetInputTextureFromFbo implements StateChange, PropertyChangeListener {
    // depthStencilRboId is a possible FBO attachment but is not covered by a case here
    // as it wouldn't work with the glBindTexture(TEXTURE_2D, ...) call.
    public enum FboTexturesTypes {
        ColorTexture,
        DepthStencilTexture,
        NormalsTexture,
        LightAccumulationTexture,
    }

    private SetInputTextureFromFbo defaultInstance;

    private int textureSlot;
    private FBO fbo;
    private FboTexturesTypes textureType;
    private ResourceUrn materialUrn;
    private String shaderParameterName;
    private Material material;
    private int textureId;

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new SetInputTextureFromFbo(0, fbo, ColorTexture,
     *                                  displayResolutionDependentFboManager, "engine:prog.chunk", "textureWater"));
     *
     * @param textureSlot an integer representing the number to add to GL_TEXTURE0 to identify a texture unit on the GPU.
     * @param fbo the FBO from which the texture attachment will be fetched.
     * @param textureType one of the types available through the FboTextureType enum.
     * @param fboManager the BaseFboManager instance that will send change notifications via the update() method of this class.
     * @param materialUrn a URN identifying a Material instance.
     * @param shaderParameterName the name of a variable in the shader program used to sample the texture.
     */
    public SetInputTextureFromFbo(int textureSlot, FBO fbo, FboTexturesTypes textureType, BaseFboManager fboManager,
                                  ResourceUrn materialUrn, String shaderParameterName) {
        this.textureSlot = textureSlot;
        this.textureType = textureType;
        this.fbo = fbo;
        this.materialUrn = materialUrn;
        this.shaderParameterName = shaderParameterName;

        this.material = getMaterial(materialUrn);

        propertyChange(null); // Cheeky way to initialise textureId
        fboManager.subscribe(DisplayResolutionDependentFbo.POST_FBO_REGENERATION, this);
    }

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new SetInputTextureFromFbo(0, fboUri, ColorTexture,
     *                                  displayResolutionDependentFboManager, "engine:prog.chunk", "textureWater"));
     *
     * @param textureSlot an integer representing the number to add to GL_TEXTURE0 to identify a texture unit on the GPU.
     * @param fboUri a SimpleUri identifying an FBO in the given fboManager, from which the texture attachment will be fetched.
     * @param textureType one of the types available through the FboTextureType enum.
     * @param fboManager the BaseFboManager instance that will send change notifications via the update() method of this class.
     * @param materialUrn a ResourceUrn identifying a Material instance.
     * @param shaderParameterName the name of a variable in the shader program used to sample the texture.
     */
    public SetInputTextureFromFbo(int textureSlot, SimpleUri fboUri, FboTexturesTypes textureType, BaseFboManager fboManager,
                                  ResourceUrn materialUrn, String shaderParameterName) {
        this(textureSlot, fboManager.get(fboUri), textureType, fboManager, materialUrn, shaderParameterName);
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
        switch (textureType) {
            case ColorTexture:
                return fbo.getColorBufferTextureId();

            case DepthStencilTexture:
                return fbo.getDepthStencilTextureId();

            case NormalsTexture:
                return fbo.getNormalsBufferTextureId();

            case LightAccumulationTexture:
                return fbo.getLightBufferTextureId();
        }

        return 0;
    }

    /**
     * Normally called by the FBO manager provided, when the FBOs are regenerated.
     *
     * This method refreshes the task's reference to the FBO attachment, so that they are always up to date.
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // The only property we are subscribing to is DisplayResolutionDependentFbo.POST_FBO_REGENERATION
        textureId = fetchTextureId();
    }

    @Override
    public String toString() {
        if (this != defaultInstance) {
            return String.format("%30s: slot %s, fbo %s (fboId: %s), textureType %s, material %s, parameter '%s'", this.getClass().getSimpleName(),
                    textureSlot, fbo.getName(), fbo.getId(), textureType.name(), materialUrn.toString(), shaderParameterName);
        } else {
            return String.format("%30s: slot %s, textureId 0, material %s, parameter '%s'", this.getClass().getSimpleName(),
                    textureSlot, materialUrn.toString(), shaderParameterName);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(textureSlot, fbo, textureType, materialUrn, shaderParameterName);
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof SetInputTextureFromFbo)
                && this.textureSlot == ((SetInputTextureFromFbo) other).textureSlot
                && this.fbo == ((SetInputTextureFromFbo) other).fbo
                && this.textureType == ((SetInputTextureFromFbo) other).textureType
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
