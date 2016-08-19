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
package org.terasology.rendering.opengl;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.terasology.assets.ResourceUrn;

/**
 * Builder class to simplify the syntax creating an FBO.
 * <p>
 * Once the desired characteristics of the FBO are set via the Builder's constructor and its
 * use*Buffer() methods, the build() method can be called for the actual FBO to be generated,
 * alongside the underlying FrameBuffer and its attachments on the GPU.
 * <p>
 * The new FBO is automatically registered with the LwjglRenderingProcess, overwriting any
 * existing FBO with the same title.
 */
public class FBOConfig {
    private ResourceUrn fboName;
    private FBO.Dimensions dimensions;
    private FBO.Type type;


    private boolean useDepthBuffer;
    private boolean useNormalBuffer;
    private boolean useLightBuffer;
    private boolean useStencilBuffer;
    private float scale;
    /**
     * Constructs an FBO builder capable of building the two most basic FBOs:
     * an FBO with no attachments or one with a single color buffer attached to it.
     * <p>
     * To attach additional buffers, see the use*Buffer() methods.
     * <p>
     * Example: FBO basicFBO = new FBObuilder("basic", new Dimensions(1920, 1080), Type.DEFAULT).build();
     *
     * @param fboName      A string identifier, the title is used to later manipulate the FBO through
     *                   methods such as LwjglRenderingProcess.getFBO(title) and LwjglRenderingProcess.bindFBO(title).
     * @param dimensions A Dimensions object providing width and height information.
     * @param type       Type.DEFAULT will result in a 32 bit color buffer attached to the FBO. (GL_RGBA, GL11.GL_UNSIGNED_BYTE, GL_LINEAR)
     *                   Type.HDR will result in a 64 bit color buffer attached to the FBO. (GL_RGBA, GL_HALF_FLOAT_ARB, GL_LINEAR)
     *                   Type.NO_COLOR will result in -no- color buffer attached to the FBO
     *                   (WARNING: this could result in an FBO with Status.DISPOSED - see FBO.getStatus()).
     */
    public FBOConfig(ResourceUrn fboName, FBO.Dimensions dimensions, FBO.Type type) {
        this.fboName = fboName;
        this.dimensions = dimensions;
        this.type = type;
    }

    /**
     * Same as the previous FBObuilder constructor, but taking in input
     * explicit, integer width and height instead of a Dimensions object.
     */
    public FBOConfig(ResourceUrn fboName, int width, int height, FBO.Type type) {
        this(fboName, new FBO.Dimensions(width, height), type);
    }

    public FBOConfig(ResourceUrn fboName, ScalingFactors factors, FBO.Type type) {
        this.fboName = fboName;
        this.type = type;
        this.scale = factors.getScale();
    }


    public FBOConfig(ResourceUrn fboName, float scale, FBO.Type type) {
        Preconditions.checkArgument(scale != 0, "scale can not be zero.");
        this.fboName = fboName;
        this.type = type;
        this.scale = scale;
    }

    public FBOConfig(ResourceUrn fboName, FBO.Type type) {
        this.fboName = fboName;
        this.type = type;
    }

    /**
     * Sets the builder to generate, allocate and attach a 24 bit depth buffer to the FrameBuffer to be built.
     * If useStencilBuffer() is also used, an 8 bit stencil buffer will also be associated with the depth buffer.
     * For details on the specific characteristics of the buffers, see the FBO.create() method.
     *
     * @return The calling instance, to chain calls, i.e.: new FBObuilder(...).useDepthBuffer().build();
     */
    public FBOConfig useDepthBuffer() {
        useDepthBuffer = true;
        return this;
    }

    /**
     * Sets the builder to generate, allocate and attach a normals buffer to the FrameBuffer to be built.
     * For details on the specific characteristics of the buffer, see the FBO.create() method.
     *
     * @return The calling instance, to chain calls, i.e.: new FBObuilder(...).useNormalsBuffer().build();
     */
    public FBOConfig useNormalBuffer() {
        useNormalBuffer = true;
        return this;
    }

    /**
     * Sets the builder to generate, allocate and attach a light buffer to the FrameBuffer to be built.
     * Be aware that the number of bits per channel for this buffer changes with the set FBO.Type.
     * For details see the FBO.create() method.
     *
     * @return The calling instance, to chain calls, i.e.: new FBObuilder(...).useLightBuffer().build();
     */
    public FBOConfig useLightBuffer() {
        useLightBuffer = true;
        return this;
    }

    /**
     * -IF- the builder has been set to generate a depth buffer, using this method sets the builder to
     * generate a depth buffer inclusive of stencil buffer, with the following characteristics:
     * internal format GL_DEPTH24_STENCIL8_EXT, data type GL_UNSIGNED_INT_24_8_EXT and filtering GL_NEAREST.
     *
     * @return The calling instance of FBObuilder, to chain calls,
     * i.e.: new FBObuilder(...).useDepthBuffer().useStencilBuffer().build();
     */
    public FBOConfig useStencilBuffer() {
        useStencilBuffer = true;
        return this;
    }

    // TODO: add javadocs
    public boolean hasDepthBuffer() {
        return useDepthBuffer;
    }

    public boolean hasNormalBuffer() {
        return useNormalBuffer;
    }

    public boolean hasLightBuffer() {
        return useLightBuffer;
    }

    public boolean hasStencilBuffer() {
        return useStencilBuffer;
    }

    public FBO.Type getType() {
        return type;
    }

    public float getScale() {
        return scale;
    }

    public FBO.Dimensions getDimensions() {
        return dimensions;
    }

    public ResourceUrn getName() {
        return fboName;
    }

    public void setDimensions(FBO.Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public int hashCode() {
        // TODO: add scale and dimension
        return Objects.hashCode(fboName, type, useDepthBuffer, useNormalBuffer, useLightBuffer, useStencilBuffer);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof FBOConfig) {
            // TODO: add scale and dimension check here
            FBOConfig config = (FBOConfig) obj;
            return this.fboName.equals(config.getName())
                    && this.type == config.getType()
                    && this.useDepthBuffer == config.useDepthBuffer
                    && this.useNormalBuffer == config.useNormalBuffer
                    && this.useLightBuffer == config.useLightBuffer
                    && this.useStencilBuffer == config.useStencilBuffer;
        }
        return false;
    }
}
