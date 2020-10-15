// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.headless;

import org.terasology.assets.module.ModuleAwareAssetTypeManager;
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
import org.terasology.nui.canvas.CanvasRenderer;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.animation.MeshAnimationBundle;
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
import org.terasology.rendering.nui.internal.TerasologyCanvasRenderer;

public class HeadlessGraphics implements EngineSubsystem {

    @In
    private ContextAwareClassFactory classFactory;

    @Override
    public String getName() {
        return "Graphics";
    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
        assetTypeManager.registerCoreAssetType(Font.class, FontImpl::new, "fonts");
        assetTypeManager.registerCoreAssetType(Texture.class, HeadlessTexture::new, "textures", "fonts");
        assetTypeManager.registerCoreFormat(Texture.class, new PNGTextureFormat(Texture.FilterMode.NEAREST,
                path -> path.getName(2).toString().equals("textures")));
        assetTypeManager.registerCoreFormat(Texture.class, new PNGTextureFormat(Texture.FilterMode.LINEAR,
                path -> path.getName(2).toString().equals("fonts")));
        assetTypeManager.registerCoreAssetType(Shader.class, HeadlessShader::new, "shaders");
        assetTypeManager.registerCoreAssetType(Material.class, HeadlessMaterial::new, "materials");
        assetTypeManager.registerCoreAssetType(Mesh.class, HeadlessMesh::new, "mesh");
        assetTypeManager.registerCoreAssetType(SkeletalMesh.class, HeadlessSkeletalMesh::new, "skeletalMesh");
        assetTypeManager.registerCoreAssetType(MeshAnimation.class, MeshAnimationImpl::new, "animations",
                "skeletalMesh");
        assetTypeManager.registerCoreAssetType(MeshAnimationBundle.class, MeshAnimationBundle::new, "skeletalMesh",
                "animations");
        assetTypeManager.registerCoreAssetType(Atlas.class, Atlas::new, "atlas");
        assetTypeManager.registerCoreAssetType(Subtexture.class, Subtexture::new);
    }

    @Override
    public void postInitialise(Context context) {
        classFactory.createInjectableInstance(HeadlessRenderingSubsystemFactory.class, RenderingSubsystemFactory.class);
        classFactory.createInjectableInstance(HeadlessDisplayDevice.class, DisplayDevice.class);
        classFactory.createInjectableInstance(ShaderManagerHeadless.class, ShaderManager.class);
        CanvasRenderer renderer = classFactory.createInjectableInstance(HeadlessCanvasRenderer.class,
                CanvasRenderer.class
        );
        context.put(TerasologyCanvasRenderer.class, (TerasologyCanvasRenderer) renderer);
    }

}
