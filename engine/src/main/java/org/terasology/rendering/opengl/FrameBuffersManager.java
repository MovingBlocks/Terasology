/*
 * Copyright 2013 MovingBlocks
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

import com.google.common.collect.Maps;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.oculusVr.OculusVrHelper;
import org.terasology.rendering.opengl.FBO.Dimensions;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * The FrameBuffersManager generates and maintains a number of Frame Buffer Objects (FBOs) used throughout the
 * rendering engine.
 *
 * In most instances Frame Buffers can be thought of as 2D arrays of pixels in GPU memory: shaders write to them or
 * read from them. Some buffers are static and never change for the lifetime of the manager. Some buffers are dynamic:
 * they get disposed and regenerated, i.e. in case the display resolution changes. Some buffers hold intermediate
 * steps of the rendering process and the content of one buffer, "sceneFinal", is eventually sent to the display.
 * <br/>
 * At this stage no buffer can be added or deleted: the list of buffers and their characteristics is hardcoded.
 * <br/>
 * The existing set of public methods is primarily intended to allow communication between this manager and other parts
 * of the rendering engine, most notably the PostProcessor and the GraphicState instances and the shaders system.
 * <br/>
 * An important exception is the takeScreenshot() method which prompts the renderer to eventually (not immediately)
 * redirect its output to a file. This is the only public method that is intended to be used from outside the
 * rendering engine.
 */
public class FrameBuffersManager {

    private static final Logger logger = LoggerFactory.getLogger(FrameBuffersManager.class);

    private PBO frontReadbackPBO;   // PBOs are 1x1 pixels buffers used to read GPU data back
    private PBO backReadbackPBO;    // into the CPU. This data is then used in the context of
    private PBO currentReadbackPBO; // eye adaptation.

    private FBO sceneOpaque;
    private FBO sceneShadowMap;

    // I could have named them fullResolution, halfResolution and so on. But halfScale is actually
    // -both- fullScale's dimensions halved, leading to -a quarter- of its resolution. Following
    // this logic one32thScale would have to be named one1024thResolution and the otherwise
    // straightforward connection between variable names and dimensions would have been lost. -- manu3d
    private Dimensions fullScale;
    private Dimensions halfScale;
    private Dimensions quarterScale;
    private Dimensions one8thScale;
    private Dimensions one16thScale;
    private Dimensions one32thScale;

    // Note: this assumes that the settings in the configs might change at runtime,
    // but the config objects will not. At some point this might change, i.e. implementing presets.
    private Config config = CoreRegistry.get(Config.class);
    private RenderingConfig renderingConfig = config.getRendering();

    private Map<String, FBO> fboLookup = Maps.newHashMap();

    private GraphicState graphicState;
    private PostProcessor postProcessor;

    public FrameBuffersManager() {
        // nothing to do here, everything happens at initialization time,
        // after GraphicState and PostProcessors have been set.
    }

    /**
    Initializes the FrameBuffersManager instance by creating static FBOs, dynamic FBOs and shadowMap FBO.
    Also instructs the PostProcessor and the GraphicState instances to fetch the FBOs they require.
     */
    public void initialize() {
        createStaticFBOs();
        postProcessor.obtainStaticFBOs();

        setDynamicFBOsDimensions();
        createDynamicFBOs();
        graphicState.refreshDynamicFBOs();
        postProcessor.refreshDynamicFBOs();

        createShadowMapFBO();
        graphicState.setSceneShadowMap(sceneShadowMap);
    }

    // Static FBOs do not change during the lifetime of a FrameBuffersManager instance.
    // They are used to progressively downsample the image all the way into a 1x1 buffer
    // holding average image brightness data. This is then used in the context of eye adaptation.
    private void createStaticFBOs() {
        // The FBObuilder takes care of registering the new FBOs on the fboLookup map.
        new FBObuilder("scene16", 16, 16, FBO.Type.DEFAULT).build();
        new FBObuilder("scene8",   8,  8, FBO.Type.DEFAULT).build();
        new FBObuilder("scene4",   4,  4, FBO.Type.DEFAULT).build();
        new FBObuilder("scene2",   2,  2, FBO.Type.DEFAULT).build();
        new FBObuilder("scene1",   1,  1, FBO.Type.DEFAULT).build();

        // Technically these are not Frame Buffer Objects but Pixel Buffer Objects.
        // Their instantiation and assignments are done here because they are static buffers.
        frontReadbackPBO = new PBO(1, 1);
        backReadbackPBO = new PBO(1, 1);
        currentReadbackPBO = frontReadbackPBO;
    }

    private void setDynamicFBOsDimensions() {
        refreshDynamicFBOsDimensions();
    }

    private void createDynamicFBOs() {
        recreateDynamicFBOs();
    }

    private void createShadowMapFBO() {
        recreateShadowMapFBO();
    }

    /**
     * Executed before any rendering begins, this method disposes and regenerates a number of FBOs if size changes
     * have been triggered. Also prompts the GraphicState and PostProcessor instances to refresh their internal
     * references to use the new FBOs.
     */
    public void preRenderUpdate() {
        refreshDynamicFBOsDimensions();
        if (sceneOpaque.dimensions().areDifferentFrom(fullScale)) {
            disposeOfAllDynamicFBOs();
            recreateDynamicFBOs();
            graphicState.refreshDynamicFBOs();
            postProcessor.refreshDynamicFBOs();
        }

        if (sceneShadowMap != null && sceneShadowMap.width() != renderingConfig.getShadowMapResolution()) {
            recreateShadowMapFBO();
            graphicState.setSceneShadowMap(sceneShadowMap);
        }
    }

    private boolean refreshDynamicFBOsDimensions() {
        boolean refreshed = false;
        Dimensions oldFullScale = fullScale;

        if (postProcessor.isNotTakingScreenshot()) {
            fullScale = new Dimensions(Display.getWidth(), Display.getHeight());
            if (renderingConfig.isOculusVrSupport()) {
                fullScale.multiplySelfBy(OculusVrHelper.getScaleFactor());
            }
        } else {
            fullScale = new Dimensions(
                    renderingConfig.getScreenshotSize().getWidth(Display.getWidth()),
                    renderingConfig.getScreenshotSize().getHeight(Display.getHeight())
            );
        }

        fullScale.multiplySelfBy(renderingConfig.getFboScale() / 100f);

        if (fullScale.isDifferentFrom(oldFullScale)) {
            halfScale    = fullScale.dividedBy(2);
            quarterScale = fullScale.dividedBy(4);
            one8thScale  = fullScale.dividedBy(8);
            one16thScale = fullScale.dividedBy(16);
            one32thScale = fullScale.dividedBy(32);
            refreshed = true;
        }

        return refreshed;
    }

    // providing a rough guide of each FBO here, as the method that creates them (recreateDynamicFBOs) is quite dense already
    private void disposeOfAllDynamicFBOs() {

        deleteFBO("sceneOpaque");           // Primary FBO: most visual information eventually ends up here
        deleteFBO("sceneOpaquePingPong");   // The sceneOpaque FBOs are swapped every frame, to use one for reading and the other for writing
                                            // Notice that these two FBOs hold a number of buffers, for color, depth, normals, etc.

        deleteFBO("sceneSkyBand0"); // two buffers used to generate a depth cue: things in the distance fades into the atmosphere's color.
        deleteFBO("sceneSkyBand1");

        deleteFBO("sceneReflectiveRefractive"); // used to render reflective and refractive surfaces, most obvious case being the water surface

        deleteFBO("sceneReflected"); // the water surface displays a reflected version of the scene. This version is stored here.

        deleteFBO("outline");       // greyscale depth-based rendering of object outlines
        deleteFBO("ssao");          // greyscale screen-space ambient occlusion rendering
        deleteFBO("ssaoBlurred");   // greyscale screen-space ambient occlusion rendering - blurred version
        deleteFBO("scenePrePost");  // intermediate step, combining a number of renderings made available so far
                                    // into one buffer to be post-processed.

        deleteFBO("lightShafts");       // light shafts rendering
        deleteFBO("sceneToneMapped");   // HDR tone mapping

        deleteFBO("sceneHighPass"); // a number of buffers to create the bloom effect
        deleteFBO("sceneBloom0");
        deleteFBO("sceneBloom1");
        deleteFBO("sceneBloom2");

        deleteFBO("sceneBlur0");    // a pair of buffers holding blurred versions of the rendered scene,
        deleteFBO("sceneBlur1");    // also used for the bloom effect, but not only.

        deleteFBO("ocUndistorted"); // if OculusRift support is enabled this buffer holds the side-by-side views
                                    // for each eye, with no lens distortion applied.

        deleteFBO("sceneFinal");    // the content of this buffer is eventually shown on the display or sent to a file if taking a screenshot
    }

    private void recreateDynamicFBOs() {
        // The FBObuilder takes care of registering thew new FBOs on the fboLookup hashmap.
        sceneOpaque = new FBObuilder("sceneOpaque", fullScale, FBO.Type.HDR).useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer().build();
        new FBObuilder("sceneOpaquePingPong", fullScale, FBO.Type.HDR).useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer().build();

        new FBObuilder("sceneSkyBand0",   one16thScale, FBO.Type.DEFAULT).build();
        new FBObuilder("sceneSkyBand1",   one32thScale, FBO.Type.DEFAULT).build();

        FBO sceneReflectiveRefractive = new FBObuilder("sceneReflectiveRefractive", fullScale, FBO.Type.HDR).useNormalBuffer().build();
        sceneOpaque.attachDepthBufferTo(sceneReflectiveRefractive);

        new FBObuilder("sceneReflected",  halfScale,    FBO.Type.DEFAULT).useDepthBuffer().build();

        new FBObuilder("outline",         fullScale,    FBO.Type.DEFAULT).build();
        new FBObuilder("ssao",            fullScale,    FBO.Type.DEFAULT).build();
        new FBObuilder("ssaoBlurred",     fullScale,    FBO.Type.DEFAULT).build();
        new FBObuilder("scenePrePost",    fullScale,    FBO.Type.HDR).build();

        new FBObuilder("lightShafts",     halfScale,    FBO.Type.DEFAULT).build();
        new FBObuilder("sceneToneMapped", fullScale,    FBO.Type.HDR).build();

        new FBObuilder("sceneHighPass",   fullScale,    FBO.Type.DEFAULT).build();
        new FBObuilder("sceneBloom0",     halfScale,    FBO.Type.DEFAULT).build();
        new FBObuilder("sceneBloom1",     quarterScale, FBO.Type.DEFAULT).build();
        new FBObuilder("sceneBloom2",     one8thScale,  FBO.Type.DEFAULT).build();

        new FBObuilder("sceneBlur0",      halfScale,    FBO.Type.DEFAULT).build();
        new FBObuilder("sceneBlur1",      halfScale,    FBO.Type.DEFAULT).build();

        new FBObuilder("ocUndistorted",   fullScale,    FBO.Type.DEFAULT).build();
        new FBObuilder("sceneFinal",      fullScale,    FBO.Type.DEFAULT).build();
    }

    private void recreateShadowMapFBO() {
        int shadowMapResFromSettings = renderingConfig.getShadowMapResolution();
        Dimensions shadowMapResolution =  new Dimensions(shadowMapResFromSettings, shadowMapResFromSettings);
        sceneShadowMap = new FBObuilder("sceneShadowMap", shadowMapResolution, FBO.Type.NO_COLOR).useDepthBuffer().build();
        handleDisposedShadowMap(sceneShadowMap);
    }

    private void handleDisposedShadowMap(FBO fbo) {
        if (fbo.getStatus() == FBO.Status.DISPOSED) {
            logger.warn("Failed to generate ShadowMap FBO. Turning off shadows.");
            renderingConfig.setDynamicShadows(false);
        }
    }

    /**
     * Returns the content of the color buffer of the FBO "sceneFinal", from GPU memory as a ByteBuffer.
     * If the FBO "sceneFinal" is unavailable, returns null.
     *
     * @return a ByteBuffer or null
     */
    public ByteBuffer getSceneFinalRawData() {
        FBO fboSceneFinal = getFBO("sceneFinal");
        if (fboSceneFinal == null) {
            logger.error("FBO sceneFinal is unavailable: cannot return data from it.");
            return null;
        }

        ByteBuffer buffer = BufferUtils.createByteBuffer(fboSceneFinal.width() * fboSceneFinal.height() * 4);

        fboSceneFinal.bindTexture();
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        FBO.unbindTexture();

        return buffer;
    }

    /**
     * Returns an FBO given its name.
     *
     * If no FBO maps to the given name, null is returned and an error is logged.
     *
     * @param fboName The name of the FBO
     * @return an FBO or null
     */
    public FBO getFBO(String fboName) {
        FBO fbo = fboLookup.get(fboName);

        if (fbo == null) {
            logger.error("Failed to retrieve FBO '" + fboName + "'!");
        }

        return fbo;
    }

    private void deleteFBO(String fboName) {
        FBO fbo = fboLookup.get(fboName);

        if (fbo != null) {
            fbo.dispose();
            fboLookup.remove(fboName);

        } else {
            logger.error("Failed to delete FBO '" + fboName + "': it doesn't exist!");
        }
    }

    /**
     * Binds the color texture of the FBO with the given name and returns true.
     *
     * If no FBO is associated with the given name, false is returned and an error is logged.
     *
     * @param fboName the name of an FBO
     * @return True if an FBO associated with the given name exists. False otherwise.
     */
    public boolean bindFboColorTexture(String fboName) {
        FBO fbo = fboLookup.get(fboName);

        if (fbo != null) {
            fbo.bindTexture();
            return true;
        }

        logger.error("Failed to bind FBO color texture since the requested " + fboName + " FBO could not be found!");
        return false;
    }

    /**
     * Binds the depth texture of the FBO with the given name and returns true.
     *
     * If no FBO is associated with the given name, false is returned and an error is logged.
     *
     * @param fboName the name of an FBO
     * @return True if an FBO associated with the given name exists. False otherwise.
     */
    public boolean bindFboDepthTexture(String fboName) {
        FBO fbo = fboLookup.get(fboName);

        if (fbo != null) {
            fbo.bindDepthTexture();
            return true;
        }

        logger.error("Failed to bind FBO depth texture since the requested " + fboName + " FBO could not be found!");
        return false;
    }

    /**
     * Binds the normals texture of the FBO with the given name and returns true.
     *
     * If no FBO is associated with the given name, false is returned and an error is logged.
     *
     * @param fboName the name of an FBO
     * @return True if an FBO associated with the given name exists. False otherwise.
     */
    public boolean bindFboNormalsTexture(String fboName) {
        FBO fbo = fboLookup.get(fboName);

        if (fbo != null) {
            fbo.bindNormalsTexture();
            return true;
        }

        logger.error("Failed to bind FBO normals texture since the requested " + fboName + " FBO could not be found!");
        return false;
    }

    /**
     * Binds the light buffer texture of the FBO with the given name and returns true.
     *
     * If no FBO is associated with the given name, false is returned and an error is logged.
     *
     * @param fboName the name of an FBO
     * @return True if an FBO associated with the given name exists. False otherwise.
     */
    public boolean bindFboLightBufferTexture(String fboName) {
        FBO fbo = fboLookup.get(fboName);

        if (fbo != null) {
            fbo.bindLightBufferTexture();
            return true;
        }

        logger.error("Failed to bind FBO light buffer texture since the requested " + fboName + " FBO could not be found!");
        return false;
    }

    /**
     * Swaps the sceneOpaque FBOs, so that the one previously used for writing is now used for reading and viceversa.
     */
    public void swapSceneOpaqueFBOs() {
        FBO currentSceneOpaquePingPong = fboLookup.get("sceneOpaquePingPong");
        fboLookup.put("sceneOpaquePingPong", fboLookup.get("sceneOpaque"));
        fboLookup.put("sceneOpaque", currentSceneOpaquePingPong);

        graphicState.setSceneOpaqueFBO(currentSceneOpaquePingPong);
        postProcessor.refreshSceneOpaqueFBOs();
    }

    /**
     * Swaps the readback PBOs, so that the one previously used for writing is now used for reading and viceversa.
     */
    public void swapReadbackPBOs() {
        if (currentReadbackPBO == frontReadbackPBO) {
            currentReadbackPBO = backReadbackPBO;
        } else {
            currentReadbackPBO = frontReadbackPBO;
        }
    }

    /**
     * Returns the current readback PBO, the one that will be used for reading from.
     * @return the current readback PBO
     */
    public PBO getCurrentReadbackPBO() {
        return currentReadbackPBO;
    }

    /**
     * Sets an internal reference to the GraphicState instance. This reference is used to inform the GraphicState
     * instance that changes have occurred and that it should refresh its references to the FBOs it uses.
     *
     * @param graphicState a GraphicState instance
     */
    public void setGraphicState(GraphicState graphicState) {
        this.graphicState = graphicState;
    }

    /**
     * Sets an internal reference to the PostProcessor instance. This reference is used to inform the PostProcessor
     * instance that changes have occurred and that it should refresh its references to the FBOs it uses.
     *
     * @param postProcessor a PostProcessor instance
     */
    public void setPostProcessor(PostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

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
    public class FBObuilder {

        private FBO generatedFBO;

        private String title;
        private FBO.Dimensions dimensions;
        private FBO.Type type;

        private boolean useDepthBuffer;
        private boolean useNormalBuffer;
        private boolean useLightBuffer;
        private boolean useStencilBuffer;

        /**
         * Constructs an FBO builder capable of building the two most basic FBOs:
         * an FBO with no attachments or one with a single color buffer attached to it.
         * <p>
         * To attach additional buffers, see the use*Buffer() methods.
         * <p>
         * Example: FBO basicFBO = new FBObuilder("basic", new Dimensions(1920, 1080), Type.DEFAULT).build();
         *
         * @param title A string identifier, the title is used to later manipulate the FBO through
         *              methods such as LwjglRenderingProcess.getFBO(title) and LwjglRenderingProcess.bindFBO(title).
         * @param dimensions A Dimensions object providing width and height information.
         * @param type Type.DEFAULT will result in a 32 bit color buffer attached to the FBO. (GL_RGBA, GL11.GL_UNSIGNED_BYTE, GL_LINEAR)
         *             Type.HDR will result in a 64 bit color buffer attached to the FBO. (GL_RGBA, GL_HALF_FLOAT_ARB, GL_LINEAR)
         *             Type.NO_COLOR will result in -no- color buffer attached to the FBO
         *             (WARNING: this could result in an FBO with Status.DISPOSED - see FBO.getStatus()).
         */
        public FBObuilder(String title, FBO.Dimensions dimensions, FBO.Type type) {
            this.title = title;
            this.dimensions = dimensions;
            this.type = type;
        }

        /**
         * Same as the previous FBObuilder constructor, but taking in input
         * explicit, integer width and height instead of a Dimensions object.
         */
        public FBObuilder(String title, int width, int height, FBO.Type type) {
            this(title,  new FBO.Dimensions(width, height), type);
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
        public FBObuilder useDepthBuffer() {
            useDepthBuffer = true;
            return this;
        }

        /**
         * Sets the builder to generate, allocate and attach a normals buffer to the FrameBuffer to be built.
         * For details on the specific characteristics of the buffer, see the FBO.create() method.
         *
         * @return The calling instance, to chain calls, i.e.: new FBObuilder(...).useNormalsBuffer().build();
         */
        public FBObuilder useNormalBuffer() {
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
        public FBObuilder useLightBuffer() {
            useLightBuffer = true;
            return this;
        }

        /**
         * -IF- the builder has been set to generate a depth buffer, using this method sets the builder to
         * generate a depth buffer inclusive of stencil buffer, with the following characteristics:
         * internal format GL_DEPTH24_STENCIL8_EXT, data type GL_UNSIGNED_INT_24_8_EXT and filtering GL_NEAREST.
         *
         * @return The calling instance of FBObuilder, to chain calls,
         *         i.e.: new FBObuilder(...).useDepthBuffer().useStencilBuffer().build();
         */
        public FBObuilder useStencilBuffer() {
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

            FBO oldFBO = fboLookup.get(title);
            if (oldFBO != null) {
                oldFBO.dispose();
                fboLookup.remove(title);
                logger.warn("FBO " + title + " has been overwritten. Ideally it would have been deleted first.");
            }

            generatedFBO = FBO.create(title, dimensions, type, useDepthBuffer, useNormalBuffer, useLightBuffer, useStencilBuffer);
            handleIncompleteAndUnexpectedStatus(generatedFBO);
            fboLookup.put(title, generatedFBO);
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
    }
}

