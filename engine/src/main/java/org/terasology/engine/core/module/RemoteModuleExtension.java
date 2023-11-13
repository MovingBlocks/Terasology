// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import org.terasology.gestalt.module.ModuleMetadata;

import java.net.URL;
import java.util.Date;
/**
 * A set of module extensions for remote modules.
 * NOTE: this is copy&amp;paste from meta-server.
 */
public enum RemoteModuleExtension {

    ARTIFACT_SIZE("artifactSize", long.class),
    LAST_UPDATED("lastUpdated", Date.class),
    DOWNLOAD_URL("downloadUri", URL.class);

    private final String key;
    private final Class<?> valueType;

     RemoteModuleExtension(String key, Class<?> valueType) {
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

    public static Long getArtifactSize(ModuleMetadata meta) {
        return meta.getExtension(ARTIFACT_SIZE.getKey(), Long.class);
    }

    public static void setArtifactSize(ModuleMetadata meta, long size) {
        meta.setExtension(ARTIFACT_SIZE.getKey(), size);
    }
}
