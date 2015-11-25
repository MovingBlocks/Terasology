/*
 * Copyright 2015 MovingBlocks
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

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBHalfFloatPixel;
import org.lwjgl.opengl.ARBTextureFloat;
import org.lwjgl.opengl.EXTPackedDepthStencil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.glGenTextures;

/**
 * FBO - Frame Buffer Object
 *
 * FBOs wrap OpenGL's FrameBuffer functionality for the needs of the rendering portion of the engine.
 *
 * In OpenGL a FrameBuffer is an entity that can have a number of attachments, i.e. textures storing per-pixel color data.
 * By binding FrameBuffers and their attachments, shaders can read from or write to them. For example the final image
 * presented on screen is a composite of a number of visual layers stored in the attachments of different FrameBuffers.
 * Shaders read from these attachments, process the per-pixel data and eventually produce the image seen on screen.
 *
 * This class simplifies the creation of FrameBuffers with specific attachments (see the create() method), the binding
 * and unbinding of both the FrameBuffer as a whole or its attachments, and the FrameBuffer's proper disposal.
 */
public final class FBO {

    private static final Logger logger = LoggerFactory.getLogger(FBO.class);

    public int fboId;
    public int colorBufferTextureId;
    public int depthStencilTextureId;
    public int depthStencilRboId;
    public int normalsBufferTextureId;
    public int lightBufferTextureId;

    private final Dimensions dimensions;

    private Status status;

    public enum Type {
        DEFAULT,        // 32 bit color buffer
        HDR,            // 64 bit color buffer
        NO_COLOR        // no color buffer
    }

    public enum Status {
        COMPLETE,       // usable FBO
        INCOMPLETE,     // creation failed the OpenGL completeness check
        DISPOSED,       // no longer known to the GPU - can occur at creation time. See getStatus().
        UNEXPECTED      // creation failed in an unexpected way
    }

    // private constructor: the only way to generate an instance of this class
    //                      should be through the static create() method.
    private FBO(int width, int height) {
        dimensions = new Dimensions(width, height);
    }

    /**
     * Binds the FrameBuffer tracked by this FBO. The result of subsequent OpenGL draw calls will be stored
     * in the FrameBuffer's attachments until a different FrameBuffer is bound.
     */
    public void bind() {
        // Originally the code contained a check to prevent the currently bound FrameBuffer from being re-bound.
        // By my understanding current OpenGL implementations are smart enough to prevent it on their own. If
        // necessary, it'd be easy to add a class variable tracking the currently bound FrameBuffer and the
        // associated checks.
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboId);
    }

    /**
     * "Unbinding" a FrameBuffer can be more easily thought as binding the application's display,
     * i.e. the whole screen or an individual window. The result of subsequent OpenGL draw calls will
     * therefore be sent to the display until a different FrameBuffer is bound.
     */
    public void unbind() {
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }

    /**
     * Binds the color attachment to the currently active texture unit.
     * Once a texture is bound it can be sampled by shaders.
     */
    public void bindTexture() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorBufferTextureId);
    }

    /**
     * Binds the depth attachment to the currently active texture unit.
     * Once a texture is bound it can be sampled by shaders.
     */
    public void bindDepthTexture() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthStencilTextureId);
    }

    /**
     * Binds the normals attachment to the currently active texture unit.
     * Once a texture is bound it can be sampled by shaders.
     */
    public void bindNormalsTexture() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalsBufferTextureId);
    }

    /**
     * Binds the light buffer attachment to the currently active texture unit.
     * Once a texture is bound it can be sampled by shaders.
     */
    public void bindLightBufferTexture() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, lightBufferTextureId);
    }

    /**
     * Unbinds the texture attached to the currently active texture unit.
     * Quirk: this also works if the texture to be unbound is -not- an attachment
     * of the calling instance's FrameBuffer.
     */
    public static void unbindTexture() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    /**
     * Attaches the calling instance's depth attachments to the target FBO.
     * Notice that the depth attachments remain attached to the calling instance too.
     *
     * @param target The FBO to attach the depth attachments to.
     */
    public void attachDepthBufferTo(FBO target) {

        target.bind();

        glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, depthStencilRboId);
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL11.GL_TEXTURE_2D, depthStencilTextureId, 0);

        target.unbind();
    }

    /**
     * Properly disposes of the underlying FrameBuffer and its attachments,
     * effectively freeing memory on the graphic adapter.
     */
    public void dispose() {
        glDeleteFramebuffersEXT(fboId);
        glDeleteRenderbuffersEXT(depthStencilRboId);
        GL11.glDeleteTextures(normalsBufferTextureId);
        GL11.glDeleteTextures(depthStencilTextureId);
        GL11.glDeleteTextures(colorBufferTextureId);
        status = Status.DISPOSED;
    }

    /**
     * @return Returns the (int) width of the FrameBuffer, in pixels.
     */
    public int width() {
        return this.dimensions.width;
    }

    /**
     * @return Returns the (int) height of the FrameBuffer, in pixels.
     */
    public int height() {
        return this.dimensions.height;
    }

    /**
     * @return Returns the width and height of the FrameBuffer, as a Dimensions object.
     */
    public Dimensions dimensions() {
        return dimensions;
    }

    /**
     * Retrieves the status of the FBO.
     *
     * A usable FBO is one with a COMPLETE status.
     *
     * If the status is INCOMPLETE something went wrong during the allocation process on the GPU. Causes
     * can range from mismatched dimensions to missing attachments, among others. The precise error code
     * can be obtained browsing the log. Using an FrameBuffer that is not COMPLETE is an error and at this
     * stage it is probably unrecoverable. No exceptions are thrown however and it is up to the calling code
     * to decide how to react to an it.
     *
     * An FBO will have a DISPOSED status if the dispose() method has been called on it, which means the
     * underlying FrameBuffer is no longer available to the GPU. The FBO is also automatically
     * disposed if it is of Type.NO_COLOR and the internal call to glCheckFramebufferStatusEXT()
     * returns GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT. This occurs on some graphic cards and the
     * resulting FBO should not be used.
     *
     * An UNEXPECTED status cover all other (unknown) cases and the resulting FBO is probably as dysfunctional
     * as an INCOMPLETE or a DISPOSED one.
     *
     * @return  Status.COMPLETE, Status.INCOMPLETE, Status.DISPOSED or Status.UNEXPECTED
     */
    public Status getStatus() {
        return status;
    }

    private void setStatus(Status newStatus) {
        this.status = newStatus;
    }

    /**
     * Creates an FBO, allocating the underlying FrameBuffer and the desired attachments on the GPU.
     *
     * Also checks the resulting FBO for completeness and logs errors and their error codes as necessary.
     * Callers must check the returned FBO's status (see getStatus()). Only FBO with a Status.COMPLETE should be used.
     *
     * In what follows, the GL constants between parenthesis represent the (internal format, data type, filtering type) of a buffer.
     *
     * An FBO of Type.DEFAULT will have a 32 bit color buffer attached to it. (GL_RGBA, GL11.GL_UNSIGNED_BYTE, GL_LINEAR)
     * An FBO of Type.HDR will have a 64 bit color buffer attached to it. (GL_RGBA, GL_HALF_FLOAT_ARB, GL_LINEAR)
     * An FBO of Type.NO_COLOR will have -no- color buffer attached to it.
     *
     * If the creation process is successful (Status.COMPLETE) GPU memory has been allocated for the FrameBuffer and
     * its attachments. However, the content of the attachments is undefined.
     *
     * @param title An identification string. It is currently used only to log creation errors and is not stored in the FBO.
     * @param dimensions A Dimensions object wrapping width and height of the FBO.
     * @param type Can be Type.DEFAULT, Type.HDR or Type.NO_COLOR
     * @param useDepthBuffer If true the FBO will have a 24 bit depth buffer attached to it. (GL_DEPTH_COMPONENT24, GL_UNSIGNED_INT, GL_NEAREST)
     * @param useNormalBuffer If true the FBO will have a 32 bit normals buffer attached to it. (GL_RGBA, GL_UNSIGNED_BYTE, GL_LINEAR)
     * @param useLightBuffer If true the FBO will have 32/64 bit light buffer attached to it, depending if Type is DEFAULT/HDR.
     *                       (GL_RGBA/GL_RGBA16F_ARB, GL_UNSIGNED_BYTE/GL_HALF_FLOAT_ARB, GL_LINEAR)
     * @param useStencilBuffer If true the depth buffer will also have an 8 bit Stencil buffer associated with it.
     *                         (GL_DEPTH24_STENCIL8_EXT, GL_UNSIGNED_INT_24_8_EXT, GL_NEAREST)
     * @return The resuting FBO object wrapping a FrameBuffer and its attachments. Use getStatus() before use to verify completeness.
     */
    public static FBO create(String title, Dimensions dimensions, Type type,
                             boolean useDepthBuffer, boolean useNormalBuffer, boolean useLightBuffer, boolean useStencilBuffer) {
        FBO fbo = new FBO(dimensions.width, dimensions.height);

        // Create the FBO on the GPU
        fbo.fboId = glGenFramebuffersEXT();
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fbo.fboId);

        if (type != Type.NO_COLOR) {
            createColorBuffer(fbo, dimensions, type);
        }

        if (useNormalBuffer) {
            createNormalsBuffer(fbo, dimensions);
        }

        if (useLightBuffer) {
            createLightBuffer(fbo, dimensions, type);
        }

        if (useDepthBuffer) {
            createDepthBuffer(fbo, dimensions, useStencilBuffer);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        IntBuffer bufferIds = BufferUtils.createIntBuffer(3);
        if (type != Type.NO_COLOR) {
            bufferIds.put(GL_COLOR_ATTACHMENT0_EXT);
        }
        if (useNormalBuffer) {
            bufferIds.put(GL_COLOR_ATTACHMENT1_EXT);
        }
        if (useLightBuffer) {
            bufferIds.put(GL_COLOR_ATTACHMENT2_EXT);
        }
        bufferIds.flip();

        if (bufferIds.limit() == 0) {
            GL11.glReadBuffer(GL11.GL_NONE);
            GL20.glDrawBuffers(GL11.GL_NONE);
        } else {
            GL20.glDrawBuffers(bufferIds);
        }

        verifyCompleteness(title, type, fbo);
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);

        return fbo;
    }

    private static void verifyCompleteness(String title, Type type, FBO fbo) {
        int checkFB = glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT);
        switch (checkFB) {
            case GL_FRAMEBUFFER_COMPLETE_EXT:
                fbo.setStatus(Status.COMPLETE);
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT exception");
                fbo.setStatus(Status.INCOMPLETE);
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT exception");
                fbo.setStatus(Status.INCOMPLETE);
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT exception");
                fbo.setStatus(Status.INCOMPLETE);
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT exception");
                fbo.setStatus(Status.INCOMPLETE);
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT exception");
                fbo.setStatus(Status.INCOMPLETE);
                break;
            case GL_FRAMEBUFFER_UNSUPPORTED_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_UNSUPPORTED_EXT exception");
                fbo.setStatus(Status.INCOMPLETE);
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT exception");

            /*
             * On some graphics cards, FBO.Type.NO_COLOR can cause a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT.
             * Code using NO_COLOR FBOs should check for this and -not use- the FBO if its status is DISPOSED
             */
                if (type == Type.NO_COLOR) {
                    logger.error("FrameBuffer: " + title
                            + ", ...but the FBO.Type was NO_COLOR, ignoring this error and continuing without this FBO.");
                    fbo.dispose();
                } else {
                    fbo.setStatus(Status.INCOMPLETE);
                }
                break;

            default:
                logger.error("FBO '" + title + "' generated an unexpected reply from glCheckFramebufferStatusEXT: " + checkFB);
                fbo.setStatus(Status.UNEXPECTED);
                break;
        }
    }

    private static void createColorBuffer(FBO fbo, Dimensions dimensions, Type type) {
        fbo.colorBufferTextureId = glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.colorBufferTextureId);

        setTextureParameters(GL11.GL_LINEAR);

        if (type == Type.HDR) {
            allocateTexture(dimensions, GL11.GL_RGBA, GL11.GL_RGBA, ARBHalfFloatPixel.GL_HALF_FLOAT_ARB);
        } else {
            allocateTexture(dimensions, GL11.GL_RGBA, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
        }

        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, fbo.colorBufferTextureId, 0);
    }

    private static void createNormalsBuffer(FBO fbo, Dimensions dimensions) {
        fbo.normalsBufferTextureId = glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.normalsBufferTextureId);

        setTextureParameters(GL11.GL_LINEAR);

        allocateTexture(dimensions, GL11.GL_RGBA, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);

        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT1_EXT, GL11.GL_TEXTURE_2D, fbo.normalsBufferTextureId, 0);
    }

    private static void createLightBuffer(FBO fbo, Dimensions dimensions, Type type) {
        fbo.lightBufferTextureId = glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.lightBufferTextureId);

        setTextureParameters(GL11.GL_LINEAR);

        if (type == Type.HDR) {
            allocateTexture(dimensions, ARBTextureFloat.GL_RGBA16F_ARB, GL11.GL_RGBA, ARBHalfFloatPixel.GL_HALF_FLOAT_ARB);
        } else {
            allocateTexture(dimensions, GL11.GL_RGBA, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
        }

        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT2_EXT, GL11.GL_TEXTURE_2D, fbo.lightBufferTextureId, 0);
    }

    private static void createDepthBuffer(FBO fbo, Dimensions dimensions, boolean useStencilBuffer) {
        fbo.depthStencilTextureId = glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.depthStencilTextureId);

        setTextureParameters(GL11.GL_NEAREST);

        if (!useStencilBuffer) {
            allocateTexture(dimensions, GL14.GL_DEPTH_COMPONENT24, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_INT);
        } else {
            allocateTexture(dimensions,
                    EXTPackedDepthStencil.GL_DEPTH24_STENCIL8_EXT,
                    EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT,
                    EXTPackedDepthStencil.GL_UNSIGNED_INT_24_8_EXT);
        }

        fbo.depthStencilRboId = glGenRenderbuffersEXT();
        glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, fbo.depthStencilRboId);

        if (!useStencilBuffer) {
            glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT24, fbo.dimensions.width, fbo.dimensions.height);
        } else {
            glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH24_STENCIL8_EXT, fbo.dimensions.width, fbo.dimensions.height);
        }

        glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, 0);

        glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, fbo.depthStencilRboId);
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL11.GL_TEXTURE_2D, fbo.depthStencilTextureId, 0);

        if (useStencilBuffer) {
            glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_STENCIL_ATTACHMENT_EXT, GL11.GL_TEXTURE_2D, fbo.depthStencilTextureId, 0);
        }
    }

    private static void setTextureParameters(float filterType) {
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filterType);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filterType);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
    }

    private static void allocateTexture(Dimensions dimensions, int internalFormat, int dataFormat, int dataType) {
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, dimensions.width, dimensions.height, 0, dataFormat, dataType, (ByteBuffer) null);
    }

    /**
     * Support class wrapping width and height of FBOs. Also provides some ad-hoc methods to make code more readable.
     */
    public static class Dimensions {
        private int width;
        private int height;

        /**
         * Standard Constructor - returns a Dimensions object.
         *
         * @param width An integer, representing the width of the FBO in pixels.
         * @param height An integer, representing the height of the FBO in pixels.
         */
        public Dimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }

        /**
         * Returns a new Dimensions object whose width and height have been divided by the divisor.
         * I.e. new Dimensions(20,10).dividedBy(2) returns a Dimensions(10,5) object.
         * @param divisor An integer.
         * @return a new Dimensions object.
         */
        public Dimensions dividedBy(int divisor) {
            return new Dimensions(width / divisor, height / divisor);
        }

        /**
         * Multiplies (in place) both width and height of this Dimensions object by multiplier.
         * @param multiplier A float representing a multiplication factor.
         */
        public void multiplySelfBy(float multiplier) {
            width  *= multiplier;
            height *= multiplier;
        }

        /**
         * Returns true if the other instance of this class is null or has different width/height.
         * Similar to the more standard equals(), doesn't bother with checking if -other- is an instance
         * of Dimensions. It also makes for more readable code, i.e.:
         *
         * newDimensions.areDifferentFrom(oldDimensions)
         *
         * @param other A Dimensions object
         * @return True if the two objects are different as defined above.
         */
        public boolean areDifferentFrom(Dimensions other) {
            return other == null || this.width != other.width || this.height != other.height;
        }

        /**
         * Identical in behaviour to areDifferentFrom(Dimensions other),
         * in some situation can be more semantically appropriate, i.e.:
         *
         * newResolution.isDifferentFrom(oldResolution);
         *
         * @param other A Dimensions object.
         * @return True if the two objects are different as defined in the javadoc for areDifferentFrom(other).
         */
        public boolean isDifferentFrom(Dimensions other) {
            return areDifferentFrom(other);
        }

        /**
         * Returns the width.
         * @return An integer representing the width stored in the Dimensions instance.
         */
        public int width() {
            return this.width;
        }

        /**
         * Returns the height.
         * @return An integer representing the height stored in the Dimensions instance.
         */
        public int height() {
            return this.height;
        }
    }
}

