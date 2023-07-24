// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameThread;
import org.terasology.engine.core.subsystem.DisplayDeviceInfo;
import org.terasology.engine.core.subsystem.RenderingSubsystemFactory;
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
import org.terasology.engine.rendering.opengl.GLSLMaterial;
import org.terasology.engine.rendering.opengl.GLSLShader;
import org.terasology.engine.rendering.opengl.OpenGLMesh;
import org.terasology.engine.rendering.opengl.OpenGLSkeletalMesh;
import org.terasology.engine.rendering.opengl.OpenGLTexture;
import org.terasology.engine.rendering.opengl.WgpuTexture;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.module.ModuleAssetScanner;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexParameterf;

public class LwjglGraphicsManager implements LwjglGraphicsProcessing {

    private final BlockingDeque<Runnable> displayThreadActions = Queues.newLinkedBlockingDeque();

    private DisplayDeviceInfo displayDeviceInfo = new DisplayDeviceInfo("unknown");
    private ThreadMode threadMode = ThreadMode.GAME_THREAD;

    public void setThreadMode(ThreadMode threadMode) {
        this.threadMode = threadMode;
    }

    public void registerRenderingSubsystem(Context context) {
        context.put(RenderingSubsystemFactory.class, new LwjglRenderingSubsystemFactory());
    }

    public void processActions() {
        if (!displayThreadActions.isEmpty()) {
            List<Runnable> actions = Lists.newArrayListWithExpectedSize(displayThreadActions.size());
            displayThreadActions.drainTo(actions);
            actions.forEach(Runnable::run);
        }
    }

    public void asynchToDisplayThread(Runnable action) {
        if (threadMode == ThreadMode.GAME_THREAD && GameThread.isCurrentThread()) {
            action.run();
        } else {
            displayThreadActions.add(action);
        }
    }

    public void createTexture3D(ByteBuffer alignedBuffer, Texture.WrapMode wrapMode, Texture.FilterMode filterMode,
                                int size, Consumer<Integer> idConsumer) {
//        asynchToDisplayThread(() -> {
//            int id = glGenTextures();
//            reloadTexture3D(id, alignedBuffer, wrapMode, filterMode, size);
//            idConsumer.accept(id);
//        });
    }

    public void reloadTexture3D(int id, ByteBuffer alignedBuffer, Texture.WrapMode wrapMode,
                                Texture.FilterMode filterMode, int size) {
//        asynchToDisplayThread(() -> {
//            glBindTexture(GL12.GL_TEXTURE_3D, id);
//
//            glTexParameterf(GL12.GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, LwjglGraphicsUtil.getGLMode(wrapMode));
//            glTexParameterf(GL12.GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, LwjglGraphicsUtil.getGLMode(wrapMode));
//            glTexParameterf(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_WRAP_R, LwjglGraphicsUtil.getGLMode(wrapMode));
//
//            GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MIN_FILTER,
//                    LwjglGraphicsUtil.getGlMinFilter(filterMode));
//            GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MAG_FILTER,
//                    LwjglGraphicsUtil.getGlMagFilter(filterMode));
//
//            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
//            GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
//
//            GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, GL11.GL_RGBA, size, size, size, 0, GL11.GL_RGBA,
//                    GL11.GL_UNSIGNED_BYTE, alignedBuffer);
//        });
    }

    public void createTexture2D(ByteBuffer[] buffers, Texture.WrapMode wrapMode, Texture.FilterMode filterMode,
                                int width, int height, Consumer<Integer> idConsumer) {
//        asynchToDisplayThread(() -> {
//            int id = glGenTextures();
//            reloadTexture2D(id, buffers, wrapMode, filterMode, width, height);
//            idConsumer.accept(id);
//        });
    }

    public void reloadTexture2D(int id, ByteBuffer[] buffers, Texture.WrapMode wrapMode,
                                Texture.FilterMode filterMode, int width, int height) {
//        asynchToDisplayThread(() -> {
//            glBindTexture(GL11.GL_TEXTURE_2D, id);
//
//            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, LwjglGraphicsUtil.getGLMode(wrapMode));
//            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, LwjglGraphicsUtil.getGLMode(wrapMode));
//            GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
//                    LwjglGraphicsUtil.getGlMinFilter(filterMode));
//            GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
//                    LwjglGraphicsUtil.getGlMagFilter(filterMode));
//            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
//            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, buffers.length - 1);
//
//            if (buffers.length > 0) {
//                for (int i = 0; i < buffers.length; i++) {
//                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, i, GL11.GL_RGBA, width >> i, height >> i, 0, GL11.GL_RGBA,
//                            GL11.GL_UNSIGNED_BYTE, buffers[i]);
//                }
//            } else {
//                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA,
//                        GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
//            }
//        });
    }

    public void disposeTexture(int id) {
        asynchToDisplayThread(() -> glDeleteTextures(id));
    }

    public DisplayDeviceInfo getDisplayDeviceInfo() {
        return displayDeviceInfo;
    }

    public enum ThreadMode {
        GAME_THREAD,
        DISPLAY_THREAD
    }
}
