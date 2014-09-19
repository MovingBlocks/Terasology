/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.asset;

import org.terasology.audio.StaticSound;
import org.terasology.audio.StreamingSound;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.naming.Name;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.assets.texture.subtexture.Subtexture;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.skin.UISkin;

import java.util.List;

/**
 * Assets provides a collection of static methods for obtaining assets and registering procedural assets
 *
 * @author Immortius
 */
public final class Assets {

    // Private constructor to prevent instantiation (static class)
    private Assets() {
    }

    /**
     * @return An iterable over the complete list of available assets
     */
    public static Iterable<AssetUri> list() {
        return CoreRegistry.get(AssetManager.class).listAssets();
    }

    /**
     * @param type
     * @return An iterable over the assets belonging to the given AssetType
     */
    public static Iterable<AssetUri> list(AssetType type) {
        return CoreRegistry.get(AssetManager.class).listAssets(type);
    }

    /**
     * @return An iterable over the list of available modules
     */
    public static Iterable<Name> listModules() {
        return CoreRegistry.get(AssetManager.class).listModuleNames();
    }

    /**
     * @param uri
     * @return The requested asset, or null if it doesn't exist.
     */
    public static Asset<?> get(AssetUri uri) {
        return CoreRegistry.get(AssetManager.class).loadAsset(uri);
    }

    public static Asset<?> get(AssetType type, String uri) {
        if (uri != null && !uri.isEmpty()) {
            return CoreRegistry.get(AssetManager.class).resolveAndLoad(type, uri);
        }
        return null;
    }

    public static <T extends Asset<?>> T get(AssetType type, String uri, Class<T> assetClass) {
        if (uri != null && !uri.isEmpty()) {
            return CoreRegistry.get(AssetManager.class).resolveAndLoad(type, uri, assetClass);
        }
        return null;
    }

    /**
     * @param name
     * @return The resolved asset, or
     */
    public static Asset<?> resolve(AssetType type, String name) {
        return CoreRegistry.get(AssetManager.class).resolveAndLoad(type, name);
    }

    public static AssetUri resolveAssetUri(AssetType type, String name) {
        return CoreRegistry.get(AssetManager.class).resolve(type, name);
    }

    /**
     * @param name
     * @return The resolved Asset URIs.
     */
    public static List<AssetUri> resolveAllUri(AssetType type, String name) {
        return CoreRegistry.get(AssetManager.class).resolveAll(type, name);
    }

    /**
     * @param uri
     * @param assetClass The expected class of the asset
     * @param <T>
     * @return The requested asset, or null if it doesn't exist or isn't of the expected class.
     */
    public static <T extends Asset<?>> T get(AssetUri uri, Class<T> assetClass) {
        Asset<?> result = get(uri);
        if (result != null && assetClass.isAssignableFrom(result.getClass())) {
            return assetClass.cast(result);
        }
        return null;
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested texture, or null if it doesn't exist
     */
    public static Texture getTexture(String simpleUri) {
        return get(AssetType.TEXTURE, simpleUri, Texture.class);
    }

    /**
     * @param module
     * @param assetName
     * @return The requested texture, or null if it doesn't exist
     */
    public static Texture getTexture(String module, String assetName) {
        return get(new AssetUri(AssetType.TEXTURE, module, assetName), Texture.class);
    }

    /**
     * @param module
     * @param assetName
     * @return The requested texture, or null if it doesn't exist
     */
    public static Texture getTexture(Name module, String assetName) {
        return get(new AssetUri(AssetType.TEXTURE, module, assetName), Texture.class);
    }

    public static Subtexture getSubtexture(String simpleUri) {
        return get(AssetType.SUBTEXTURE, simpleUri, Subtexture.class);
    }

    public static Subtexture getSubtexture(String module, String assetName) {
        return get(new AssetUri(AssetType.SUBTEXTURE, module, assetName), Subtexture.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested shader, or null if it doesn't exist
     */
    public static Shader getShader(String simpleUri) {
        return get(AssetType.SHADER, simpleUri, Shader.class);
    }

    /**
     * @param module
     * @param assetName
     * @return The requested shader, or null if it doesn't exist
     */
    public static Shader getShader(String module, String assetName) {
        return get(new AssetUri(AssetType.SHADER, module, assetName), Shader.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested font, or null if it doesn't exist
     */
    public static Font getFont(String simpleUri) {
        return get(AssetType.FONT, simpleUri, Font.class);
    }

    /**
     * @param module
     * @param assetName
     * @return The requested font, or null if it doesn't exist
     */
    public static Font getFont(String module, String assetName) {
        return get(new AssetUri(AssetType.FONT, module, assetName), Font.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested sound, or null if it doesn't exist
     */
    public static StaticSound getSound(String simpleUri) {
        return get(AssetType.SOUND, simpleUri, StaticSound.class);
    }

    /**
     * @param module
     * @param assetName
     * @return The requested sound, or null if it doesn't exist
     */
    public static StaticSound getSound(String module, String assetName) {
        return get(new AssetUri(AssetType.SOUND, module, assetName), StaticSound.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested music, or null if it doesn't exist
     */
    public static StreamingSound getMusic(String simpleUri) {
        return get(AssetType.MUSIC, simpleUri, StreamingSound.class);
    }

    /**
     * @param module
     * @param assetName
     * @return The requested music, or null if it doesn't exist
     */
    public static StreamingSound getMusic(String module, String assetName) {
        return get(new AssetUri(AssetType.MUSIC, module, assetName), StreamingSound.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested mesh, or null if it doesn't exist
     */
    public static Mesh getMesh(String simpleUri) {
        return get(AssetType.MESH, simpleUri, Mesh.class);
    }

    /**
     * @param module
     * @param assetName
     * @return The requested mesh, or null if it doesn't exist
     */
    public static Mesh getMesh(String module, String assetName) {
        return get(new AssetUri(AssetType.MESH, module, assetName), Mesh.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested material, or null if it doesn't exist
     */
    public static Material getMaterial(String simpleUri) {
        return get(AssetType.MATERIAL, simpleUri, Material.class);
    }

    /**
     * @param module
     * @param assetName
     * @return The requested material, or null if it doesn't exist
     */
    public static Material getMaterial(String module, String assetName) {
        return get(new AssetUri(AssetType.MATERIAL, module, assetName), Material.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested SkeletalMesh, or null if it doesn't exist
     */
    public static SkeletalMesh getSkeletalMesh(String simpleUri) {
        return get(AssetType.SKELETON_MESH, simpleUri, SkeletalMesh.class);
    }

    /**
     * @param module
     * @param assetName
     * @return The requested SkeletalMesh, or null if it doesn't exist
     */
    public static SkeletalMesh getSkeletalMesh(String module, String assetName) {
        return get(new AssetUri(AssetType.SKELETON_MESH, module, assetName), SkeletalMesh.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested MeshAnimation, or null if it doesn't exist
     */
    public static MeshAnimation getAnimation(String simpleUri) {
        return get(AssetType.ANIMATION, simpleUri, MeshAnimation.class);
    }

    /**
     * @param module
     * @param assetName
     * @return The requested MeshAnimation, or null if it doesn't exist
     */
    public static MeshAnimation getAnimation(String module, String assetName) {
        return get(new AssetUri(AssetType.ANIMATION, module, assetName), MeshAnimation.class);
    }

    public static Prefab getPrefab(String simpleUri) {
        return get(AssetType.PREFAB, simpleUri, Prefab.class);
    }

    public static BehaviorTree getBehaviorTree(String simpleUri) {
        return get(AssetType.BEHAVIOR, simpleUri, BehaviorTree.class);
    }

    public static UISkin getSkin(String uri) {
        return get(AssetType.UI_SKIN, uri, UISkin.class);
    }

    public static UIElement getUIElement(String uri) {
        return get(AssetType.UI_ELEMENT, uri, UIElement.class);
    }

    public static <T extends Asset<U>, U extends AssetData> T generateAsset(AssetUri uri, U data, Class<T> assetClass) {
        Asset<U> asset = CoreRegistry.get(AssetManager.class).generateAsset(uri, data);
        if (assetClass.isInstance(asset)) {
            return assetClass.cast(asset);
        }
        return null;
    }

    public static <T extends Asset<U>, U extends AssetData> T generateAsset(AssetType type, U data, Class<T> assetClass) {
        Asset<U> asset = CoreRegistry.get(AssetManager.class).generateTemporaryAsset(type, data);
        if (assetClass.isInstance(asset)) {
            return assetClass.cast(asset);
        }
        return null;
    }

    public static void dispose(Asset<?> asset) {
        CoreRegistry.get(AssetManager.class).dispose(asset);
    }

    public static TextureRegion getTextureRegion(String simpleUri) {
        if (simpleUri.isEmpty()) {
            return null;
        }
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        AssetUri uri = assetManager.resolve(AssetType.TEXTURE, simpleUri);
        if (uri != null) {
            Texture result = assetManager.tryLoadAsset(uri, Texture.class);
            if (result != null) {
                return result;
            }
        }
        uri = assetManager.resolve(AssetType.SUBTEXTURE, simpleUri);
        if (uri != null) {
            return assetManager.loadAsset(uri, Subtexture.class);
        }
        return null;
    }
}
