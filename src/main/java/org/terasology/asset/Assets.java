package org.terasology.asset;

import org.terasology.audio.Sound;
import org.terasology.rendering.assets.Font;
import org.terasology.rendering.assets.Material;
import org.terasology.rendering.assets.MaterialShader;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.primitives.Mesh;

/**
 * Assets provides a collection of static methods for obtaining assets and registering procedural assets
 *
 * @author Immortius
 */
public final class Assets {

    /**
     * @return An iterable over the complete list of available assets
     */
    public static Iterable<AssetUri> list() {
        return AssetManager.getInstance().listAssets();
    }

    /**
     * @param type
     * @return An iterable over the assets belonging to the given AssetType
     */
    public static Iterable<AssetUri> list(AssetType type) {
        return AssetManager.getInstance().listAssets(type);
    }

    /**
     * @return An iterable over the list of available modules
     */
    public static Iterable<String> listModules() {
        return AssetManager.getInstance().listModuleNames();
    }

    /**
     * @param uri
     * @return The requested asset, or null if it doesn't exist.
     */
    public static Asset get(AssetUri uri) {
        return AssetManager.getInstance().loadAsset(uri);
    }

    /**
     * @param uri
     * @param assetClass The expected class of the asset
     * @param <T>
     * @return The requested asset, or null if it doesn't exist or isn't of the expected class.
     */
    public static <T extends Asset> T get(AssetUri uri, Class<T> assetClass) {
        Asset result = get(uri);
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
        return get(new AssetUri(AssetType.TEXTURE, simpleUri), Texture.class);
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
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested shader, or null if it doesn't exist
     */
    public static MaterialShader getShader(String simpleUri) {
        return get(new AssetUri(AssetType.SHADER, simpleUri), MaterialShader.class);
    }

    /**
     * @param module
     * @param assetName
     * @return The requested shader, or null if it doesn't exist
     */
    public static MaterialShader getShader(String module, String assetName) {
        return get(new AssetUri(AssetType.SHADER, module, assetName), MaterialShader.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested font, or null if it doesn't exist
     */
    public static Font getFont(String simpleUri) {
        return get(new AssetUri(AssetType.FONT, simpleUri), Font.class);
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
    public static Sound getSound(String simpleUri) {
        return get(new AssetUri(AssetType.SOUND, simpleUri), Sound.class);
    }

    /**
     * @param module
     * @param assetName
     * @return The requested sound, or null if it doesn't exist
     */
    public static Sound getSound(String module, String assetName) {
        return get(new AssetUri(AssetType.SOUND, module, assetName), Sound.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested music, or null if it doesn't exist
     */
    public static Sound getMusic(String simpleUri) {
        return get(new AssetUri(AssetType.MUSIC, simpleUri), Sound.class);
    }

    /**
     * @param module
     * @param assetName
     * @return The requested music, or null if it doesn't exist
     */
    public static Sound getMusic(String module, String assetName) {
        return get(new AssetUri(AssetType.MUSIC, module, assetName), Sound.class);
    }

    /**
     * @param simpleUri The two-part uri for asset ("module:assetName")
     * @return The requested mesh, or null if it doesn't exist
     */
    public static Mesh getMesh(String simpleUri) {
        return get(new AssetUri(AssetType.MESH, simpleUri), Mesh.class);
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
        return get(new AssetUri(AssetType.MATERIAL, simpleUri), Material.class);
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
        return get(new AssetUri(AssetType.SKELETON_MESH, simpleUri), SkeletalMesh.class);
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
        return get(new AssetUri(AssetType.ANIMATION, simpleUri), MeshAnimation.class);
    }

    /**
     * @param module
     * @param assetName
     * @return The requested MeshAnimation, or null if it doesn't exist
     */
    public static MeshAnimation getAnimation(String module, String assetName) {
        return get(new AssetUri(AssetType.ANIMATION, module, assetName), MeshAnimation.class);
    }

    // Private constructor to prevent instantiation (static class)
    private Assets() {
    }
}
