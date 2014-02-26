/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.subsystem.lwjgl;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLContext;
import org.newdawn.slick.opengl.ImageIOImageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
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
import org.terasology.rendering.assets.texture.ColorTextureAssetResolver;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.assets.texture.subtexture.Subtexture;
import org.terasology.rendering.assets.texture.subtexture.SubtextureData;
import org.terasology.rendering.assets.texture.subtexture.SubtextureFromAtlasResolver;
import org.terasology.rendering.iconmesh.IconMeshResolver;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.internal.LwjglCanvasRenderer;
import org.terasology.rendering.nui.internal.NUIManagerInternal;
import org.terasology.rendering.opengl.GLSLMaterial;
import org.terasology.rendering.opengl.GLSLShader;
import org.terasology.rendering.opengl.OpenGLMesh;
import org.terasology.rendering.opengl.OpenGLSkeletalMesh;
import org.terasology.rendering.opengl.OpenGLTexture;

import javax.imageio.ImageIO;
import javax.swing.*;
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
    public void preInitialise() {
        super.preInitialise();
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
        Display.sync(60);
        currentState.render();

        if (Display.wasResized()) {
            glViewport(0, 0, Display.getWidth(), Display.getHeight());
        }
    }

    @Override
    public void shutdown(Config config) {
        if (!Display.isFullscreen() && Display.isVisible()) {
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
                        new ImageIOImageData().imageToByteBuffer(icon16, false, false, null),
                        new ImageIOImageData().imageToByteBuffer(icon32, false, false, null),
                        new ImageIOImageData().imageToByteBuffer(icon64, false, false, null),
                        new ImageIOImageData().imageToByteBuffer(icon128, false, false, null)
                });
            } catch (IOException | IllegalArgumentException e) {
                logger.warn("Could not set icon", e);
            }
            Display.create(rc.getPixelFormat());
            Display.setVSyncEnabled(rc.isVSync());
        } catch (LWJGLException e) {
            logger.error("Can not initialize graphics device.", e);
            System.exit(1);
        }
    }

    private void initOpenGL() {
        checkOpenGL();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        initOpenGLParams();
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        assetManager.setAssetFactory(AssetType.FONT, new AssetFactory<FontData, Font>() {
            @Override
            public Font buildAsset(AssetUri uri, FontData data) {
                return new FontImpl(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.TEXTURE, new AssetFactory<TextureData, Texture>() {
            @Override
            public Texture buildAsset(AssetUri uri, TextureData data) {
                return new OpenGLTexture(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.SHADER, new AssetFactory<ShaderData, Shader>() {
            @Override
            public Shader buildAsset(AssetUri uri, ShaderData data) {
                return new GLSLShader(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.MATERIAL, new AssetFactory<MaterialData, Material>() {
            @Override
            public Material buildAsset(AssetUri uri, MaterialData data) {
                return new GLSLMaterial(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.MESH, new AssetFactory<MeshData, Mesh>() {
            @Override
            public Mesh buildAsset(AssetUri uri, MeshData data) {
                return new OpenGLMesh(uri, data, bufferPool);
            }
        });
        assetManager.setAssetFactory(AssetType.SKELETON_MESH, new AssetFactory<SkeletalMeshData, SkeletalMesh>() {
            @Override
            public SkeletalMesh buildAsset(AssetUri uri, SkeletalMeshData data) {
                return new OpenGLSkeletalMesh(uri, data, bufferPool);
            }
        });
        assetManager.setAssetFactory(AssetType.ANIMATION, new AssetFactory<MeshAnimationData, MeshAnimation>() {
            @Override
            public MeshAnimation buildAsset(AssetUri uri, MeshAnimationData data) {
                return new MeshAnimationImpl(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.ATLAS, new AssetFactory<AtlasData, Atlas>() {
            @Override
            public Atlas buildAsset(AssetUri uri, AtlasData data) {
                return new Atlas(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.SUBTEXTURE, new AssetFactory<SubtextureData, Subtexture>() {
            @Override
            public Subtexture buildAsset(AssetUri uri, SubtextureData data) {
                return new Subtexture(uri, data);
            }
        });
        assetManager.addResolver(AssetType.SUBTEXTURE, new SubtextureFromAtlasResolver());
        assetManager.addResolver(AssetType.TEXTURE, new ColorTextureAssetResolver());
        assetManager.addResolver(AssetType.MESH, new IconMeshResolver());
        CoreRegistry.putPermanently(ShaderManager.class, new ShaderManagerLwjgl());
        CoreRegistry.get(ShaderManager.class).initShaders();
    }

    private void checkOpenGL() {
        boolean canRunGame = GLContext.getCapabilities().OpenGL11
                & GLContext.getCapabilities().OpenGL12
                & GLContext.getCapabilities().OpenGL14
                & GLContext.getCapabilities().OpenGL15
                & GLContext.getCapabilities().GL_ARB_framebuffer_object
                & GLContext.getCapabilities().GL_ARB_texture_float
                & GLContext.getCapabilities().GL_ARB_half_float_pixel
                & GLContext.getCapabilities().GL_ARB_shader_objects;

        if (!canRunGame) {
            String message = "Your GPU driver is not supporting the mandatory versions or extensions of OpenGL. Considered updating your GPU drivers? Exiting...";
            logger.error(message);
            JOptionPane.showMessageDialog(null, message, "Mandatory OpenGL version(s) or extension(s) not supported", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

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
