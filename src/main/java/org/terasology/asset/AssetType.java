/*
 * Copyright 2012
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

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author Immortius
 */
public enum AssetType {
    PREFAB("prefab", "prefabs"),
    SOUND("sound", "sounds"),
    MUSIC("music", "music"),
    SHAPE("shape", "shapes"),
    MESH("mesh", "mesh"),
    TEXTURE("texture", "textures"),
    SHADER("shader", "shaders") {
        @Override
        public AssetUri getUri(String sourceId, String item) {
            int index = item.lastIndexOf("_frag.glsl");
            if (index == -1) {
                index = item.lastIndexOf("_vert.glsl");
            }
            if (index != -1) {
                return new AssetUri(this, sourceId, item.substring(0, index));
            }
            return null;
        }
    },
    MATERIAL("material", "materials");

    private String typeId;
    private String subDir;

    private static Map<String, AssetType> typeIdLookup;
    private static Map<String, AssetType> subDirLookup;

    static {
        typeIdLookup = Maps.newHashMap();
        subDirLookup = Maps.newHashMap();
        for (AssetType type : AssetType.values()) {
            typeIdLookup.put(type.getTypeId(), type);
            subDirLookup.put(type.getSubDir(), type);
        }

    }

    private AssetType(String typeId, String subDir) {
        this.typeId = typeId;
        this.subDir = subDir;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getSubDir() {
        return subDir;
    }

    public static AssetType getTypeForId(String id) {
        return typeIdLookup.get(id);
    }

    public static AssetType getTypeForSubDir(String dir) {
        return subDirLookup.get(dir);
    }

    public AssetUri getUri(String sourceId, String item) {
        if (item.contains(".")) {
            return new AssetUri(this, sourceId, item.substring(0, item.lastIndexOf('.')));
        }
        return null;
    }

}
