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
package org.terasology.engine.core.subsystem.headless;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.core.subsystem.RenderingSubsystemFactory;
import org.terasology.engine.core.subsystem.headless.assets.HeadlessMaterial;
import org.terasology.engine.core.subsystem.headless.assets.HeadlessMesh;
import org.terasology.engine.core.subsystem.headless.assets.HeadlessShader;
import org.terasology.engine.core.subsystem.headless.assets.HeadlessSkeletalMesh;
import org.terasology.engine.core.subsystem.headless.assets.HeadlessTexture;
import org.terasology.engine.core.subsystem.headless.device.HeadlessDisplayDevice;
import org.terasology.engine.core.subsystem.headless.renderer.HeadlessCanvasRenderer;
import org.terasology.engine.core.subsystem.headless.renderer.HeadlessRenderingSubsystemFactory;
import org.terasology.engine.core.subsystem.headless.renderer.ShaderManagerHeadless;
import org.terasology.engine.rendering.ShaderManager;
import org.terasology.engine.rendering.assets.animation.MeshAnimation;
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
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.nui.canvas.CanvasRenderer;

public class HeadlessGraphics implements EngineSubsystem {

    @Override
    public String getName() {
        return "Graphics";
    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
        assetTypeManager.createAssetType(Font.class, FontImpl::new, "fonts");
        AssetType<Texture, TextureData> texture = assetTypeManager.createAssetType(Texture.class,
                (urn, assetType, data) -> new HeadlessTexture(urn, assetType, data, new HeadlessTexture.DisposalAction()), "textures", "fonts");
        assetTypeManager.getAssetFileDataProducer(texture)
                .addAssetFormat(new PNGTextureFormat(Texture.FilterMode.NEAREST,
                        path -> path.getPath().get(1).equals("textures")));
        assetTypeManager.getAssetFileDataProducer(texture)
                .addAssetFormat(new PNGTextureFormat(Texture.FilterMode.LINEAR, path -> path.getPath().get(1).equals(
                        "fonts")));
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
