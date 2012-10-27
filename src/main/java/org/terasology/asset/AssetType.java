/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.terasology.asset.loaders.OggSoundLoader;
import org.terasology.asset.loaders.OggStreamingSoundLoader;
import org.terasology.rendering.assetLoaders.FontLoader;
import org.terasology.rendering.assetLoaders.GLSLShaderLoader;
import org.terasology.rendering.assetLoaders.MaterialLoader;
import org.terasology.rendering.assetLoaders.ObjMeshLoader;
import org.terasology.rendering.assetLoaders.PNGTextureLoader;
import org.terasology.rendering.assetLoaders.md5.MD5AnimationLoader;
import org.terasology.rendering.assetLoaders.md5.MD5SkeletonLoader;
import org.terasology.world.block.loader.TileLoader;
import org.terasology.world.block.shapes.JsonBlockShapeLoader;

import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


/**
 * An AssetType defines a type of resource accessible through the AssetManager.
 *
 * @author Immortius
 * @author Lucas Jenss <public@x3ro.de>
 */
public enum AssetType {
    PREFAB("prefab", "prefabs", "prefab", null),
    SOUND("sound", "sounds", "ogg", new OggSoundLoader()),
    MUSIC("music", "music", "ogg", new OggStreamingSoundLoader()),
    SHAPE("shape", "shapes", "json", new JsonBlockShapeLoader()),
    MESH("mesh", "mesh", "obj", new ObjMeshLoader()),
    TEXTURE("texture", new String[] {"textures", "fonts"}, "png", new PNGTextureLoader()),
    SHADER("shader", "shaders", "glsl", new GLSLShaderLoader()) {
        @Override
        public AssetUri getUri(String sourceId, String item) {
            if (item.endsWith("_frag")) {
                item = item.substring(0, item.length() - "_frag".length());
            } else if (item.endsWith("_vert")) {
                item = item.substring(0, item.length() - "_vert".length());
            }
            return new AssetUri(this, sourceId, item);
        }
    },
    MATERIAL("material", "materials", "mat", new MaterialLoader()),
    BLOCK_DEFINITION("blockdef", "blocks", "json", null),
    BLOCK_TILE("blocktile", "blockTiles", "png", new TileLoader()),
    SKELETON_MESH("skeletalmesh", "skeletalMesh", "md5mesh", new MD5SkeletonLoader()),
    ANIMATION("animation", "animations", "md5anim", new MD5AnimationLoader()),
    FONT("font", "fonts", "fnt", new FontLoader());


    /* ============
     * Class fields
     * ============ */

    private String typeId;

    /**
     * The sub-directory from which assets of this type can be loaded from.
     */
    private List<String> subDirs = Lists.newArrayList();

    /**
     * The file extension for assets of this type.
     */
    private String fileExtension;

    private static Map<String, AssetType> typeIdLookup;
    private static Table<String, String, AssetType> subDirLookup;

    /**
     * An instance of the asset loader for the current asset type.
     */
    private AssetLoader assetLoader;

    static {
        typeIdLookup = Maps.newHashMap();
        subDirLookup = HashBasedTable.create();
        for (AssetType type : AssetType.values()) {
            typeIdLookup.put(type.getTypeId(), type);
            for (String dir : type.getSubDirs()) {
                subDirLookup.put(dir, type.fileExtension, type);
            }
        }
    }


    /* ==========
     * Public API
     * ========== */

    private AssetType(String typeId, String subDir, String fileExtension, AssetLoader assetLoader) {
        this.typeId = typeId;
        this.subDirs.add(subDir);
        this.fileExtension = fileExtension;
        this.assetLoader = assetLoader;
    }

    private AssetType(String typeId, String[] subDirs, String fileExtension, AssetLoader assetLoader) {
        this.typeId = typeId;
        this.subDirs.addAll(Arrays.asList(subDirs));
        this.fileExtension = fileExtension;
        this.assetLoader = assetLoader;
    }

    public String getTypeId() {
        return typeId;
    }

    public Collection<String> getSubDirs() {
        return subDirs;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * Returns an instance of the asset loader class assigned to this asset type.
     */
    public AssetLoader getAssetLoader() {
        return assetLoader;
    }

    public AssetUri getUri(String sourceId, String item) {
        return new AssetUri(this, sourceId, item);
    }


    /* ==========
     * Static API
     * ========== */

    public static AssetType getTypeForId(String id) {
        return typeIdLookup.get(id);
    }

    public static AssetType getTypeFor(String dir, String extension) {
        return subDirLookup.get(dir, extension);
    }

    /**
     * Registers all asset types with the AssetManager if they have an AssetLoader
     * class associated with them.
     */
    public static void registerAssetTypes() {
        for (AssetType type : AssetType.values()) {
            AssetLoader loader = type.getAssetLoader();
            if (loader == null) {
                continue; // No loader has been assigned to this AssetType
            }

            AssetManager.getInstance().register(
                    type,
                    type.getFileExtension(),
                    loader
            );
        }
    }
}
