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

/**
 * This state change implementation sets a texture attached to an FBO as the input for a material.
 */
public class SetInputTextureFromFBO implements StateChange, FBOManagerSubscriber {
    private static final Logger logger = Logger.getLogger("SetInputTextureFromFBO");

    // depthStencilRboId is a possible FBO attachment but is not covered by a case here
    // as it wouldn't work with the glBindTexture(TEXTURE_2D, ...) call.
    public enum FboTexturesTypes {
        ColorTexture,
        DepthStencilTexture,
        NormalsTexture,
        LightAccumulationTexture,
    }

    private int textureSlot;
    private ResourceUrn fboUrn;
    private FBO inputFbo;
    private FboTexturesTypes textureType;
    private BaseFBOsManager fboManager;
    private ResourceUrn materialURN;
    private String parameterName;

    private SetInputTextureFromFBO defaultInstance;
    private Task task;

    /**
     * Constructs and returns an instance of this class, according to the parameters provided.
     *
     * Instances of this class can be added to the list of desired state changes of a node, to set an FBO-attached
     * texture as input to the enabled material.
     *
     * @param textureSlot an integer representing the number to add to GL_TEXTURE0 to identify a texture unit on the GPU.
     * @param fboUrn an URN identifying an FBO.
     * @param textureType one of the types available through the FboTextureType enum.
     * @param fboManager the BaseFBOsManager instance that will send change notifications via the update() method of this class.
     * @param materialURN an URN identifying a Material instance.
     * @param parameterName the name of a variable in the shader program used to sample the texture.
     */
    public SetInputTextureFromFBO(int textureSlot, ResourceUrn fboUrn, FboTexturesTypes textureType, BaseFBOsManager fboManager,
                                  ResourceUrn materialURN, String parameterName) {
        this.textureSlot = textureSlot;
        this.textureType = textureType;
        this.fboUrn = fboUrn;
        this.inputFbo = fboManager.get(fboUrn);
        this.fboManager = fboManager;

        this.materialURN = materialURN;
        this.parameterName = parameterName;
    }

    private SetInputTextureFromFBO(int textureSlot, ResourceUrn materialURN, String parameterName) {
        this.textureSlot = textureSlot;
        this.materialURN = materialURN;
        this.parameterName = parameterName;

        defaultInstance = this;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            if (inputFbo != null) {
                task = new Task(this.textureSlot, fetchTextureId(), this.materialURN, this.parameterName);
                fboManager.subscribe(this);
            } else {
                task = new Task(this.textureSlot, 0, this.materialURN, this.parameterName);
            }
        }

        return task;
    }

    @Override
    public StateChange getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new SetInputTextureFromFBO(this.textureSlot, materialURN, this.parameterName);
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
        task.setTextureId(fetchTextureId());
    }

    @Override
    public String toString() {
        if (this != defaultInstance) {
            return String.format("%30s: slot %s, fbo %s, textureType %s, fboManager %s, material %s, parameter '%s'", this.getClass().getSimpleName(),
                    textureSlot, fboUrn.toString(), textureType.name(), fboManager.toString(), materialURN.toString(), parameterName);
        } else {
            return String.format("%30s: slot %s, textureId 0, material %s, parameter '%s'", this.getClass().getSimpleName(),
                    textureSlot, materialURN.toString(), parameterName);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(textureSlot, fboUrn, textureType, fboManager, materialURN, parameterName);
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof SetInputTextureFromFBO)
                && this.textureSlot == ((SetInputTextureFromFBO) other).textureSlot
                && this.fboUrn == ((SetInputTextureFromFBO) other).fboUrn
                && this.textureType == ((SetInputTextureFromFBO) other).textureType
                && this.fboManager == ((SetInputTextureFromFBO) other).fboManager
                && this.materialURN.equals(((SetInputTextureFromFBO) other).materialURN)
                && this.parameterName.equals(((SetInputTextureFromFBO) other).parameterName);
    }

    /**
     * Instances of this class do the actual work of activating a given texture unit,
     * binding the appropriate texture to it and configuring a material to use it as input.
     */
    protected class Task implements RenderPipelineTask {
        private final int textureSlot;
        private final Material material;
        private final String shaderParameterName;
        private int textureId;

        /**
         * Constructs an instance of this inner class, according to the given parameters.
         *
         * @param textureSlot an integer indirectly identifying a texture unit on the GPU (textureUnit = GL_TEXTURE0 + textureSlot).
         * @param textureId the opengl id of a texture, usually obtained via glGenTextures().
         * @param materialURN an URN identifying a material.
         * @param shaderParameterName the name of the variable in the shader program used to sample the texture.
         */
        private Task(int textureSlot, int textureId, ResourceUrn materialURN, String shaderParameterName) {
            this.textureSlot = textureSlot;
            this.textureId = textureId;
            this.material = getMaterial(materialURN);
            this.shaderParameterName = shaderParameterName;
        }

        /**
         * This method is used when FBOs have changed and the id of the texture attached to one needs refreshing.
         *
         * @param textureId an integer identifying a texture buffer on the GPU, originally obtained via glGenTextures().
         */
        protected void setTextureId(int textureId) {
            this.textureId = textureId;
        }

        /**
         * Activates the texture unit GL_TEXTURE0 + textureSlot, binds the GL_TEXTURE_2D identified by textureId to it
         * and sets the material provided on construction to sample the texture via the parameterName also provided on
         * construction.
         */
        @Override
        public void execute() {
            glActiveTexture(GL_TEXTURE0 + textureSlot);
            glBindTexture(GL_TEXTURE_2D, textureId);
            material.setInt(shaderParameterName, textureSlot, true);
        }

        @Override
        public String toString() {
            return String.format("%30s: slot %s, texture %s, material %s, parameter %s", this.getClass().getSimpleName(),
                    textureSlot, textureId, material.getUrn().toString(), shaderParameterName);
        }
    }
}
