// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetData;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.StreamingSound;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.context.annotation.API;
import org.terasology.gestalt.naming.Name;
import org.terasology.nui.asset.UIElement;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.assets.font.Font;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.shader.Shader;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.nui.skin.UISkin;
import org.terasology.nui.skin.UISkinAsset;

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

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested shader, or null if it doesn't exist
     */
    public static Optional<Shader> getShader(String simpleUri) {
        return get(simpleUri, Shader.class);
    }

    /**
     * @param urn The two-part uri for asset ("module:assetName")
     * @return The requested shader, or null if it doesn't exist
     */
    public static Optional<Shader> getShader(ResourceUrn urn) {
        return get(urn, Shader.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested font, or null if it doesn't exist
     */
    public static Optional<Font> getFont(String simpleUri) {
        return get(simpleUri, Font.class);
    }
    /**
     * @param urn The two-part uri for asset ("module:assetName")
     * @return The requested font, or null if it doesn't exist
     */
    public static Optional<Font> getFont(ResourceUrn urn) {
        return get(urn, Font.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested sound, or null if it doesn't exist
     */
    public static Optional<StaticSound> getSound(String simpleUri) {
        return get(simpleUri, StaticSound.class);
    }
    /**
     * @param urn The two-part uri for asset ("module:assetName")
     * @return The requested sound, or null if it doesn't exist
     */
    public static Optional<StaticSound> getSound(ResourceUrn urn) {
        return get(urn, StaticSound.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested music, or null if it doesn't exist
     */
    public static Optional<StreamingSound> getMusic(String simpleUri) {
        return get(simpleUri, StreamingSound.class);
    }
    /**
     * @param urn The two-part uri for asset ("module:assetName")
     * @return The requested music, or null if it doesn't exist
     */
    public static Optional<StreamingSound> getMusic(ResourceUrn urn) {
        return get(urn, StreamingSound.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested mesh, or null if it doesn't exist
     */
    public static Optional<Mesh> getMesh(String simpleUri) {
        return get(simpleUri, Mesh.class);
    }
    /**
     * @param urn The two-part uri for asset ("module:assetName")
     * @return The requested mesh, or null if it doesn't exist
     */
    public static Optional<Mesh> getMesh(ResourceUrn urn) {
        return get(urn, Mesh.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested material, or null if it doesn't exist
     */
    public static Optional<Material> getMaterial(String simpleUri) {
        return get(simpleUri, Material.class);
    }
    /**
     * @param urn The two-part uri for asset ("module:assetName")
     * @return The requested material, or null if it doesn't exist
     */
    public static Optional<Material> getMaterial(ResourceUrn urn) {
        return get(urn, Material.class);
    }

    public static Optional<Prefab> getPrefab(String simpleUri) {
        return get(simpleUri, Prefab.class);
    }

    public static Optional<Prefab> getPrefab(ResourceUrn urn) {
        return get(urn, Prefab.class);
    }

    public static Optional<UISkin> getSkin(String uri) {
        return get(uri, UISkinAsset.class).map(UISkinAsset::getSkin);
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

    public static Optional<TextureRegionAsset> getTextureRegion(String simpleUri) {
        return CoreRegistry.get(AssetManager.class).getAsset(simpleUri, TextureRegionAsset.class);
    }
}
