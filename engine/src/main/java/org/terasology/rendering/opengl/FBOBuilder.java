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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class FBOBuilder {
    private static final Logger logger = LoggerFactory.getLogger(FBOBuilder.class);
    private static FrameBuffersManager frameBuffersManager;

    private FBO generatedFBO;

    private String title;
    private FBO.Dimensions dimensions;
    private FBO.Type type;

    private boolean useDepthBuffer;
    private boolean useNormalBuffer;
    private boolean useLightBuffer;
    private boolean useStencilBuffer;
    private boolean isStatic;

    /**
     * Constructs an FBO builder capable of building the two most basic FBOs:
     * an FBO with no attachments or one with a single color buffer attached to it.
     * <p>
     * To attach additional buffers, see the use*Buffer() methods.
     * <p>
     * Example: FBO basicFBO = new FBObuilder("basic", new Dimensions(1920, 1080), Type.DEFAULT).build();
     *
     * @param title      A string identifier, the title is used to later manipulate the FBO through
     *                   methods such as LwjglRenderingProcess.getFBO(title) and LwjglRenderingProcess.bindFBO(title).
     * @param dimensions A Dimensions object providing width and height information.
     * @param type       Type.DEFAULT will result in a 32 bit color buffer attached to the FBO. (GL_RGBA, GL11.GL_UNSIGNED_BYTE, GL_LINEAR)
     *                   Type.HDR will result in a 64 bit color buffer attached to the FBO. (GL_RGBA, GL_HALF_FLOAT_ARB, GL_LINEAR)
     *                   Type.NO_COLOR will result in -no- color buffer attached to the FBO
     *                   (WARNING: this could result in an FBO with Status.DISPOSED - see FBO.getStatus()).
     */
    public FBOBuilder(String title, FBO.Dimensions dimensions, FBO.Type type) {
        this.title = title;
        this.dimensions = dimensions;
        this.type = type;
        this.isStatic = false;
    }

    /**
     * Same as the previous FBObuilder constructor, but taking in input
     * explicit, integer width and height instead of a Dimensions object.
     */
    public FBOBuilder(String title, int width, int height, FBO.Type type) {
        this(title, new FBO.Dimensions(width, height), type);
    }

    public FBOBuilder(String title, float scale, FBO.Type type) {
        this(title, frameBuffersManager.getFullScale().multiplyBy(scale), type);
    }

    public FBOBuilder(String title, float scale, FBO.Type type, boolean isStatic) {
        this(title, frameBuffersManager.getFullScale().multiplyBy(scale), type);
        this.isStatic = isStatic;
    }

    public static void setFrameBuffersManager(FrameBuffersManager frameBuffersManager) {
        FBOBuilder.frameBuffersManager = frameBuffersManager;
    }

    public boolean isFBODynamic() {
        return !this.isStatic;
    }
/*
 *  * @param useDepthBuffer If true the FBO will have a 24 bit depth buffer attached to it. (GL_DEPTH_COMPONENT24, GL_UNSIGNED_INT, GL_NEAREST)
    * @param useNormalBuffer If true the FBO will have a 32 bit normals buffer attached to it. (GL_RGBA, GL_UNSIGNED_BYTE, GL_LINEAR)
    * @param useLightBuffer If true the FBO will have 32/64 bit light buffer attached to it, depending if Type is DEFAULT/HDR.
*                       (GL_RGBA/GL_RGBA16F_ARB, GL_UNSIGNED_BYTE/GL_HALF_FLOAT_ARB, GL_LINEAR)
    * @param useStencilBuffer If true the depth buffer will also have an 8 bit Stencil buffer associated with it.
    *                         (GL_DEPTH24_STENCIL8_EXT, GL_UNSIGNED_INT_24_8_EXT, GL_NEAREST)
                *                         */

    /**
     * Sets the builder to generate, allocate and attach a 24 bit depth buffer to the FrameBuffer to be built.
     * If useStencilBuffer() is also used, an 8 bit stencil buffer will also be associated with the depth buffer.
     * For details on the specific characteristics of the buffers, see the FBO.create() method.
     *
     * @return The calling instance, to chain calls, i.e.: new FBObuilder(...).useDepthBuffer().build();
     */
    public FBOBuilder useDepthBuffer() {
        useDepthBuffer = true;
        return this;
    }

    /**
     * Sets the builder to generate, allocate and attach a normals buffer to the FrameBuffer to be built.
     * For details on the specific characteristics of the buffer, see the FBO.create() method.
     *
     * @return The calling instance, to chain calls, i.e.: new FBObuilder(...).useNormalsBuffer().build();
     */
    public FBOBuilder useNormalBuffer() {
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
    public FBOBuilder useLightBuffer() {
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
    public FBOBuilder useStencilBuffer() {
        useStencilBuffer = true;
        return this;
    }

    /**
     * Given information set through the constructor and the use*Buffer() methods, builds and returns
     * an FBO instance, inclusive the underlying OpenGL FrameBuffer and any requested attachments.
     * <p>
     * The FBO is also automatically registered with the LwjglRenderingProcess through its title string.
     * This allows its retrieval and binding through methods such as getFBO(String title) and
     * bindFBO(String title). If another FBO is registered with the same title, it is disposed and
     * the new FBO registered in its place.
     * <p>
     * This method is effectively mono-use: calling it more than once will return the exact same FBO
     * returned the first time. To build a new FBO with identical or different characteristics it's
     * necessary to instantiate a new builder.
     *
     * @return An FBO. Make sure to check it with FBO.getStatus() before using it.
     */
    public FBO build() {
        if (generatedFBO != null) {
            return generatedFBO;
        }


        if (frameBuffersManager.isFBOAvailable(title)) {
            frameBuffersManager.dispose(title);
            frameBuffersManager.removeFromLookup(title);
            logger.warn("FBO " + title + " has been overwritten. Ideally it would have been deleted first.");
        }

        generatedFBO = FBO.create(title, dimensions, type, useDepthBuffer, useNormalBuffer, useLightBuffer, useStencilBuffer);
        handleIncompleteAndUnexpectedStatus(generatedFBO);
        frameBuffersManager.putToLookup(title, generatedFBO);
        return generatedFBO;
    }

    private void handleIncompleteAndUnexpectedStatus(FBO fbo) {
        // At this stage it's unclear what should be done in this circumstances as I (manu3d) do not know what
        // the effects of using an incomplete FrameBuffer are. Throw an exception? Live with visual artifacts?
        if (fbo.getStatus() == FBO.Status.INCOMPLETE) {
            logger.error("FBO " + title + " is incomplete. Look earlier in the log for details.");
        } else if (fbo.getStatus() == FBO.Status.UNEXPECTED) {
            logger.error("FBO " + title + " has generated an unexpected status code. Look earlier in the log for details.");
        }
    }

    public String getTitle() {
        return title;
    }
}
