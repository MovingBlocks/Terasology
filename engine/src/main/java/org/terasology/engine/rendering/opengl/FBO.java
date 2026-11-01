// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.SimpleUri;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
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
    private static final boolean DEFAULT_COLOR_MASK = true;
    private static final boolean DEFAULT_NORMAL_MASK = true;
    private static final boolean DEFAULT_LIGHT_BUFFER_MASK = true;
    private static final Logger logger = LoggerFactory.getLogger(FBO.class);

    private SimpleUri fboName;
    private int fboId;
    private int colorBufferTextureId;
    private int depthStencilTextureId;
    private int depthStencilRboId;
    private int normalsBufferTextureId;
    private int lightBufferTextureId;

    private Dimensions dimensions;
    private boolean writeToColorBuffer;
    private boolean writeToNormalsBuffer;
    private boolean writeToLightBuffer;

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
    private FBO(SimpleUri fboName, int width, int height) {
        this.fboName = fboName;
        dimensions = new Dimensions(width, height);
        writeToColorBuffer = DEFAULT_COLOR_MASK;
        writeToNormalsBuffer = DEFAULT_NORMAL_MASK;
        writeToLightBuffer = DEFAULT_LIGHT_BUFFER_MASK;
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
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboId);
    }

    /**
     * Once an FBO is bound, opengl commands will act on it, i.e. by drawing on it.
     * Meanwhile shaders might output not just colors but additional per-pixel data. This method establishes on which
     * of an FBOs attachments, subsequent opengl commands and shaders will draw on.
     *
     * @param renderToColorBuffer If True the color buffer is set as drawable.
     * If false subsequent commands and shaders won't be able to draw on it.
     * @param renderToNormalsBuffer If True the normal buffer is set as drawable.
     * If false subsequent commands and shaders won't be able to draw on it.
     * @param renderToLightBuffer If True the light buffer is set as drawable.
     * If false subsequent commands and shaders won't be able to draw on it.
     */
    public void setRenderBufferMask(boolean renderToColorBuffer, boolean renderToNormalsBuffer, boolean renderToLightBuffer) {
        if (this.writeToColorBuffer == renderToColorBuffer
                && this.writeToNormalsBuffer == renderToNormalsBuffer
                && this.writeToLightBuffer == renderToLightBuffer) {
            return;
        }

        this.writeToColorBuffer = renderToColorBuffer;
        this.writeToNormalsBuffer = renderToNormalsBuffer;
        this.writeToLightBuffer = renderToLightBuffer;

        int attachmentId = 0;

        IntBuffer bufferIds = BufferUtils.createIntBuffer(3);

        // TODO: change GL_COLOR_ATTACHMENT0_EXT + attachmentId into something like COLOR_BUFFER_ATTACHMENT,
        // TODO: in turn set within the class or method
        if (colorBufferTextureId != 0) {
            if (this.writeToColorBuffer) {
                bufferIds.put(GL30.GL_COLOR_ATTACHMENT0 + attachmentId);
            }
            attachmentId++;
        }
        if (normalsBufferTextureId != 0) {
            if (this.writeToNormalsBuffer) {
                bufferIds.put(GL30.GL_COLOR_ATTACHMENT0 + attachmentId);
            }
            attachmentId++;
        }
        if (lightBufferTextureId != 0 && this.writeToLightBuffer) { // compacted if block because Jenkins was complaining about it.
            bufferIds.put(GL30.GL_COLOR_ATTACHMENT0 + attachmentId);
        }

        bufferIds.flip();

        GL20.glDrawBuffers(bufferIds);
    }

    /**
     * "Unbinding" a FrameBuffer can be more easily thought as binding the application's display,
     * i.e. the whole screen or an individual window. The result of subsequent OpenGL draw calls will
     * therefore be sent to the display until a different FrameBuffer is bound.
     */
    public void unbind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    /**
     * Binds the color attachment to the currently active texture unit.
     * Once a texture is bound it can be sampled by shaders.
     */
    public void bindTexture() {
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, colorBufferTextureId);
    }

    /**
     * Binds the depth attachment to the currently active texture unit.
     * Once a texture is bound it can be sampled by shaders.
     */
    public void bindDepthTexture() {
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, depthStencilTextureId);
    }

    /**
     * Binds the normals attachment to the currently active texture unit.
     * Once a texture is bound it can be sampled by shaders.
     */
    public void bindNormalsTexture() {
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, normalsBufferTextureId);
    }

    /**
     * Binds the light buffer attachment to the currently active texture unit.
     * Once a texture is bound it can be sampled by shaders.
     */
    public void bindLightBufferTexture() {
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, lightBufferTextureId);
    }

    /**
     * Unbinds the texture attached to the currently active texture unit.
     * Quirk: this also works if the texture to be unbound is -not- an attachment
     * of the calling instance's FrameBuffer.
     */
    public static void unbindTexture() {
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
    }

    /**
     * Attaches the calling instance's depth attachments to the target FBO.
     * Notice that the depth attachments remain attached to the calling instance too.
     *
     * @param target The FBO to attach the depth attachments to.
     */
    public void attachDepthBufferTo(FBO target) {

        target.bind();

        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthStencilRboId);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_TEXTURE_2D, depthStencilTextureId, 0);

        target.unbind();
    }

    /**
     * Detaches the depth attachments of this FBO.
     */
    public void detachDepthBuffer() {
        bind();

        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, 0);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_TEXTURE_2D, 0, 0);

        unbind();
    }

    /**
     * Properly disposes of the underlying FrameBuffer and its attachments,
     * effectively freeing memory on the graphic adapter.
     */
    public void dispose() {
        GL30.glDeleteFramebuffers(fboId);
        GL30.glDeleteRenderbuffers(depthStencilRboId);
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

    public SimpleUri getName() {
        return fboName;
    }

    public int getId() {
        return fboId;
    }

    public int getColorBufferTextureId() {
        return colorBufferTextureId;
    }

    public int getDepthStencilTextureId() {
        return depthStencilTextureId;
    }

    public int getDepthStencilRboId() {
        return depthStencilRboId;
    }

    public int getNormalsBufferTextureId() {
        return normalsBufferTextureId;
    }

    public int getLightBufferTextureId() {
        return lightBufferTextureId;
    }

    /**
     * Creates an FBO, allocating the underlying FrameBuffer and the desired attachments on the GPU.
     *
     * Check FBO create(String title, Dimensions dimensions, Type type ...) for more.
     * @param config A FboConfig object that stores information used for creating FBO.
     * @return The resuting FBO object wrapping a FrameBuffer and its attachments. Use getStatus() before use to verify completeness.
     */
    public static FBO create(FboConfig config) {
        return FBO.create(config.getName(),
                config.getDimensions(),
                config.getType(),
                config.hasDepthBuffer(),
                config.hasNormalBuffer(),
                config.hasLightBuffer(),
                config.hasStencilBuffer());
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
     * @param fboName A SimpleUri that can be used to uniquely identify the FBO.
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
    public static FBO create(SimpleUri fboName, Dimensions dimensions, Type type,
                             boolean useDepthBuffer, boolean useNormalBuffer, boolean useLightBuffer, boolean useStencilBuffer) {
        FBO fbo = new FBO(fboName, dimensions.width, dimensions.height);

        // Create the FBO on the GPU
        fbo.fboId = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo.fboId);

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
            bufferIds.put(GL30.GL_COLOR_ATTACHMENT0);
        }
        if (useNormalBuffer) {
            bufferIds.put(GL30.GL_COLOR_ATTACHMENT1);
        }
        if (useLightBuffer) {
            bufferIds.put(GL30.GL_COLOR_ATTACHMENT2);
        }
        bufferIds.flip();

        if (bufferIds.limit() == 0) {
            GL11.glReadBuffer(GL11.GL_NONE);
            GL20.glDrawBuffers(GL11.GL_NONE);
        } else {
            GL20.glDrawBuffers(bufferIds);
        }

        verifyCompleteness(fboName, type, fbo);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        return fbo;
    }

    public static void recreate(FBO fbo, FboConfig fboConfig) {
        Type type = fboConfig.getType();
        Dimensions dimensions = fboConfig.getDimensions();
        boolean useNormalBuffer = fboConfig.hasNormalBuffer();
        boolean useLightBuffer = fboConfig.hasLightBuffer();
        boolean useDepthBuffer = fboConfig.hasDepthBuffer();
        boolean useStencilBuffer = fboConfig.hasStencilBuffer();

        fbo.dimensions = fboConfig.getDimensions();

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo.fboId);

        if (type != Type.NO_COLOR) {
            glDeleteTextures(fbo.colorBufferTextureId);
            createColorBuffer(fbo, dimensions, type);
        }

        if (useNormalBuffer) {
            glDeleteTextures(fbo.normalsBufferTextureId);
            createNormalsBuffer(fbo, dimensions);
        }

        if (useLightBuffer) {
            glDeleteTextures(fbo.lightBufferTextureId);
            createLightBuffer(fbo, dimensions, type);
        }

        if (useDepthBuffer) {
            glDeleteTextures(fbo.depthStencilTextureId);
            GL30.glDeleteRenderbuffers(fbo.depthStencilRboId);
            createDepthBuffer(fbo, dimensions, useStencilBuffer);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        IntBuffer bufferIds = BufferUtils.createIntBuffer(3);
        if (type != Type.NO_COLOR) {
            bufferIds.put(GL30.GL_COLOR_ATTACHMENT0);
        }
        if (useNormalBuffer) {
            bufferIds.put(GL30.GL_COLOR_ATTACHMENT1);
        }
        if (useLightBuffer) {
            bufferIds.put(GL30.GL_COLOR_ATTACHMENT2);
        }
        bufferIds.flip();

        if (bufferIds.limit() == 0) {
            GL11.glReadBuffer(GL11.GL_NONE);
            GL20.glDrawBuffers(GL11.GL_NONE);
        } else {
            GL20.glDrawBuffers(bufferIds);
        }

        verifyCompleteness(fboConfig.getName(), type, fbo);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    @Override
    public String toString() {
        return "FBO: " + this.fboName;
    }

    private static void verifyCompleteness(SimpleUri urn, Type type, FBO fbo) {
        int checkFB = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        switch (checkFB) {
            case GL30.GL_FRAMEBUFFER_COMPLETE:
                fbo.setStatus(Status.COMPLETE);
                break;
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                logger.error("FrameBuffer: {}, has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT exception", urn);
                fbo.setStatus(Status.INCOMPLETE);
                break;
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                logger.error("FrameBuffer: {}, has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT exception", urn);
                fbo.setStatus(Status.INCOMPLETE);
                break;
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                logger.error("FrameBuffer: {}, has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT exception", urn);
                fbo.setStatus(Status.INCOMPLETE);
                break;
            case GL30.GL_FRAMEBUFFER_UNSUPPORTED:
                logger.error("FrameBuffer: {}, has caused a GL_FRAMEBUFFER_UNSUPPORTED_EXT exception", urn);
                fbo.setStatus(Status.INCOMPLETE);
                break;
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                logger.error("FrameBuffer: {}, has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT exception", urn);

            /*
             * On some graphics cards, FBO.Type.NO_COLOR can cause a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT.
             * Code using NO_COLOR FBOs should check for this and -not use- the FBO if its status is DISPOSED
             */
                if (type == Type.NO_COLOR) {
                    logger.error("FrameBuffer: {}, ...but the FBO.Type was NO_COLOR, ignoring this error and continuing without this FBO.",
                            urn);
                    fbo.dispose();
                } else {
                    fbo.setStatus(Status.INCOMPLETE);
                }
                break;

            default:
                logger.error("FBO '{}' generated an unexpected reply from glCheckFramebufferStatusEXT: {}", urn, checkFB);
                fbo.setStatus(Status.UNEXPECTED);
                break;
        }
    }

    /**
     * Returns the content of the color buffer from GPU memory as a ByteBuffer.
     * @return a ByteBuffer or null
     */
    public ByteBuffer getColorBufferRawData() {
        ByteBuffer buffer = BufferUtils.createByteBuffer(this.width() * this.height() * 4);

        this.bindTexture();
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        FBO.unbindTexture();

        return buffer;
    }

    private static void createColorBuffer(FBO fbo, Dimensions dimensions, Type type) {
        fbo.colorBufferTextureId = glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.colorBufferTextureId);

        setTextureParameters(GL11.GL_LINEAR);

        if (type == Type.HDR) {
            allocateTexture(dimensions, GL11.GL_RGBA, GL11.GL_RGBA, GL30.GL_HALF_FLOAT);
        } else {
            allocateTexture(dimensions, GL11.GL_RGBA, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
        }

        GL30.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, fbo.colorBufferTextureId, 0);
    }

    private static void createNormalsBuffer(FBO fbo, Dimensions dimensions) {
        fbo.normalsBufferTextureId = glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.normalsBufferTextureId);

        setTextureParameters(GL11.GL_LINEAR);

        allocateTexture(dimensions, GL11.GL_RGBA, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);


        GL30.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1, GL30.GL_TEXTURE_2D, fbo.normalsBufferTextureId, 0);
    }

    private static void createLightBuffer(FBO fbo, Dimensions dimensions, Type type) {
        fbo.lightBufferTextureId = glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.lightBufferTextureId);

        setTextureParameters(GL11.GL_LINEAR);

        if (type == Type.HDR) {
            allocateTexture(dimensions, GL30.GL_RGBA16F, GL11.GL_RGBA, GL30.GL_HALF_FLOAT);
        } else {
            allocateTexture(dimensions, GL11.GL_RGBA, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
        }

        GL30.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT2, GL30.GL_TEXTURE_2D, fbo.lightBufferTextureId, 0);
    }

    private static void createDepthBuffer(FBO fbo, Dimensions dimensions, boolean useStencilBuffer) {
        fbo.depthStencilTextureId = glGenTextures();
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, fbo.depthStencilTextureId);

        setTextureParameters(GL30.GL_NEAREST);

        if (!useStencilBuffer) {
            allocateTexture(dimensions, GL30.GL_DEPTH_COMPONENT32F, GL30.GL_DEPTH_COMPONENT, GL30.GL_FLOAT);
            float[] borderColor = {1.0f, 1.0f, 1.0f, 1.0f};
            GL30.glTexParameterfv(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_BORDER_COLOR, borderColor);
        } else {
            allocateTexture(dimensions,
                    GL30.GL_DEPTH24_STENCIL8,
                    GL30.GL_DEPTH_STENCIL,
                    GL30.GL_UNSIGNED_INT_24_8);
        }

        fbo.depthStencilRboId = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, fbo.depthStencilRboId);

        if (!useStencilBuffer) {
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, fbo.dimensions.width, fbo.dimensions.height);
        } else {
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, fbo.dimensions.width, fbo.dimensions.height);
        }

        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);

        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, fbo.depthStencilRboId);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_TEXTURE_2D, fbo.depthStencilTextureId, 0);

        if (useStencilBuffer) {
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL11.GL_TEXTURE_2D, fbo.depthStencilTextureId, 0);
        }
    }

    private static void setTextureParameters(float filterType) {
        GL30.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filterType);
        GL30.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filterType);
        GL30.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL30.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
    }

    private static void allocateTexture(Dimensions dimensions, int internalFormat, int dataFormat, int dataType) {
        GL30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, dimensions.width, dimensions.height,
                0, dataFormat, dataType, (ByteBuffer) null);
    }

    /**
     * Support class wrapping width and height of FBOs. Also provides some ad-hoc methods to make code more readable.
     */
    public static class Dimensions {
        private int width;
        private int height;

        /**
         * Default Constructor - returns a Dimensions object.
         */
        public Dimensions() {
            this.width = 0;
            this.height = 0;
        }

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
         * Copy constructor: construct a Dimensions instance with the dimensions of another.
         *
         * @param dimensions a Dimensions instance
         */
        public Dimensions(Dimensions dimensions) {
            this(dimensions.width(), dimensions.height());
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

        public Dimensions multiplyBy(float multiplier) {
            int w = (int) (width * multiplier);
            int h = (int) (height * multiplier);
            return new Dimensions(w, h);
        }

        /**
         * Multiplies (in place) both width and height of this Dimensions object by multiplier.
         * @param multiplier A float representing a multiplication factor.
         */
        public void multiplySelfBy(float multiplier) {
            width  = (int) (width * multiplier);
            height = (int) (height * multiplier);
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

        /**
         * Updates the dimensions.
         * @param width An integer representing the new width.
         * @param height An integer representing the new height.
         */
        public void setDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }

        /**
         * Updates the dimensions.
         * @param other A Dimension to use the width and height from.
         */
        public void setDimensions(Dimensions other) {
            this.width = other.width;
            this.height = other.height;
        }
    }
}

