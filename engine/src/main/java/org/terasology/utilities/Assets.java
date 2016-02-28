/*
 * Copyright 2016 MovingBlocks
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

package org.terasology.utilities;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Preconditions;
import org.terasology.assets.Asset;
import org.terasology.assets.AssetData;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.audio.StaticSound;
import org.terasology.audio.StreamingSound;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.module.sandbox.API;
import org.terasology.naming.Name;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegionAsset;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.skin.UISkin;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Provides a collection of static methods for obtaining assets and registering procedural assets.
 */
@API
public final class Assets {

    // Private constructor to prevent instantiation (static class)
    private Assets() {
    }

    /**
     * @param type
     * @return An set containing the urns of resources belonging to the givan asset type
     */
    public static Set<ResourceUrn> list(Class<? extends Asset<?>> type) {
        return CoreRegistry.get(AssetManager.class).getAvailableAssets(type);
    }

    /**
     * @return An iterable over the list of available modules
     */
    public static Iterable<Name> listModules() {
        return CoreRegistry.get(ModuleManager.class).getEnvironment().getModuleIdsOrderedByDependencies();
    }

    /**
     * @param urn
     * @return The requested asset, or null if it doesn't exist.
     */
    public static <T extends Asset<U>, U extends AssetData> Optional<T> get(ResourceUrn urn, Class<T> type) {
        return CoreRegistry.get(AssetManager.class).getAsset(urn, type);
    }

    public static <T extends Asset<U>, U extends AssetData> Optional<T> get(String urn, Class<T> type) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(urn));
        return CoreRegistry.get(AssetManager.class).getAsset(urn, type);
    }

    public static Set<ResourceUrn> resolveAssetUri(String name, Class<? extends Asset<?>> type) {
        return CoreRegistry.get(AssetManager.class).resolve(name, type);
    }

    /**
     * @param urn The two-part uri for asset ("module:assetName")
     * @return The requested texture, or null if it doesn't exist
     */
    public static Optional<Texture> getTexture(String urn) {
        return get(urn, Texture.class);
    }

    /**
     * @param urn The two-part uri for asset ("module:assetName")
     * @return The requested texture, or null if it doesn't exist
     */
    public static Optional<Texture> getTexture(ResourceUrn urn) {
        return get(urn, Texture.class);
    }

    //
//    /**
//     * @param module
//     * @param assetName
//     * @return The requested texture, or null if it doesn't exist
//     */
//    public static Texture getTexture(String module, String assetName) {
//        return get(new AssetUri(AssetType.TEXTURE, module, assetName), Texture.class);
//    }
//
//    /**
//     * @param module
//     * @param assetName
//     * @return The requested texture, or null if it doesn't exist
//     */
//    public static Texture getTexture(Name module, String assetName) {
//        return get(new AssetUri(AssetType.TEXTURE, module, assetName), Texture.class);
//    }
//
//    public static Subtexture getSubtexture(String simpleUri) {
//        return get(AssetType.SUBTEXTURE, simpleUri, Subtexture.class);
//    }
//
//    public static Subtexture getSubtexture(String module, String assetName) {
//        return get(new AssetUri(AssetType.SUBTEXTURE, module, assetName), Subtexture.class);
//    }
//

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested shader, or null if it doesn't exist
     */
    public static Optional<Shader> getShader(String simpleUri) {
        return get(simpleUri, Shader.class);
    }
//
//    /**
//     * @param module
//     * @param assetName
//     * @return The requested shader, or null if it doesn't exist
//     */
//    public static Shader getShader(String module, String assetName) {
//        return get(new AssetUri(AssetType.SHADER, module, assetName), Shader.class);
//    }
//

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested font, or null if it doesn't exist
     */
    public static Optional<Font> getFont(String simpleUri) {
        return get(simpleUri, Font.class);
    }
//
//    /**
//     * @param module
//     * @param assetName
//     * @return The requested font, or null if it doesn't exist
//     */
//    public static Font getFont(String module, String assetName) {
//        return get(new AssetUri(AssetType.FONT, module, assetName), Font.class);
//    }
//

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested sound, or null if it doesn't exist
     */
    public static Optional<StaticSound> getSound(String simpleUri) {
        return get(simpleUri, StaticSound.class);
    }
//
//    /**
//     * @param module
//     * @param assetName
//     * @return The requested sound, or null if it doesn't exist
//     */
//    public static StaticSound getSound(String module, String assetName) {
//        return get(new AssetUri(AssetType.SOUND, module, assetName), StaticSound.class);
//    }
//

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested music, or null if it doesn't exist
     */
    public static Optional<StreamingSound> getMusic(String simpleUri) {
        return get(simpleUri, StreamingSound.class);
    }
//
//    /**
//     * @param module
//     * @param assetName
//     * @return The requested music, or null if it doesn't exist
//     */
//    public static StreamingSound getMusic(String module, String assetName) {
//        return get(new AssetUri(AssetType.MUSIC, module, assetName), StreamingSound.class);
//    }
//

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested mesh, or null if it doesn't exist
     */
    public static Optional<Mesh> getMesh(String simpleUri) {
        return get(simpleUri, Mesh.class);
    }
//
//    /**
//     * @param module
//     * @param assetName
//     * @return The requested mesh, or null if it doesn't exist
//     */
//    public static Mesh getMesh(String module, String assetName) {
//        return get(new AssetUri(AssetType.MESH, module, assetName), Mesh.class);
//    }
//

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested material, or null if it doesn't exist
     */
    public static Optional<Material> getMaterial(String simpleUri) {
        return get(simpleUri, Material.class);
    }

    //
//    /**
//     * @param module
//     * @param assetName
//     * @return The requested material, or null if it doesn't exist
//     */
//    public static Material getMaterial(String module, String assetName) {
//        return get(new AssetUri(AssetType.MATERIAL, module, assetName), Material.class);
//    }
//
//    /**
//     * @param simpleUri The two-part uri for asset ("module:assetName")
//     * @return The requested SkeletalMesh, or null if it doesn't exist
//     */
//    public static SkeletalMesh getSkeletalMesh(String simpleUri) {
//        return get(AssetType.SKELETON_MESH, simpleUri, SkeletalMesh.class);
//    }
//
//    /**
//     * @param module
//     * @param assetName
//     * @return The requested SkeletalMesh, or null if it doesn't exist
//     */
//    public static SkeletalMesh getSkeletalMesh(String module, String assetName) {
//        return get(new AssetUri(AssetType.SKELETON_MESH, module, assetName), SkeletalMesh.class);
//    }
//
//    /**
//     * @param simpleUri The two-part uri for asset ("module:assetName")
//     * @return The requested MeshAnimation, or null if it doesn't exist
//     */
//    public static MeshAnimation getAnimation(String simpleUri) {
//        return get(AssetType.ANIMATION, simpleUri, MeshAnimation.class);
//    }
//
//    /**
//     * @param module
//     * @param assetName
//     * @return The requested MeshAnimation, or null if it doesn't exist
//     */
//    public static MeshAnimation getAnimation(String module, String assetName) {
//        return get(new AssetUri(AssetType.ANIMATION, module, assetName), MeshAnimation.class);
//    }
//
    public static Optional<Prefab> getPrefab(String simpleUri) {
        return get(simpleUri, Prefab.class);
    }

    //
//    public static BehaviorTree getBehaviorTree(String simpleUri) {
//        return get(AssetType.BEHAVIOR, simpleUri, BehaviorTree.class);
//    }
//
    public static Optional<UISkin> getSkin(String uri) {
        return get(uri, UISkin.class);
    }


    public static Optional<UIElement> getUIElement(String uri) {
        return get(uri, UIElement.class);
    }

    public static <T extends Asset<U>, U extends AssetData> T generateAsset(ResourceUrn urn, U data, Class<T> assetClass) {
        return CoreRegistry.get(AssetManager.class).loadAsset(urn, data, assetClass);
    }

    public static <T extends Asset<U>, U extends AssetData> T generateAsset(U data, Class<T> assetClass) {
        ResourceUrn urn = new ResourceUrn("temp", UUID.randomUUID().toString());
        return CoreRegistry.get(AssetManager.class).loadAsset(urn, data, assetClass);
    }

    //
//    public static void dispose(Asset<?> asset) {
//        CoreRegistry.get(AssetManager.class).dispose(asset);
//    }
//
    public static Optional<TextureRegionAsset> getTextureRegion(String simpleUri) {
        if (simpleUri.isEmpty()) {
            return Optional.empty();
        }
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        return assetManager.getAsset(simpleUri, TextureRegionAsset.class);
    }
}
