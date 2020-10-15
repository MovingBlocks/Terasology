// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.lwjgl;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.KHRDebugCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.module.ModuleAssetDataProducer;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.GameThread;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.RenderingSubsystemFactory;
import org.terasology.nui.canvas.CanvasRenderer;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.ShaderManagerLwjgl;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.animation.MeshAnimationBundle;
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
import org.terasology.rendering.nui.internal.LwjglCanvasRenderer;
import org.terasology.rendering.nui.internal.TerasologyCanvasRenderer;
import org.terasology.rendering.opengl.GLSLMaterial;
import org.terasology.rendering.opengl.GLSLShader;
import org.terasology.rendering.opengl.OpenGLMesh;
import org.terasology.rendering.opengl.OpenGLSkeletalMesh;
import org.terasology.rendering.opengl.OpenGLTexture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_NORMALIZE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexParameterf;
import static org.lwjgl.opengl.GL11.glViewport;

public class LwjglGraphics extends BaseLwjglSubsystem {
    private static final Logger logger = LoggerFactory.getLogger(LwjglGraphics.class);

    private final GLBufferPool bufferPool = new GLBufferPool(false);

    private final BlockingDeque<Runnable> displayThreadActions = Queues.newLinkedBlockingDeque();

    @In
    private ContextAwareClassFactory classFactory;

    @In
    private Config config;

    private RenderingConfig renderingConfig;

    private GameEngine engine;
    private LwjglDisplayDevice lwjglDisplay;

    public static void initOpenGLParams() {
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_NORMALIZE);
        glDepthFunc(GL_LEQUAL);
    }

    @Override
    public String getName() {
        return "Graphics";
    }

    @Override
    public void initialise(GameEngine gameEngine, Context rootContext) {
        logger.info("Starting initialization of LWJGL");
        this.engine = gameEngine;
        this.renderingConfig = config.getRendering();
        lwjglDisplay = classFactory.createInjectableInstance(LwjglDisplayDevice.class, DisplayDevice.class
        );
        logger.info("Initial initialization complete");
    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {

        // cast lambdas explicitly to avoid inconsistent compiler behavior wrt. type inference
        assetTypeManager.registerCoreAssetType(Font.class,
                FontImpl::new, "fonts");
        assetTypeManager.registerCoreAssetType(Texture.class, (urn, assetType, data) -> (new OpenGLTexture(urn,
                assetType, data, this)), "textures", "fonts");
        assetTypeManager.registerCoreFormat(Texture.class,
                new PNGTextureFormat(Texture.FilterMode.NEAREST, path -> {
                    if (path.getName(1).toString().equals(ModuleAssetDataProducer.OVERRIDE_FOLDER)) {
                        return path.getName(3).toString().equals("textures");
                    } else {
                        return path.getName(2).toString().equals("textures");
                    }
                }));
        assetTypeManager.registerCoreFormat(Texture.class,
                new PNGTextureFormat(Texture.FilterMode.LINEAR, path -> {
                    if (path.getName(1).toString().equals(ModuleAssetDataProducer.OVERRIDE_FOLDER)) {
                        return path.getName(3).toString().equals("fonts");
                    } else {
                        return path.getName(2).toString().equals("fonts");
                    }
                }));
        assetTypeManager.registerCoreAssetType(Shader.class,
                GLSLShader::new, "shaders");
        assetTypeManager.registerCoreAssetType(Material.class,
                GLSLMaterial::new, "materials");
        assetTypeManager.registerCoreAssetType(Mesh.class, (urn, assetType, data) -> new OpenGLMesh(urn, assetType,
                bufferPool, data), "mesh");
        assetTypeManager.registerCoreAssetType(SkeletalMesh.class,
                (urn, assetType, data) -> new OpenGLSkeletalMesh(urn, assetType, data, bufferPool), "skeletalMesh");
        assetTypeManager.registerCoreAssetType(MeshAnimation.class,
                MeshAnimationImpl::new, "animations", "skeletalMesh");
        assetTypeManager.registerCoreAssetType(Atlas.class,
                Atlas::new, "atlas");
        assetTypeManager.registerCoreAssetType(MeshAnimationBundle.class,
                MeshAnimationBundle::new, "skeletalMesh", "animations");
        assetTypeManager.registerCoreAssetType(Subtexture.class,
                Subtexture::new);
    }

    @Override
    public void postInitialise(Context rootContext) {
        classFactory.createInjectable(RenderingSubsystemFactory.class,
                () -> new LwjglRenderingSubsystemFactory(bufferPool));
        initDisplay();
        initOpenGL();
        CanvasRenderer renderer = classFactory.createInjectableInstance(LwjglCanvasRenderer.class, CanvasRenderer.class
        );
        rootContext.put(TerasologyCanvasRenderer.class, (TerasologyCanvasRenderer) renderer);
    }

    @Override
    public void postUpdate(GameState currentState, float delta) {
        Display.update();

        if (!displayThreadActions.isEmpty()) {
            List<Runnable> actions = Lists.newArrayListWithExpectedSize(displayThreadActions.size());
            displayThreadActions.drainTo(actions);
            actions.forEach(Runnable::run);
        }

        int frameLimit = renderingConfig.getFrameLimit();
        if (frameLimit > 0) {
            Display.sync(frameLimit);
        }
        currentState.render();

        lwjglDisplay.update();

        if (lwjglDisplay.isCloseRequested()) {
            engine.shutdown();
        }
    }

    @Override
    public void preShutdown() {
        if (Display.isCreated() && !Display.isFullscreen() && Display.isVisible()) {
            renderingConfig.setWindowPosX(Display.getX());
            renderingConfig.setWindowPosY(Display.getY());

            renderingConfig.setWindowWidth(Display.getWidth());
            renderingConfig.setWindowHeight(Display.getHeight());

        }
    }

    @Override
    public void shutdown() {
        Display.destroy();
    }

    private void initDisplay() {
        logger.info("Initializing display (if last line in log then likely the game crashed from an issue with your " +
                "video card)");

        try {

            lwjglDisplay.setDisplayModeSetting(renderingConfig.getDisplayModeSetting(), false);

            Display.setTitle("Terasology" + " | " + "Alpha");
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

            if (renderingConfig.getDebug().isEnabled()) {
                try {
                    ContextAttribs ctxAttribs = new ContextAttribs().withDebug(true);
                    Display.create(renderingConfig.getPixelFormat(), ctxAttribs);

                    try {
                        GL43.glDebugMessageCallback(new KHRDebugCallback(new DebugCallback()));
                    } catch (IllegalStateException e) {
                        logger.warn("Unable to specify DebugCallback to receive debugging messages from the GL.");
                    }

                } catch (LWJGLException e) {
                    logger.warn("Unable to create an OpenGL debug context. Maybe your graphics card does not support " +
                            "it.", e);
                    Display.create(renderingConfig.getPixelFormat()); // Create a normal context instead
                }

            } else {
                Display.create(renderingConfig.getPixelFormat());
            }

            Display.setVSyncEnabled(renderingConfig.isVSync());
        } catch (LWJGLException e) {
            throw new RuntimeException("Can not initialize graphics device.", e);
        }
    }

    private void initOpenGL() {
        logger.info("Initializing OpenGL");
        checkOpenGL();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        initOpenGLParams();
        classFactory.createInjectableInstance(ShaderManagerLwjgl.class, ShaderManager.class);
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
                GLContext.getCapabilities().GL_ARB_half_float_pixel};   // http://en.wikipedia
        // .org/wiki/OpenGL#OpenGL_3.0

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

    public void asynchToDisplayThread(Runnable action) {
        if (GameThread.isCurrentThread()) {
            action.run();
        } else {
            displayThreadActions.add(action);
        }
    }

    public void createTexture3D(ByteBuffer alignedBuffer, Texture.WrapMode wrapMode, Texture.FilterMode filterMode,
                                int size, Consumer<Integer> idConsumer) {
        asynchToDisplayThread(() -> {
            int id = glGenTextures();
            reloadTexture3D(id, alignedBuffer, wrapMode, filterMode, size);
            idConsumer.accept(id);
        });
    }

    public void reloadTexture3D(int id, ByteBuffer alignedBuffer, Texture.WrapMode wrapMode,
                                Texture.FilterMode filterMode, int size) {
        asynchToDisplayThread(() -> {
            glBindTexture(GL12.GL_TEXTURE_3D, id);

            glTexParameterf(GL12.GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, LwjglGraphicsUtil.getGLMode(wrapMode));
            glTexParameterf(GL12.GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, LwjglGraphicsUtil.getGLMode(wrapMode));
            glTexParameterf(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_WRAP_R, LwjglGraphicsUtil.getGLMode(wrapMode));

            GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MIN_FILTER,
                    LwjglGraphicsUtil.getGlMinFilter(filterMode));
            GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MAG_FILTER,
                    LwjglGraphicsUtil.getGlMagFilter(filterMode));

            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
            GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_MAX_LEVEL, 0);

            GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, GL11.GL_RGBA, size, size, size, 0, GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE, alignedBuffer);
        });
    }

    public void createTexture2D(ByteBuffer[] buffers, Texture.WrapMode wrapMode, Texture.FilterMode filterMode,
                                int width, int height, Consumer<Integer> idConsumer) {
        asynchToDisplayThread(() -> {
            int id = glGenTextures();
            reloadTexture2D(id, buffers, wrapMode, filterMode, width, height);
            idConsumer.accept(id);
        });
    }

    public void reloadTexture2D(int id, ByteBuffer[] buffers, Texture.WrapMode wrapMode,
                                Texture.FilterMode filterMode, int width, int height) {
        asynchToDisplayThread(() -> {
            glBindTexture(GL11.GL_TEXTURE_2D, id);

            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, LwjglGraphicsUtil.getGLMode(wrapMode));
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, LwjglGraphicsUtil.getGLMode(wrapMode));
            GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
                    LwjglGraphicsUtil.getGlMinFilter(filterMode));
            GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
                    LwjglGraphicsUtil.getGlMagFilter(filterMode));
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, buffers.length - 1);

            if (buffers.length > 0) {
                for (int i = 0; i < buffers.length; i++) {
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, i, GL11.GL_RGBA, width >> i, height >> i, 0, GL11.GL_RGBA,
                            GL11.GL_UNSIGNED_BYTE, buffers[i]);
                }
            } else {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA,
                        GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            }
        });
    }

    public void disposeTexture(int id) {
        asynchToDisplayThread(() -> glDeleteTextures(id));
    }
}
