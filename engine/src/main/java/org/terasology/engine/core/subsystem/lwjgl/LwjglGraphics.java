// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.glfw.GLFWNativeX11;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.RenderingConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.rendering.ShaderManager;
import org.terasology.engine.rendering.ShaderManagerLwjgl;
import org.terasology.engine.rendering.assets.animation.MeshAnimation;
import org.terasology.engine.rendering.assets.animation.MeshAnimationBundle;
import org.terasology.engine.rendering.assets.animation.MeshAnimationImpl;
import org.terasology.engine.rendering.assets.atlas.Atlas;
import org.terasology.engine.rendering.assets.font.Font;
import org.terasology.engine.rendering.assets.font.FontImpl;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.shader.Shader;
import org.terasology.engine.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.engine.rendering.assets.texture.PNGTextureFormat;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureData;
import org.terasology.engine.rendering.assets.texture.subtexture.Subtexture;
import org.terasology.engine.rendering.nui.internal.WgpuCanvasRenderer;
import org.terasology.engine.rendering.opengl.GLSLMaterial;
import org.terasology.engine.rendering.opengl.GLSLShader;
import org.terasology.engine.rendering.opengl.OpenGLMesh;
import org.terasology.engine.rendering.opengl.OpenGLSkeletalMesh;
import org.terasology.engine.rendering.opengl.WgpuTexture;
import org.terasology.engine.rust.EngineKernel;
import org.terasology.engine.utilities.OS;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.module.ModuleAssetScanner;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.nui.canvas.CanvasRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class LwjglGraphics extends BaseLwjglSubsystem {
    private static final Logger logger = LoggerFactory.getLogger(LwjglGraphics.class);

    // we don't use context so we need to
    public static long primaryWindow = 0;
    private EngineKernel kernel = null;

    private Context context;
    private RenderingConfig config;

    private GameEngine engine;
    private LwjglDisplayDevice lwjglDisplay;

    private LwjglGraphicsManager graphics = new LwjglGraphicsManager();

    @Override
    public String getName() {
        return "Graphics";
    }

    @Override
    public void initialise(GameEngine gameEngine, Context rootContext) {
        logger.info("Starting initialization of LWJGL");
        this.engine = gameEngine;
        this.context = rootContext;
        this.config = context.get(Config.class).getRendering();
        lwjglDisplay = new LwjglDisplayDevice(context);
        context.put(DisplayDevice.class, lwjglDisplay);
        logger.info("Initial initialization complete");
    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {

        AssetType<Texture, TextureData> texture = assetTypeManager.createAssetType(Texture.class,
                (ResourceUrn urn, AssetType<Texture, TextureData> assetType, TextureData data) -> {
                    WgpuTexture.TextureResources resources = new WgpuTexture.TextureResources(
                            data,
                            this.kernel.resource.createTexture(WgpuTexture.createDesc(data), data.getBuffers()[0])
                    );

                    return new WgpuTexture(this.kernel, urn, assetType, resources);
                }, "textures", "fonts");
        assetTypeManager.getAssetFileDataProducer(texture).addAssetFormat(
                new PNGTextureFormat(Texture.FilterMode.NEAREST, path -> {
                    if (path.getPath().get(0).equals(ModuleAssetScanner.OVERRIDE_FOLDER)) {
                        return path.getPath().get(2).equals("textures");
                    } else {
                        return path.getPath().get(1).equals("textures");
                    }
                }));
        assetTypeManager.getAssetFileDataProducer(texture).addAssetFormat(
                new PNGTextureFormat(Texture.FilterMode.LINEAR, path -> {
                    if (path.getPath().get(0).equals(ModuleAssetScanner.OVERRIDE_FOLDER)) {
                        return path.getPath().get(2).equals("fonts");
                    } else {
                        return path.getPath().get(1).equals("fonts");
                    }
                }));
        assetTypeManager.createAssetType(Font.class,
                FontImpl::new, "fonts");

        assetTypeManager.createAssetType(Shader.class, (urn, assetType, data) -> GLSLShader.create(urn, assetType, data, graphics), "shaders");
        assetTypeManager.createAssetType(Material.class, (urn, assetType, data) ->
                        GLSLMaterial.create(urn, graphics, assetType, data),
                "materials");
        assetTypeManager.createAssetType(Mesh.class, (urn, assetType, data) -> OpenGLMesh.create(urn, assetType, data, graphics),
                "mesh");
        assetTypeManager.createAssetType(SkeletalMesh.class,
                (urn, assetType, data) ->
                        OpenGLSkeletalMesh.create(urn, assetType, data, graphics),
                "skeletalMesh");
        assetTypeManager.createAssetType(MeshAnimation.class, MeshAnimationImpl::new,
                "animations", "skeletalMesh");
        assetTypeManager.createAssetType(Atlas.class, Atlas::new, "atlas");
        assetTypeManager.createAssetType(MeshAnimationBundle.class, MeshAnimationBundle::new,
                "skeletalMesh", "animations");
        assetTypeManager.createAssetType(Subtexture.class, Subtexture::new);
    }

    @Override
    public void postInitialise(Context rootContext) {
        graphics.registerRenderingSubsystem(context);

        if (!GLFW.glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_COCOA_GRAPHICS_SWITCHING, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, config.getPixelFormat());
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
        if (config.getDebug().isEnabled()) {
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
        }
        GLFW.glfwSetErrorCallback(new GLFWErrorCallback());
        logger.info("Initializing display (if last line in log then likely the game crashed from an issue with your " +
                "video card)");

        long window = GLFW.glfwCreateWindow(
                config.getWindowWidth(), config.getWindowHeight(), "Terasology Alpha", 0, 0);
        primaryWindow = window;

        EngineKernel.EngineKernelBuild builder = new EngineKernel.EngineKernelBuild();
        switch (GLFW.glfwGetPlatform()) {
            case GLFW.GLFW_PLATFORM_X11:
                builder.configureX11Window(GLFWNativeX11.glfwGetX11Window(window), GLFWNativeX11.glfwGetX11Display());
                break;
            case GLFW.GLFW_PLATFORM_WIN32:
                builder.configureWin32Window(GLFWNativeWin32.glfwGetWin32Window(window), GLFWNativeWin32.nglfwGetWin32Adapter(window));
                break;
            default:
                throw new RuntimeException("missing platform: " + GLFW.glfwGetPlatform());
        }
        this.kernel = new EngineKernel(builder);
        context.put(EngineKernel.class, this.kernel);
        this.kernel.resizeSurface(lwjglDisplay.getWidth(), lwjglDisplay.getHeight());

        if (window == 0) {
            throw new RuntimeException("Failed to create window");
        }

        if (OS.get() != OS.MACOSX) {
            try {
                String root = "org/terasology/engine/icons/";
                ClassLoader classLoader = getClass().getClassLoader();

                BufferedImage icon16 = ImageIO.read(classLoader.getResourceAsStream(root + "gooey_sweet_16.png"));
                BufferedImage icon32 = ImageIO.read(classLoader.getResourceAsStream(root + "gooey_sweet_32.png"));
                BufferedImage icon64 = ImageIO.read(classLoader.getResourceAsStream(root + "gooey_sweet_64.png"));
                BufferedImage icon128 = ImageIO.read(classLoader.getResourceAsStream(root + "gooey_sweet_128.png"));
                GLFWImage.Buffer buffer = GLFWImage.create(4);
                buffer.put(0, LwjglGraphicsUtil.convertToGLFWFormat(icon16));
                buffer.put(1, LwjglGraphicsUtil.convertToGLFWFormat(icon32));
                buffer.put(2, LwjglGraphicsUtil.convertToGLFWFormat(icon64));
                buffer.put(3, LwjglGraphicsUtil.convertToGLFWFormat(icon128));
                // Not supported on Mac: Code: 65548, Description: Cocoa: Regular windows do not have icons on macOS
                GLFW.glfwSetWindowIcon(window, buffer);

            } catch (IOException | IllegalArgumentException e) {
                logger.warn("Could not set icon", e);
            }
        }

        lwjglDisplay.setDisplayModeSetting(config.getDisplayModeSetting(), false);
        GLFW.glfwShowWindow(window);
        GLFW.glfwSetFramebufferSizeCallback(LwjglGraphics.primaryWindow, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                lwjglDisplay.updateViewport(width, height);
                kernel.resizeSurface(width, height);
            }
        });

        context.put(ShaderManager.class, new ShaderManagerLwjgl());
        context.put(CanvasRenderer.class, new WgpuCanvasRenderer(context));
    }

    @Override
    public void postUpdate(GameState currentState, float delta) {
        graphics.processActions();

        boolean gameWindowIsMinimized = GLFW.glfwGetWindowAttrib(LwjglGraphics.primaryWindow, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
        if (!gameWindowIsMinimized) {
            this.kernel.cmdPrepare();
            currentState.render();
            this.kernel.cmdDispatch();
        }

        lwjglDisplay.update();
        int frameLimit = context.get(Config.class).getRendering().getFrameLimit();
        if (frameLimit > 0) {
            Lwjgl2Sync.sync(frameLimit);
        }
        if (lwjglDisplay.isCloseRequested()) {
            engine.shutdown();
        }
    }

    @Override
    public void preShutdown() {
        if (primaryWindow != MemoryUtil.NULL) {
            boolean isVisible = GLFW.glfwGetWindowAttrib(primaryWindow, GLFW.GLFW_VISIBLE) == GLFW.GLFW_TRUE;
            boolean isFullScreen = lwjglDisplay.isFullscreen();
            if (!isFullScreen && isVisible) {
                int[] xBuffer = new int[1];
                int[] yBuffer = new int[1];
                GLFW.glfwGetWindowPos(primaryWindow, xBuffer, yBuffer);
                int[] widthBuffer = new int[1];
                int[] heightBuffer = new int[1];
                GLFW.glfwGetWindowSize(primaryWindow, widthBuffer, heightBuffer);

                if (widthBuffer[0] > 0 && heightBuffer[0] > 0 && xBuffer[0] > 0 && yBuffer[0] > 0) {
                    config.setWindowWidth(widthBuffer[0]);
                    config.setWindowHeight(heightBuffer[0]);
                    config.setWindowPosX(xBuffer[0]);
                    config.setWindowPosY(yBuffer[0]);
                }
            }
        }
    }

    @Override
    public void shutdown() {
        GLFW.glfwTerminate();
    }
}
