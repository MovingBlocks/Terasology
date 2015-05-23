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

import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.engine.subsystem.RenderingSubsystemFactory;
import org.terasology.engine.subsystem.headless.assets.HeadlessMaterial;
import org.terasology.engine.subsystem.headless.assets.HeadlessMesh;
import org.terasology.engine.subsystem.headless.assets.HeadlessShader;
import org.terasology.engine.subsystem.headless.assets.HeadlessSkeletalMesh;
import org.terasology.engine.subsystem.headless.assets.HeadlessTexture;
import org.terasology.engine.subsystem.headless.device.HeadlessDisplayDevice;
import org.terasology.engine.subsystem.headless.renderer.HeadlessCanvasRenderer;
import org.terasology.engine.subsystem.headless.renderer.HeadlessRenderingSubsystemFactory;
import org.terasology.engine.subsystem.headless.renderer.ShaderManagerHeadless;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.ShaderManager;
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
import org.terasology.rendering.assets.texture.subtexture.Subtexture;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.internal.NUIManagerInternal;

import java.nio.file.Path;
import java.nio.file.PathMatcher;

public class HeadlessGraphics implements EngineSubsystem {

    @Override
    public void preInitialise() {
    }

    @Override
    public void initialise(Config config) {

    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
        assetTypeManager.registerCoreAssetType(Font.class, FontImpl::new, "fonts");
        assetTypeManager.registerCoreAssetType(Texture.class, HeadlessTexture::new, "textures", "fonts");
        assetTypeManager.registerCoreFormat(Texture.class, new PNGTextureFormat(Texture.FilterMode.NEAREST, path -> path.getName(1).toString().equals("textures")));
        assetTypeManager.registerCoreFormat(Texture.class, new PNGTextureFormat(Texture.FilterMode.LINEAR, path -> path.getName(1).toString().equals("fonts")));


        assetTypeManager.registerCoreAssetType(Shader.class, HeadlessShader::new, "shaders");
        assetTypeManager.registerCoreAssetType(Material.class, HeadlessMaterial::new, "materials");
        assetTypeManager.registerCoreAssetType(Mesh.class, HeadlessMesh::new, "mesh");
        assetTypeManager.registerCoreAssetType(SkeletalMesh.class, HeadlessSkeletalMesh::new, "skeletalMesh");
        assetTypeManager.registerCoreAssetType(MeshAnimation.class, MeshAnimationImpl::new, "animations");

        assetTypeManager.registerCoreAssetType(Atlas.class, Atlas::new, "atlas");
        assetTypeManager.registerCoreAssetType(Subtexture.class, Subtexture::new);


    }

    @Override
    public void postInitialise(Config config) {
        CoreRegistry.putPermanently(RenderingSubsystemFactory.class, new HeadlessRenderingSubsystemFactory());

        HeadlessDisplayDevice headlessDisplay = new HeadlessDisplayDevice();
        CoreRegistry.putPermanently(DisplayDevice.class, headlessDisplay);
        initHeadless();

        CoreRegistry.putPermanently(NUIManager.class, new NUIManagerInternal(new HeadlessCanvasRenderer()));

        //        CoreRegistry.putPermanently(LwjglRenderingProcess.class, new HeadlessRenderingProcess());
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

    private void initHeadless() {
        CoreRegistry.putPermanently(ShaderManager.class, new ShaderManagerHeadless());
    }

    @Override
    public void registerSystems(ComponentSystemManager componentSystemManager) {
    }

}
