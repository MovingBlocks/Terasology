// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.editor.subsystem;

import com.google.common.base.Preconditions;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.editor.input.AwtKeyboardDevice;
import org.terasology.editor.input.AwtMouseDevice;
import org.terasology.engine.GameEngine;
import org.terasology.engine.GameThread;
import org.terasology.engine.TerasologyEngine;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.lwjgl.BaseLwjglSubsystem;
import org.terasology.engine.subsystem.lwjgl.DebugCallback;
import org.terasology.engine.subsystem.lwjgl.GLFWErrorCallback;
import org.terasology.engine.subsystem.lwjgl.LwjglGraphicsManager;
import org.terasology.engine.subsystem.lwjgl.LwjglGraphicsUtil;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.input.InputSystem;
import org.terasology.nui.canvas.CanvasRenderer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.ShaderManagerLwjgl;
import org.terasology.rendering.nui.internal.LwjglCanvasRenderer;
import org.terasology.rendering.world.WorldRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glLoadIdentity;

public class LwjglPortlet extends BaseLwjglSubsystem {

    private static final Logger logger = LoggerFactory.getLogger(LwjglPortlet.class);

    private Context context;
    private RenderingConfig config;

    private GameEngine engine;
    private AWTGLCanvas canvas;
    private LwjglPortletDisplayDevice display;
    private AwtMouseDevice mouseDevice;

    private final LwjglGraphicsManager graphics = new LwjglGraphicsManager();

    @Override
    public String getName() {
        return "Portlet";
    }

    @Override
    public void initialise(GameEngine gameEngine, Context rootContext) {
        logger.info("Starting initialization of LWJGL");
        this.engine = gameEngine;
        this.context = rootContext;
        this.config = context.get(Config.class).getRendering();

        graphics.setThreadMode(LwjglGraphicsManager.ThreadMode.DISPLAY_THREAD);
        display = new LwjglPortletDisplayDevice(canvas, graphics);
        context.put(DisplayDevice.class, display);
        logger.info("Initial initialization complete");
    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
        graphics.registerCoreAssetTypes(assetTypeManager);
    }

    @Override
    public void postInitialise(Context rootContext) {
        graphics.registerRenderingSubsystem(context);

        initBuffer();

        context.put(ShaderManager.class, new ShaderManagerLwjgl());
        context.put(CanvasRenderer.class, new LwjglCanvasRenderer(context));
    }

    @Override
    public void postUpdate(GameState currentState, float delta) {
        graphics.processActions();

        currentState.render();

        display.update();
        int frameLimit = context.get(Config.class).getRendering().getFrameLimit();
        if (frameLimit > 0) {
//            Lwjgl2Sync.sync(frameLimit);
        }
        if (display.isCloseRequested()) {
            engine.shutdown();
        }
    }

    public void setupThreads() {
        GameThread.reset();
        GameThread.setToCurrentThread();
        graphics.setThreadMode(LwjglGraphicsManager.ThreadMode.GAME_THREAD);

        EventSystem eventSystem = CoreRegistry.get(EventSystem.class);
        if (eventSystem != null) {
            eventSystem.setToCurrentThread();
        }
    }

    public void createCanvas() {
        GLData data = new GLData();
        data.samples = 4;
        canvas = new AWTGLCanvas() {
            @Override
            public void initGL() {
                initGLFW();
                initOpenGL();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                glLoadIdentity();
            }

            @Override
            public void paintGL() {
                if (((TerasologyEngine) engine).tick()) {
                    mouseDevice.resetDelta();
                }
            }
        };
    }

    public AWTGLCanvas getCanvas() {
        return this.canvas;
    }

    private void initGLFW() {
        if (!GLFW.glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_COCOA_GRAPHICS_SWITCHING, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, config.getPixelFormat());

        if (config.getDebug().isEnabled()) {
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
        }

        GLFW.glfwSetErrorCallback(new GLFWErrorCallback());
    }

    private void initBuffer() {
        logger.info("Initializing display (if last line in log then likely the game crashed from an issue with your " +
                "video card)");

        if (!config.isVSync()) {
            GLFW.glfwSwapInterval(0);
        }

        try {
            String root = "org/terasology/icons/";
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

        } catch (IOException | IllegalArgumentException e) {
            logger.warn("Could not set icon", e);
        }

        display.setDisplayModeSetting(config.getDisplayModeSetting());
    }

    private void initOpenGL() {
        logger.info("Initializing OpenGL");
        LwjglGraphicsUtil.checkOpenGL();
        LwjglGraphicsUtil.initOpenGLParams();
        if (config.getDebug().isEnabled()) {
            try {
                GL43.glDebugMessageCallback(new DebugCallback(), MemoryUtil.NULL);
            } catch (IllegalStateException e) {
                logger.warn("Unable to specify DebugCallback to receive debugging messages from the GL.");
            }
        }
    }

    public void initInputs() {
        final InputSystem inputSystem = context.get(InputSystem.class);
        Preconditions.checkNotNull(inputSystem);
        mouseDevice = ((AwtMouseDevice) inputSystem.getMouseDevice());
        mouseDevice.registerToAwtGlCanvas(canvas);
        ((AwtKeyboardDevice) inputSystem.getKeyboard()).registerToAwtGlCanvas(canvas);
    }

    @Override
    public void shutdown() {
        GLFW.glfwTerminate();
    }
}
