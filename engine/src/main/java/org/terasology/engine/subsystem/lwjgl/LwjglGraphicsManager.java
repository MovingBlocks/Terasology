/*
 * Copyright 2017 MovingBlocks
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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.terasology.assets.AssetFactory;
import org.terasology.assets.module.ModuleAssetDataProducer;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.context.Context;
import org.terasology.engine.GameThread;
import org.terasology.engine.subsystem.DisplayDeviceInfo;
import org.terasology.engine.subsystem.RenderingSubsystemFactory;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.animation.MeshAnimationBundle;
import org.terasology.rendering.assets.animation.MeshAnimationBundleData;
import org.terasology.rendering.assets.animation.MeshAnimationImpl;
import org.terasology.rendering.assets.atlas.Atlas;
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
import org.terasology.rendering.assets.texture.subtexture.Subtexture;
import org.terasology.rendering.assets.texture.subtexture.SubtextureData;
import org.terasology.rendering.opengl.GLSLMaterial;
import org.terasology.rendering.opengl.GLSLShader;
import org.terasology.rendering.opengl.OpenGLMesh;
import org.terasology.rendering.opengl.OpenGLSkeletalMesh;
import org.terasology.rendering.opengl.OpenGLTexture;

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

    private final GLBufferPool bufferPool = new GLBufferPool(false);

    private final BlockingDeque<Runnable> displayThreadActions = Queues.newLinkedBlockingDeque();

    private DisplayDeviceInfo displayDeviceInfo = new DisplayDeviceInfo("unknown");
    private ThreadMode threadMode = ThreadMode.GAME_THREAD;

    public void setThreadMode(ThreadMode threadMode) {
        this.threadMode = threadMode;
    }

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
                (AssetFactory<Shader, ShaderData>) (urn, assetType, data) ->
                        new GLSLShader(urn, assetType, data, this),
                "shaders");
        assetTypeManager.registerCoreAssetType(Material.class,
                (AssetFactory<Material, MaterialData>) (urn, assetType, data) ->
                        new GLSLMaterial(urn, assetType, data, this),
                "materials");
        assetTypeManager.registerCoreAssetType(Mesh.class, (AssetFactory<Mesh, MeshData>)
                        (urn, assetType, data) ->
                                new OpenGLMesh(urn, assetType, bufferPool, data, this),
                "mesh");
        assetTypeManager.registerCoreAssetType(SkeletalMesh.class, (AssetFactory<SkeletalMesh, SkeletalMeshData>)
                        (urn, assetType, data) ->
                                new OpenGLSkeletalMesh(urn, assetType, bufferPool, data, this),
                "skeletalMesh");
        assetTypeManager.registerCoreAssetType(MeshAnimation.class, MeshAnimationImpl::new,
                "animations", "skeletalMesh");
        assetTypeManager.registerCoreAssetType(Atlas.class, Atlas::new, "atlas");
        assetTypeManager.registerCoreAssetType(MeshAnimationBundle.class,
                (AssetFactory<MeshAnimationBundle, MeshAnimationBundleData>) MeshAnimationBundle::new,
                "skeletalMesh", "animations");
        assetTypeManager.registerCoreAssetType(Subtexture.class,
                (AssetFactory<Subtexture, SubtextureData>) Subtexture::new);
    }

    public void registerRenderingSubsystem(Context context) {
        context.put(RenderingSubsystemFactory.class, new LwjglRenderingSubsystemFactory(bufferPool));
    }

    public void processActions() {
        LwjglGraphicsUtil.updateDisplayDeviceInfo(displayDeviceInfo);

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

    public DisplayDeviceInfo getDisplayDeviceInfo() {
        return displayDeviceInfo;
    }

    public enum ThreadMode {
        GAME_THREAD,
        DISPLAY_THREAD
    }
}
