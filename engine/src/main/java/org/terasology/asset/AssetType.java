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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.terasology.audio.loaders.OggSoundLoader;
import org.terasology.audio.loaders.OggStreamingSoundLoader;
import org.terasology.entitySystem.prefab.internal.PrefabLoader;
import org.terasology.logic.behavior.asset.BehaviorTreeLoader;
import org.terasology.rendering.assets.atlas.AtlasLoader;
import org.terasology.rendering.assets.font.FontLoader;
import org.terasology.rendering.assets.material.MaterialLoader;
import org.terasology.rendering.assets.mesh.ColladaMeshLoader;
import org.terasology.rendering.assets.mesh.ObjMeshLoader;
import org.terasology.rendering.assets.shader.GLSLShaderLoader;
import org.terasology.rendering.assets.texture.PNGTextureLoader;
import org.terasology.rendering.md5.ColladaSkeletalMeshLoader;
import org.terasology.rendering.md5.MD5AnimationLoader;
import org.terasology.rendering.md5.MD5SkeletonLoader;
import org.terasology.rendering.nui.asset.UILoader;
import org.terasology.rendering.nui.skin.UISkinLoader;
import org.terasology.world.block.loader.TileLoader;
import org.terasology.world.block.shapes.JsonBlockShapeLoader;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * An AssetType defines a type of resource accessible through the AssetManager.
 *
 * @author Immortius
 * @author Lucas Jenss <public@x3ro.de>
 */
public enum AssetType {
    PREFAB("prefab", "prefabs", "prefab", new PrefabLoader(), true),
    SOUND("sound", "sounds", "ogg", new OggSoundLoader(), false),
    MUSIC("music", "music", "ogg", new OggStreamingSoundLoader(), false),
    SHAPE("shape", "shapes", "shape", new JsonBlockShapeLoader(), false),
    MESH("mesh", "mesh", new String[]{"obj", "dae"}, new AssetLoader[]{new ObjMeshLoader(), new ColladaMeshLoader()}, false),
    TEXTURE("texture", new String[]{"textures", "fonts"}, new String[]{"png", "texinfo"}, new PNGTextureLoader(), false),
    SHADER("shader", "shaders", new String[]{"glsl", "info"}, new GLSLShaderLoader(), false) {
        @Override
        public AssetUri getUri(String sourceId, String item) {
            if (item.endsWith("_frag")) {
                String itemPart = item.substring(0, item.length() - "_frag".length());
                return new AssetUri(this, sourceId, itemPart);
            } else if (item.endsWith("_vert")) {
                String itemPart = item.substring(0, item.length() - "_vert".length());
                return new AssetUri(this, sourceId, itemPart);
            }
            return new AssetUri(this, sourceId, item);
        }
    },
    MATERIAL("material", "materials", "mat", new MaterialLoader(), false),
    BLOCK_DEFINITION("blockdef", "blocks", "block", null, false),
    BLOCK_TILE("blocktile", "blockTiles", "png", new TileLoader(), false),
    SKELETON_MESH("skeletalmesh", "skeletalMesh", new String[]{"md5mesh", "dae"}, new AssetLoader[]{new MD5SkeletonLoader(), new ColladaSkeletalMeshLoader()}, false),
    ANIMATION("animation", "animations", "md5anim", new MD5AnimationLoader(), false),
    FONT("font", "fonts", "fnt", new FontLoader(), false),
    SUBTEXTURE("subtexture", new String[]{}, "", null, false),
    ATLAS("atlas", "atlas", "atlas", new AtlasLoader(), false),
    UI_SKIN("skin", "skins", "skin", new UISkinLoader(), false),
    BEHAVIOR("behavior", "behaviors", "behavior", new BehaviorTreeLoader(), false),
    UI_ELEMENT("ui", "ui", "ui", new UILoader(), false);


    private static Map<String, AssetType> typeIdLookup;
    private static Table<String, String, AssetType> subDirLookup;

    private String typeId;

    /**
     * The sub-directory from which assets of this type can be loaded from.
     */
    private List<String> subDirs;

    /**
     * The file extension for assets of this type.
     */
    private List<String> fileExtensions;

    /**
     * Does this asset type support deltas.
     */
    private boolean deltaSupported;

    /**
     * An instance of the asset loaders for the current asset type.
     */
    private List<AssetLoader<?>> assetLoaderList;

    static {
        typeIdLookup = Maps.newHashMap();
        subDirLookup = HashBasedTable.create();
        for (AssetType type : AssetType.values()) {
            typeIdLookup.put(type.getTypeId(), type);
            for (String dir : type.getSubDirs()) {
                for (String extension : type.getFileExtension()) {
                    subDirLookup.put(dir, extension, type);
                }
            }
        }
    }

    private AssetType(String typeId, String subDir, String fileExtension, AssetLoader<?> assetLoader, boolean supportsDelta) {
        this(typeId,
                new String[]{subDir},
                new String[]{fileExtension},
                assetLoader,
                supportsDelta);
    }

    private AssetType(String typeId, String[] subDirs, String[] fileExtensions, AssetLoader<?> assetLoader, boolean supportsDelta) {
        this(typeId, subDirs, fileExtensions, (assetLoader == null) ? new AssetLoader[0] : new AssetLoader[]{assetLoader}, supportsDelta);
    }

    private AssetType(String typeId, String subDir, String[] fileExtensions, AssetLoader<?> assetLoader, boolean supportsDelta) {
        this(typeId, new String[]{subDir}, fileExtensions, assetLoader, supportsDelta);
    }

    private AssetType(String typeId, String[] subDirs, String fileExtension, AssetLoader<?> assetLoader, boolean supportsDelta) {
        this(typeId, subDirs, new String[]{fileExtension}, assetLoader, supportsDelta);
    }

    /**
     * We're going to assume that there's exactly one file extension per asset loader specified in the case of multiple asset loaders
     */
    private AssetType(String typeId, String subDir, String[] fileExtensions, AssetLoader<?>[] assetLoaders, boolean supportsDelta) {
        this(typeId, new String[]{subDir}, fileExtensions, assetLoaders, supportsDelta);
    }

    private AssetType(String typeId, String[] subDir, String[] fileExtensions, AssetLoader<?>[] assetLoaders, boolean supportsDelta) {
        this.typeId = typeId;
        this.subDirs = ImmutableList.copyOf(subDir);
        this.fileExtensions = ImmutableList.copyOf(fileExtensions);
        this.assetLoaderList = ImmutableList.copyOf(assetLoaders);
        this.deltaSupported = supportsDelta;
    }

    public String getTypeId() {
        return typeId;
    }

    public Collection<String> getSubDirs() {
        return subDirs;
    }

    public Collection<String> getFileExtension() {
        return fileExtensions;
    }

    public Collection<AssetLoader<?>> getAssetLoaders() {
        return assetLoaderList;
    }

    /**
     * Returns an instance of the asset loader class assigned to this asset type.
     */
    public AssetLoader getAssetLoader(String fileExtension) {
        if (1 == assetLoaderList.size()) {
            return assetLoaderList.get(0);
        }

        int index = fileExtensions.indexOf(fileExtension);
        if (-1 == index) {
            throw new RuntimeException("Log an error here and maybe pick first assetLoader when I clean this up");
        }
        return assetLoaderList.get(index);
    }

    public AssetUri getUri(String sourceId, String item) {
        return new AssetUri(this, sourceId, item);
    }

    public boolean isDeltaSupported() {
        return deltaSupported;
    }

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
    public static void registerAssetTypes(AssetManager assetManager) {
        for (AssetType type : AssetType.values()) {
            Collection<AssetLoader<?>> loaders = type.getAssetLoaders();
            if ((loaders == null) || (loaders.isEmpty())) {
                continue; // No loader has been assigned to this AssetType
            }

            for (String extension : type.getFileExtension()) {
                AssetLoader<?> loader = type.getAssetLoader(extension);
                if (loader != null) {
                    // A loader has been assigned to this AssetType's file extension
                    assetManager.register(type, extension, loader);
                }
            }
        }
    }
}
