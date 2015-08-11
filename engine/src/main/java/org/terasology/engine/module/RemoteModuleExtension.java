/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.engine.module;

import java.net.URL;
import java.util.Date;

import org.terasology.module.ModuleMetadata;

/**
 * A set of module extensions for remote modules.
 * NOTE: this is copy&paste from meta-server.
 */
public enum RemoteModuleExtension {

    ARTIFACT_SIZE("artifactSize", long.class),
    LAST_UPDATED("lastUpdated", Date.class),
    DOWNLOAD_URL("downloadUri", URL.class);

    private final String key;
    private final Class<?> valueType;

    private RemoteModuleExtension(String key, Class<?> valueType) {
        this.key = key;
        this.valueType = valueType;
    }

    public String getKey() {
        return key;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public static URL getDownloadUrl(ModuleMetadata meta) {
        return meta.getExtension(DOWNLOAD_URL.getKey(), URL.class);
    }

    public static void setDownloadUrl(ModuleMetadata meta, URL url) {
        meta.setExtension(DOWNLOAD_URL.getKey(), url);
    }

    public static Date getLastUpdated(ModuleMetadata meta) {
        return meta.getExtension(LAST_UPDATED.getKey(), Date.class);
    }

    public static void setLastUpdated(ModuleMetadata meta, Date date) {
        meta.setExtension(LAST_UPDATED.getKey(), date);
    }

    public static long getArtifactSize(ModuleMetadata meta) {
        return meta.getExtension(ARTIFACT_SIZE.getKey(), Long.class);
    }

    public static void setArtifactSize(ModuleMetadata meta, long size) {
        meta.setExtension(ARTIFACT_SIZE.getKey(), size);
    }
}
