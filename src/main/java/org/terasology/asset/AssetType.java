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

import com.google.common.collect.Maps;
import org.terasology.asset.loaders.*;
import org.terasology.world.block.loader.TileLoader;
import org.terasology.world.block.shapes.JsonBlockShapeLoader;

import java.util.Map;
import java.util.logging.Logger;



/**
 * An AssetType defines a type of resource accessible through the AssetManager.
 *
 * @author Immortius
 * @author Lucas Jenss <public@x3ro.de>
 */
public enum AssetType {
    PREFAB("prefab", "prefabs", null, null),
    SOUND("sound", "sounds", "ogg", new OggSoundLoader()),
    MUSIC("music", "music", "ogg", new OggStreamingSoundLoader()),
    SHAPE("shape", "shapes", "json", new JsonBlockShapeLoader()),
    MESH("mesh", "mesh", "obj", new ObjMeshLoader()),
    TEXTURE("texture", "textures", "png", new PNGTextureLoader()),
    SHADER("shader", "shaders", "glsl", new GLSLShaderLoader()) {
        @Override
        public AssetUri getUri(String sourceId, String item) {
            int index = item.lastIndexOf("_frag.glsl");
            if (index == -1) {
                index = item.lastIndexOf("_vert.glsl");
            }
            if (index == -1) {
                index = item.lastIndexOf(".");
            }
            if (index != -1) {
                return new AssetUri(this, sourceId, item.substring(0, index));
            }
            return null;
        }
    },
    MATERIAL("material", "materials", "mat", new MaterialLoader()),
    BLOCK_DEFINITION("blockdef", "blocks", null, null),
    BLOCK_TILE("blocktile", "blockTiles", "png", new TileLoader());



    /* ============
     * Class fields
     * ============ */

    private String typeId;

    /**
     * The sub-directory from which assets of this type can be loaded from.
     */
    private String subDir;

    /**
     * The file extension for assets of this type.
     */
    private String fileExtension;

    private static Map<String, AssetType> typeIdLookup;
    private static Map<String, AssetType> subDirLookup;

    /**
     * An instance of the asset loader for the current asset type.
     */
    private AssetLoader assetLoader;

    private Logger logger = Logger.getLogger(getClass().getName());

    static {
        typeIdLookup = Maps.newHashMap();
        subDirLookup = Maps.newHashMap();
        for (AssetType type : AssetType.values()) {
            typeIdLookup.put(type.getTypeId(), type);
            subDirLookup.put(type.getSubDir(), type);
        }

    }



    /* ==========
     * Public API
     * ========== */

    private AssetType(String typeId, String subDir, String fileExtension, AssetLoader assetLoader) {
        this.typeId = typeId;
        this.subDir = subDir;
        this.fileExtension = fileExtension;
        this.assetLoader = assetLoader;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getSubDir() {
        return subDir;
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
        if (item.contains(".")) {
            return new AssetUri(this, sourceId, item.substring(0, item.lastIndexOf('.')));
        }
        return null;
    }



    /* ==========
     * Static API
     * ========== */

    public static AssetType getTypeForId(String id) {
        return typeIdLookup.get(id);
    }

    public static AssetType getTypeForSubDir(String dir) {
        return subDirLookup.get(dir);
    }

    /**
     * Registers all asset types with the AssetManager if they have an AssetLoader
     * class associated with them.
     */
    public static void registerAssetTypes() {
        for(AssetType type : AssetType.values()) {
            AssetLoader loader = type.getAssetLoader();
            if(loader == null) continue; // No loader has been assigned to this AssetType

            AssetManager.getInstance().register(
                    type,
                    type.getFileExtension(),
                    loader
            );
        }
    }
}
