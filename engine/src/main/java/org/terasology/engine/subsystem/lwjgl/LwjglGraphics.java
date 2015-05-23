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
package org.terasology.engine.subsystem.lwjgl;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.KHRDebugCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.RenderingSubsystemFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.ShaderManagerLwjgl;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.animation.MeshAnimationImpl;
import org.terasology.rendering.assets.atlas.Atlas;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.font.FontImpl;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.assets.texture.PNGTextureFormat;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureUtil;
import org.terasology.rendering.assets.texture.subtexture.Subtexture;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.internal.LwjglCanvasRenderer;
import org.terasology.rendering.nui.internal.NUIManagerInternal;
import org.terasology.rendering.opengl.GLSLMaterial;
import org.terasology.rendering.opengl.GLSLShader;
import org.terasology.rendering.opengl.OpenGLMesh;
import org.terasology.rendering.opengl.OpenGLSkeletalMesh;
import org.terasology.rendering.opengl.OpenGLTexture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_NORMALIZE;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;

public class LwjglGraphics extends BaseLwjglSubsystem {

    private static final Logger logger = LoggerFactory.getLogger(LwjglGraphics.class);

    private GLBufferPool bufferPool = new GLBufferPool(false);

    @Override
    public void initialise(Config config) {

    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
        assetTypeManager.registerCoreAssetType(Font.class, FontImpl::new, "fonts");
        assetTypeManager.registerCoreAssetType(Texture.class, OpenGLTexture::new, "textures", "fonts");
        assetTypeManager.registerCoreFormat(Texture.class, new PNGTextureFormat(Texture.FilterMode.NEAREST, path -> path.getName(1).toString().equals("textures")));
        assetTypeManager.registerCoreFormat(Texture.class, new PNGTextureFormat(Texture.FilterMode.LINEAR, path -> path.getName(1).toString().equals("fonts")));

        assetTypeManager.registerCoreAssetType(Shader.class, GLSLShader::new, "shaders");
        assetTypeManager.registerCoreAssetType(Material.class, GLSLMaterial::new, "materials");
        assetTypeManager.registerCoreAssetType(Mesh.class, (urn, assetType, data) -> new OpenGLMesh(urn, assetType, bufferPool, data), "mesh");
        assetTypeManager.registerCoreAssetType(SkeletalMesh.class, (urn, assetType, data) -> new OpenGLSkeletalMesh(urn, assetType, data, bufferPool), "skeletalMesh");
        assetTypeManager.registerCoreAssetType(MeshAnimation.class, MeshAnimationImpl::new, "animations");
        assetTypeManager.registerCoreAssetType(Atlas.class, Atlas::new, "atlas");
        assetTypeManager.registerCoreAssetType(Subtexture.class, Subtexture::new);
    }

    @Override
    public void postInitialise(Config config) {
        CoreRegistry.putPermanently(RenderingSubsystemFactory.class, new LwjglRenderingSubsystemFactory(bufferPool));

        LwjglDisplayDevice lwjglDisplay = new LwjglDisplayDevice();
        CoreRegistry.putPermanently(DisplayDevice.class, lwjglDisplay);

        initDisplay(config, lwjglDisplay);
        initOpenGL();

        CoreRegistry.putPermanently(NUIManager.class, new NUIManagerInternal(new LwjglCanvasRenderer()));
    }

    @Override
    public void preUpdate(GameState currentState, float delta) {
    }

    @Override
    public void postUpdate(GameState currentState, float delta) {
        Display.update();

        int frameLimit = CoreRegistry.get(Config.class).getRendering().getFrameLimit();
        if (frameLimit > 0) {
            Display.sync(frameLimit);
        }
        currentState.render();

        if (Display.wasResized()) {
            glViewport(0, 0, Display.getWidth(), Display.getHeight());
        }
    }

    @Override
    public void shutdown(Config config) {
        if (Display.isCreated() && !Display.isFullscreen() && Display.isVisible()) {
            config.getRendering().setWindowPosX(Display.getX());
            config.getRendering().setWindowPosY(Display.getY());
        }
    }

    @Override
    public void dispose() {
        Display.destroy();
    }

    private void initDisplay(Config config, LwjglDisplayDevice lwjglDisplay) {
        try {
            lwjglDisplay.setFullscreen(config.getRendering().isFullscreen(), false);

            RenderingConfig rc = config.getRendering();
            Display.setLocation(rc.getWindowPosX(), rc.getWindowPosY());
            Display.setTitle("Terasology" + " | " + "Pre Alpha");
            try {

                String root = "org/terasology/icons/";
                ClassLoader classLoader = getClass().getClassLoader();

                BufferedImage icon16 = ImageIO.read(classLoader.getResourceAsStream(root + "gooey_sweet_16.png"));
                BufferedImage icon32 = ImageIO.read(classLoader.getResourceAsStream(root + "gooey_sweet_32.png"));
                BufferedImage icon64 = ImageIO.read(classLoader.getResourceAsStream(root + "gooey_sweet_64.png"));
                BufferedImage icon128 = ImageIO.read(classLoader.getResourceAsStream(root + "gooey_sweet_128.png"));

                Display.setIcon(new ByteBuffer[]{
                        TextureUtil.convertToByteBuffer(icon16),
                        TextureUtil.convertToByteBuffer(icon32),
                        TextureUtil.convertToByteBuffer(icon64),
                        TextureUtil.convertToByteBuffer(icon128)
                });

            } catch (IOException | IllegalArgumentException e) {
                logger.warn("Could not set icon", e);
            }

            if (config.getRendering().getDebug().isEnabled()) {
                try {
                    ContextAttribs ctxAttribs = new ContextAttribs().withDebug(true);
                    Display.create(config.getRendering().getPixelFormat(), ctxAttribs);

                    GL43.glDebugMessageCallback(new KHRDebugCallback(new DebugCallback()));
                } catch (LWJGLException e) {
                    logger.warn("Unable to create an OpenGL debug context. Maybe your graphics card does not support it.", e);
                    Display.create(rc.getPixelFormat()); // Create a normal context instead
                }

            } else {
                Display.create(rc.getPixelFormat());
            }

            Display.setVSyncEnabled(rc.isVSync());
        } catch (LWJGLException e) {
            throw new RuntimeException("Can not initialize graphics device.", e);
        }
    }

    private void initOpenGL() {
        checkOpenGL();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        initOpenGLParams();
        CoreRegistry.putPermanently(ShaderManager.class, new ShaderManagerLwjgl());
    }

    private void checkOpenGL() {
        boolean[] requiredCapabilities = {
                GLContext.getCapabilities().OpenGL12,
                GLContext.getCapabilities().OpenGL14,
                GLContext.getCapabilities().OpenGL15,
                GLContext.getCapabilities().OpenGL20,
                GLContext.getCapabilities().OpenGL21,   // needed as we use GLSL 1.20

                GLContext.getCapabilities().GL_ARB_framebuffer_object,  // Extensions eventually included in
                GLContext.getCapabilities().GL_ARB_texture_float,       // OpenGl 3.0 according to
                GLContext.getCapabilities().GL_ARB_half_float_pixel};   // http://en.wikipedia.org/wiki/OpenGL#OpenGL_3.0

        String[] capabilityNames = {"OpenGL12",
                "OpenGL14",
                "OpenGL15",
                "OpenGL20",
                "OpenGL21",
                "GL_ARB_framebuffer_object",
                "GL_ARB_texture_float",
                "GL_ARB_half_float_pixel"};

        boolean canRunTheGame = true;
        String missingCapabilitiesMessage = "";

        for (int index = 0; index < requiredCapabilities.length; index++) {
            if (!requiredCapabilities[index]) {
                missingCapabilitiesMessage += "    - " + capabilityNames[index] + "\n";
                canRunTheGame = false;
            }
        }

        if (!canRunTheGame) {
            String completeErrorMessage = completeErrorMessage(missingCapabilitiesMessage);
            throw new IllegalStateException(completeErrorMessage);
        }
    }

    private String completeErrorMessage(String errorMessage) {
        return "\n" +
                "\nThe following OpenGL versions/extensions are required but are not supported by your GPU driver:\n" +
                "\n" +
                errorMessage +
                "\n" +
                "GPU Information:\n" +
                "\n" +
                "    Vendor:  " + GL11.glGetString(GL11.GL_VENDOR) + "\n" +
                "    Model:   " + GL11.glGetString(GL11.GL_RENDERER) + "\n" +
                "    Driver:  " + GL11.glGetString(GL11.GL_VERSION) + "\n" +
                "\n" +
                "Try updating the driver to the latest version available.\n" +
                "If that fails you might need to use a different GPU (graphics card). Sorry!\n";
    }

    public void initOpenGLParams() {
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_NORMALIZE);
        glDepthFunc(GL_LEQUAL);
    }

    @Override
    public void registerSystems(ComponentSystemManager componentSystemManager) {
    }

}
