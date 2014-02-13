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
package org.terasology.engine.subsystem.headless;

import org.terasology.asset.AssetFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.engine.subsystem.RenderingSubsystemFactory;
import org.terasology.engine.subsystem.headless.assets.HeadlessFont;
import org.terasology.engine.subsystem.headless.assets.HeadlessMaterial;
import org.terasology.engine.subsystem.headless.assets.HeadlessMesh;
import org.terasology.engine.subsystem.headless.assets.HeadlessShader;
import org.terasology.engine.subsystem.headless.assets.HeadlessSkeletalMesh;
import org.terasology.engine.subsystem.headless.assets.HeadlessTexture;
import org.terasology.engine.subsystem.headless.device.HeadlessDisplayDevice;
import org.terasology.engine.subsystem.headless.renderer.GUIManagerHeadless;
import org.terasology.engine.subsystem.headless.renderer.HeadlessCanvasRenderer;
import org.terasology.engine.subsystem.headless.renderer.HeadlessRenderingSubsystemFactory;
import org.terasology.engine.subsystem.headless.renderer.ShaderManagerHeadless;
import org.terasology.logic.manager.GUIManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.VertexBufferObjectManager;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.animation.MeshAnimationData;
import org.terasology.rendering.assets.animation.MeshAnimationImpl;
import org.terasology.rendering.assets.atlas.Atlas;
import org.terasology.rendering.assets.atlas.AtlasData;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.font.FontData;
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
import org.terasology.rendering.nui.internal.NUIManagerInternal;

public class HeadlessGraphics implements EngineSubsystem {

    @Override
    public void preInitialise() {
    }

    @Override
    public void postInitialise(Config config) {
        CoreRegistry.putPermanently(RenderingSubsystemFactory.class, new HeadlessRenderingSubsystemFactory());

        HeadlessDisplayDevice headlessDisplay = new HeadlessDisplayDevice();
        CoreRegistry.putPermanently(DisplayDevice.class, headlessDisplay);
        initHeadless(headlessDisplay);

        CoreRegistry.putPermanently(GUIManager.class, new GUIManagerHeadless());
        CoreRegistry.putPermanently(NUIManager.class, new NUIManagerInternal(CoreRegistry.get(AssetManager.class), new HeadlessCanvasRenderer()));

        //        CoreRegistry.putPermanently(DefaultRenderingProcess.class, new HeadlessRenderingProcess());
    }

    @Override
    public void preUpdate(GameState currentState, float delta) {
    }

    @Override
    public void postUpdate(GameState currentState, float delta) {
    }

    @Override
    public void shutdown(Config config) {
    }

    @Override
    public void dispose() {
    }

    private void initHeadless(HeadlessDisplayDevice headlessDisplay) {
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        assetManager.setAssetFactory(AssetType.TEXTURE, new AssetFactory<TextureData, Texture>() {
            @Override
            public Texture buildAsset(AssetUri uri, TextureData data) {
                return new HeadlessTexture(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.FONT, new AssetFactory<FontData, Font>() {
            @Override
            public Font buildAsset(AssetUri uri, FontData data) {
                return new HeadlessFont(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.SHADER, new AssetFactory<ShaderData, Shader>() {
            @Override
            public Shader buildAsset(AssetUri uri, ShaderData data) {
                return new HeadlessShader(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.MATERIAL, new AssetFactory<MaterialData, Material>() {
            @Override
            public Material buildAsset(AssetUri uri, MaterialData data) {
                return new HeadlessMaterial(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.MESH, new AssetFactory<MeshData, Mesh>() {
            @Override
            public Mesh buildAsset(AssetUri uri, MeshData data) {
                return new HeadlessMesh(uri, data);
            }
        });
        assetManager.setAssetFactory(AssetType.SKELETON_MESH, new AssetFactory<SkeletalMeshData, SkeletalMesh>() {
            @Override
            public SkeletalMesh buildAsset(AssetUri uri, SkeletalMeshData data) {
                return new HeadlessSkeletalMesh(uri, data);
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
        CoreRegistry.putPermanently(ShaderManager.class, new ShaderManagerHeadless());
        CoreRegistry.get(ShaderManager.class).initShaders();
        VertexBufferObjectManager.getInstance();

    }

    @Override
    public void registerSystems(ComponentSystemManager componentSystemManager) {
    }

}
