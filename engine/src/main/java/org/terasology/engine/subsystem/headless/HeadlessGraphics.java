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

import org.terasology.context.Context;
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
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.nui.canvas.CanvasRenderer;
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
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.assets.texture.subtexture.Subtexture;

public class HeadlessGraphics implements EngineSubsystem {

    @Override
    public String getName() {
        return "Graphics";
    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
        assetTypeManager.createAssetType(Font.class,  FontImpl::new, "fonts");
        AssetType<Texture, TextureData> texture = assetTypeManager.createAssetType(Texture.class, HeadlessTexture::create, "textures", "fonts");
        assetTypeManager.getAssetFileDataProducer(texture)
                .addAssetFormat(new PNGTextureFormat(Texture.FilterMode.NEAREST, path -> path.getPath().get(1).equals("textures")));
        assetTypeManager.getAssetFileDataProducer(texture)
                .addAssetFormat(new PNGTextureFormat(Texture.FilterMode.LINEAR, path -> path.getPath().get(1).equals("fonts")));
        assetTypeManager.createAssetType(Shader.class, HeadlessShader::new, "shaders");
        assetTypeManager.createAssetType(Material.class, HeadlessMaterial::new, "materials");
        assetTypeManager.createAssetType(Mesh.class, HeadlessMesh::new, "mesh");
        assetTypeManager.createAssetType(SkeletalMesh.class, HeadlessSkeletalMesh::new, "skeletalMesh");
        assetTypeManager.createAssetType(MeshAnimation.class, MeshAnimationImpl::new, "animations");
        assetTypeManager.createAssetType(Atlas.class, Atlas::new, "atlas");
        assetTypeManager.createAssetType(Subtexture.class, Subtexture::new);
    }

    @Override
    public void postInitialise(Context context) {
        context.put(RenderingSubsystemFactory.class, new HeadlessRenderingSubsystemFactory());

        HeadlessDisplayDevice headlessDisplay = new HeadlessDisplayDevice();
        context.put(DisplayDevice.class, headlessDisplay);
        initHeadless(context);

        context.put(CanvasRenderer.class, new HeadlessCanvasRenderer());
    }

    private void initHeadless(Context context) {
        context.put(ShaderManager.class, new ShaderManagerHeadless());
    }


}
