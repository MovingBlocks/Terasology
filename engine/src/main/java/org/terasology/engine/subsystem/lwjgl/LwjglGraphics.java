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
import org.terasology.assets.AssetFactory;
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
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.ShaderManagerLwjgl;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.animation.MeshAnimationData;
import org.terasology.rendering.assets.animation.MeshAnimationImpl;
import org.terasology.rendering.assets.atlas.Atlas;
import org.terasology.rendering.assets.atlas.AtlasData;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.font.FontData;
import org.terasology.rendering.assets.font.FontImpl;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.material.MaterialData;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.mesh.MeshData;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.shader.ShaderData;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshData;
import org.terasology.rendering.assets.texture.PNGTextureFormat;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.assets.texture.TextureUtil;
import org.terasology.rendering.assets.texture.subtexture.Subtexture;
import org.terasology.rendering.assets.texture.subtexture.SubtextureData;
import org.terasology.rendering.nui.internal.CanvasRenderer;
import org.terasology.rendering.nui.internal.LwjglCanvasRenderer;
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

    private GLBufferPool bufferPool = new GLBufferPool(false);

    private BlockingDeque<Runnable> displayThreadActions = Queues.newLinkedBlockingDeque();

    private Context context;
    private RenderingConfig config;

    private GameEngine engine;
    private LwjglDisplayDevice lwjglDisplay;

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

        // cast lambdas explicitly to avoid inconsistent compiler behavior wrt. type inference
        assetTypeManager.registerCoreAssetType(Font.class,
                (AssetFactory<Font, FontData>) FontImpl::new, "fonts");
        assetTypeManager.registerCoreAssetType(Texture.class, (AssetFactory<Texture, TextureData>)
                (urn, assetType, data) -> (new OpenGLTexture(urn, assetType, data, this)), "textures", "fonts");
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
                (AssetFactory<Shader, ShaderData>) GLSLShader::new, "shaders");
        assetTypeManager.registerCoreAssetType(Material.class,
                (AssetFactory<Material, MaterialData>) GLSLMaterial::new, "materials");
        assetTypeManager.registerCoreAssetType(Mesh.class, (AssetFactory<Mesh, MeshData>)
                (urn, assetType, data) -> new OpenGLMesh(urn, assetType, bufferPool, data), "mesh");
        assetTypeManager.registerCoreAssetType(SkeletalMesh.class, (AssetFactory<SkeletalMesh, SkeletalMeshData>)
                (urn, assetType, data) -> new OpenGLSkeletalMesh(urn, assetType, data, bufferPool), "skeletalMesh");
        assetTypeManager.registerCoreAssetType(MeshAnimation.class,
                (AssetFactory<MeshAnimation, MeshAnimationData>) MeshAnimationImpl::new, "animations");
        assetTypeManager.registerCoreAssetType(Atlas.class,
                (AssetFactory<Atlas, AtlasData>) Atlas::new, "atlas");
        assetTypeManager.registerCoreAssetType(Subtexture.class,
                (AssetFactory<Subtexture, SubtextureData>) Subtexture::new);
    }

    @Override
    public void postInitialise(Context rootContext) {
        context.put(RenderingSubsystemFactory.class, new LwjglRenderingSubsystemFactory(bufferPool));

        initDisplay();
        initOpenGL(context);

        context.put(CanvasRenderer.class, new LwjglCanvasRenderer(context));
    }

    @Override
    public void postUpdate(GameState currentState, float delta) {
        Display.update();

        if (!displayThreadActions.isEmpty()) {
            List<Runnable> actions = Lists.newArrayListWithExpectedSize(displayThreadActions.size());
            displayThreadActions.drainTo(actions);
            actions.forEach(Runnable::run);
        }

        int frameLimit = context.get(Config.class).getRendering().getFrameLimit();
        if (frameLimit > 0) {
            Display.sync(frameLimit);
        }
        currentState.render();

        if (Display.wasResized()) {
            glViewport(0, 0, Display.getWidth(), Display.getHeight());
        }

        if (lwjglDisplay.isCloseRequested()) {
            engine.shutdown();
        }


    }

    @Override
    public void preShutdown() {
        if (Display.isCreated() && !Display.isFullscreen() && Display.isVisible()) {
            config.setWindowPosX(Display.getX());
            config.setWindowPosY(Display.getY());
        }
    }

    @Override
    public void shutdown() {
        Display.destroy();
    }

    private void initDisplay() {
        logger.info("Initializing display");
        try {
            lwjglDisplay.setFullscreen(config.isFullscreen(), false);

            Display.setLocation(config.getWindowPosX(), config.getWindowPosY());
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

            if (config.getDebug().isEnabled()) {
                try {
                    ContextAttribs ctxAttribs = new ContextAttribs().withDebug(true);
                    Display.create(config.getPixelFormat(), ctxAttribs);

                    GL43.glDebugMessageCallback(new KHRDebugCallback(new DebugCallback()));
                } catch (LWJGLException e) {
                    logger.warn("Unable to create an OpenGL debug context. Maybe your graphics card does not support it.", e);
                    Display.create(config.getPixelFormat()); // Create a normal context instead
                }

            } else {
                Display.create(config.getPixelFormat());
            }

            Display.setVSyncEnabled(config.isVSync());
        } catch (LWJGLException e) {
            throw new RuntimeException("Can not initialize graphics device.", e);
        }
    }

    private void initOpenGL(Context currentContext) {
        logger.info("Initializing OpenGL");
        checkOpenGL();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        initOpenGLParams();
        currentContext.put(ShaderManager.class, new ShaderManagerLwjgl());
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

    public void reloadTexture3D(int id, ByteBuffer alignedBuffer, Texture.WrapMode wrapMode, Texture.FilterMode filterMode, int size) {
        asynchToDisplayThread(() -> {
            glBindTexture(GL12.GL_TEXTURE_3D, id);

            glTexParameterf(GL12.GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, LwjglGraphicsUtil.getGLMode(wrapMode));
            glTexParameterf(GL12.GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, LwjglGraphicsUtil.getGLMode(wrapMode));
            glTexParameterf(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_WRAP_R, LwjglGraphicsUtil.getGLMode(wrapMode));

            GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MIN_FILTER, LwjglGraphicsUtil.getGlMinFilter(filterMode));
            GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MAG_FILTER, LwjglGraphicsUtil.getGlMagFilter(filterMode));

            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
            GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_MAX_LEVEL, 0);

            GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, GL11.GL_RGBA, size, size, size, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, alignedBuffer);
        });
    }

    public void createTexture2D(ByteBuffer[] buffers, Texture.WrapMode wrapMode, Texture.FilterMode filterMode, int width, int height, Consumer<Integer> idConsumer) {
        asynchToDisplayThread(() -> {
            int id = glGenTextures();
            reloadTexture2D(id, buffers, wrapMode, filterMode, width, height);
            idConsumer.accept(id);
        });
    }

    public void reloadTexture2D(int id, ByteBuffer[] buffers, Texture.WrapMode wrapMode, Texture.FilterMode filterMode, int width, int height) {
        asynchToDisplayThread(() -> {
            glBindTexture(GL11.GL_TEXTURE_2D, id);

            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, LwjglGraphicsUtil.getGLMode(wrapMode));
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, LwjglGraphicsUtil.getGLMode(wrapMode));
            GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, LwjglGraphicsUtil.getGlMinFilter(filterMode));
            GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, LwjglGraphicsUtil.getGlMagFilter(filterMode));
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, buffers.length - 1);

            if (buffers.length > 0) {
                for (int i = 0; i < buffers.length; i++) {
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, i, GL11.GL_RGBA, width >> i, height >> i, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffers[i]);
                }
            } else {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            }
        });
    }

    public void disposeTexture(int id) {
        asynchToDisplayThread(() -> glDeleteTextures(id));
    }
}
