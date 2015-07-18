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
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.common.ThreadManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.oculusVr.OculusVrHelper;
import org.terasology.rendering.opengl.FBO.Dimensions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.lwjgl.opengl.EXTFramebufferObject.glDeleteFramebuffersEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glDeleteRenderbuffersEXT;

/**
 * The Default Rendering Process class.
 *
 * @author Benjamin Glatzel
 */
public class LwjglRenderingProcess {

    private static final Logger logger = LoggerFactory.getLogger(LwjglRenderingProcess.class);

    private PBO readBackPBOFront;
    private PBO readBackPBOBack;
    private PBO readBackPBOCurrent;

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

    private int overwriteRtWidth;
    private int overwriteRtHeight;

    private String currentlyBoundFboName = "";
    private FBO currentlyBoundFbo;
    //private int currentlyBoundTextureId = -1;

    /* VARIOUS */
    private boolean isTakingScreenshot;

    // Note: this assumes that the settings in the configs might change at runtime,
    // but the config objects will not. At some point this might change, i.e. implementing presets.
    private Config config = CoreRegistry.get(Config.class);
    private RenderingConfig renderingConfig = config.getRendering();

    private Map<String, FBO> fboLookup = Maps.newHashMap();

    private GraphicState graphicState;
    private PostProcessor postProcessor;

    public LwjglRenderingProcess() {

    }

    public void initialize() {
        createOrUpdateFullscreenFbos();

        // Note: the FBObuilder takes care of registering thew new FBOs on fboLookup.
        new FBObuilder("scene16", 16, 16, FBO.Type.DEFAULT).build();
        new FBObuilder("scene8",   8,  8, FBO.Type.DEFAULT).build();
        new FBObuilder("scene4",   4,  4, FBO.Type.DEFAULT).build();
        new FBObuilder("scene2",   2,  2, FBO.Type.DEFAULT).build();
        new FBObuilder("scene1",   1,  1, FBO.Type.DEFAULT).build();

        postProcessor.obtainStaticFBOs();

        readBackPBOFront = new PBO(1, 1);
        readBackPBOBack = new PBO(1, 1);
        readBackPBOCurrent = readBackPBOFront;
    }

    public void setGraphicState(GraphicState graphicState) {
        this.graphicState = graphicState;
    }

    public void setPostProcessor(PostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    /**
     * Creates the scene FBOs and updates them according to the size of the viewport. The current size
     * provided by the display class is only used if the parameters overwriteRTWidth and overwriteRTHeight are set
     * to zero.
     */
    public void createOrUpdateFullscreenFbos() {

        if (overwriteRtWidth == 0) {
            fullScale = new Dimensions(Display.getWidth(), Display.getHeight());
        } else {
            fullScale = new Dimensions(overwriteRtWidth, overwriteRtHeight);
            if (renderingConfig.isOculusVrSupport()) {
                fullScale.multiplySelfBy(OculusVrHelper.getScaleFactor());
            }
        }

        fullScale.multiplySelfBy(renderingConfig.getFboScale() / 100f);

        halfScale    = fullScale.dividedBy(2);   // quarter resolution
        quarterScale = fullScale.dividedBy(4);   // one 16th resolution
        one8thScale  = fullScale.dividedBy(8);   // one 64th resolution
        one16thScale = fullScale.dividedBy(16);  // one 256th resolution
        one32thScale = fullScale.dividedBy(32);  // one 1024th resolution

        FBO scene = fboLookup.get("sceneOpaque");
        final boolean recreate = scene == null || (scene.dimensions().areDifferentFrom(fullScale));

        if (!recreate) {
            return;
        }

        // Note: the FBObuilder takes care of registering thew new FBOs on fboLookup.
        int shadowMapResolution = renderingConfig.getShadowMapResolution();
        FBO sceneShadowMap =
                new FBObuilder("sceneShadowMap", shadowMapResolution, shadowMapResolution, FBO.Type.NO_COLOR).useDepthBuffer().build();
        graphicState.setSceneShadowMap(sceneShadowMap);

        // buffers for the initial renderings
        FBO sceneOpaque =
                new FBObuilder("sceneOpaque", fullScale, FBO.Type.HDR).useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer().build();
        new FBObuilder("sceneOpaquePingPong", fullScale, FBO.Type.HDR).useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer().build();

        new FBObuilder("sceneSkyBand0", one16thScale, FBO.Type.DEFAULT).build();
        new FBObuilder("sceneSkyBand1", one32thScale, FBO.Type.DEFAULT).build();

        FBO sceneReflectiveRefractive = new FBObuilder("sceneReflectiveRefractive", fullScale, FBO.Type.HDR).useNormalBuffer().build();
        sceneOpaque.attachDepthBufferTo(sceneReflectiveRefractive);

        new FBObuilder("sceneReflected",  halfScale,    FBO.Type.DEFAULT).useDepthBuffer().build();

        // buffers for the prePost-Processing composite
        new FBObuilder("outline",         fullScale,    FBO.Type.DEFAULT).build();
        new FBObuilder("ssao",            fullScale,    FBO.Type.DEFAULT).build();
        new FBObuilder("ssaoBlurred",     fullScale,    FBO.Type.DEFAULT).build();

        // buffers for the Initial Post-Processing
        new FBObuilder("lightShafts",     halfScale,    FBO.Type.DEFAULT).build();
        new FBObuilder("initialPost",     fullScale,    FBO.Type.HDR).build();
        new FBObuilder("sceneToneMapped", fullScale,    FBO.Type.HDR).build();

        new FBObuilder("sceneHighPass",   fullScale,    FBO.Type.DEFAULT).build();
        new FBObuilder("sceneBloom0",     halfScale,    FBO.Type.DEFAULT).build();
        new FBObuilder("sceneBloom1",     quarterScale, FBO.Type.DEFAULT).build();
        new FBObuilder("sceneBloom2",     one8thScale,  FBO.Type.DEFAULT).build();

        new FBObuilder("sceneBlur0",      halfScale,    FBO.Type.DEFAULT).build();
        new FBObuilder("sceneBlur1",      halfScale,    FBO.Type.DEFAULT).build();

        // buffers for the Final Post-Processing
        new FBObuilder("ocUndistorted",   fullScale,    FBO.Type.DEFAULT).build();
        new FBObuilder("sceneFinal",      fullScale,    FBO.Type.DEFAULT).build();

        graphicState.refreshDynamicFBOs();
        postProcessor.refreshDynamicFBOs();
    }

    public void deleteFBO(String title) {
        if (fboLookup.containsKey(title)) {
            FBO fbo = fboLookup.get(title);

            glDeleteFramebuffersEXT(fbo.fboId);
            glDeleteRenderbuffersEXT(fbo.depthStencilRboId);
            GL11.glDeleteTextures(fbo.normalsBufferTextureId);
            GL11.glDeleteTextures(fbo.depthStencilTextureId);
            GL11.glDeleteTextures(fbo.colorBufferTextureId);
        }
    }

    public void takeScreenshot() {
        isTakingScreenshot = true;

        overwriteRtWidth = renderingConfig.getScreenshotSize().getWidth(Display.getWidth());
        overwriteRtHeight = renderingConfig.getScreenshotSize().getHeight(Display.getHeight());

        createOrUpdateFullscreenFbos();
    }

    public void saveScreenshot() {
        if (!isTakingScreenshot) {
            return;
        }

        final FBO fboSceneFinal = getFBO("sceneFinal");

        if (fboSceneFinal == null) {
            return;
        }

        final ByteBuffer buffer = BufferUtils.createByteBuffer(fboSceneFinal.width() * fboSceneFinal.height() * 4);

        fboSceneFinal.bindTexture();
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        fboSceneFinal.unbindTexture();

        Runnable task = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");

                final String format = renderingConfig.getScreenshotFormat().toString();
                final String fileName = "Terasology-" + sdf.format(new Date()) + "-" + fboSceneFinal.width() + "x" + fboSceneFinal.height() + "." + format;
                Path path = PathManager.getInstance().getScreenshotPath().resolve(fileName);
                BufferedImage image = new BufferedImage(fboSceneFinal.width(), fboSceneFinal.height(), BufferedImage.TYPE_INT_RGB);

                for (int x = 0; x < fboSceneFinal.width(); x++) {
                    for (int y = 0; y < fboSceneFinal.height(); y++) {
                        int i = (x + fboSceneFinal.width() * y) * 4;
                        int r = buffer.get(i) & 0xFF;
                        int g = buffer.get(i + 1) & 0xFF;
                        int b = buffer.get(i + 2) & 0xFF;
                        image.setRGB(x, fboSceneFinal.height() - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                    }
                }

                try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
                    ImageIO.write(image, format, out);
                    logger.info("Screenshot '" + fileName + "' saved! ");
                } catch (IOException e) {
                    logger.warn("Failed to save screenshot!", e);
                }
            }
        };

        CoreRegistry.get(ThreadManager.class).submitTask("Write screenshot", task);

        isTakingScreenshot = false;
        overwriteRtWidth = 0;
        overwriteRtHeight = 0;

        createOrUpdateFullscreenFbos();
    }

    public FBO getFBO(String title) {
        FBO fbo = fboLookup.get(title);

        if (fbo == null) {
            logger.error("Failed to retrieve FBO '" + title + "'!");
        }

        return fbo;
    }

    public boolean bindFbo(String title) {
        FBO fbo = fboLookup.get(title);

        if (fbo != null) {
            fbo.bind();
            currentlyBoundFboName = title;
            return true;
        }

        logger.error("Failed to bind FBO since the requested FBO could not be found!");
        return false;
    }

    public boolean unbindFbo(String title) {
        FBO fbo = fboLookup.get(title);

        if (fbo != null) {
            fbo.unbind();
            currentlyBoundFboName = "";
            return true;
        }

        logger.error("Failed to unbind FBO since the requested FBO could not be found!");
        return false;
    }

    public boolean bindFboColorTexture(String title) {
        FBO fbo = fboLookup.get(title);

        if (fbo != null) {
            fbo.bindTexture();
            return true;
        }

        logger.error("Failed to bind FBO color texture since the requested " + title + " FBO could not be found!");
        return false;
    }

    public boolean bindFboDepthTexture(String title) {
        FBO fbo = fboLookup.get(title);

        if (fbo != null) {
            fbo.bindDepthTexture();
            return true;
        }

        logger.error("Failed to bind FBO depth texture since the requested " + title + " FBO could not be found!");
        return false;
    }

    public boolean bindFboNormalsTexture(String title) {
        FBO fbo = fboLookup.get(title);

        if (fbo != null) {
            fbo.bindNormalsTexture();
            return true;
        }

        logger.error("Failed to bind FBO normals texture since the requested " + title + " FBO could not be found!");
        return false;
    }

    public boolean bindFboLightBufferTexture(String title) {
        FBO fbo = fboLookup.get(title);

        if (fbo != null) {
            fbo.bindLightBufferTexture();
            return true;
        }

        logger.error("Failed to bind FBO light buffer texture since the requested " + title + " FBO could not be found!");
        return false;
    }

    public void flipPingPongFbo(String title) {
        FBO fbo1 = getFBO(title);
        FBO fbo2 = getFBO(title + "PingPong");

        if (fbo1 == null || fbo2 == null) {
            return;
        }

        fboLookup.put(title, fbo2);
        fboLookup.put(title + "PingPong", fbo1);
    }

    public void swapSceneOpaqueFBOs() {
        FBO currentSceneOpaquePingPong = fboLookup.get("sceneOpaquePingPong");
        fboLookup.put("sceneOpaquePingPong", fboLookup.get("sceneOpaque"));
        fboLookup.put("sceneOpaque", currentSceneOpaquePingPong);

        graphicState.setSceneOpaqueFBO(currentSceneOpaquePingPong);
        postProcessor.refreshSceneOpaqueFBOs();
    }

    public void swapReadbackPBOs() {
        if (readBackPBOCurrent == readBackPBOFront) {
            readBackPBOCurrent = readBackPBOBack;
        } else {
            readBackPBOCurrent = readBackPBOFront;
        }
    }

    public PBO getCurrentReadbackPBO() {
        return readBackPBOCurrent;
    }

    public boolean isTakingScreenshot() {
        return isTakingScreenshot;
    }

    public boolean isNotTakingScreenshot() {
        return !isTakingScreenshot;
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

