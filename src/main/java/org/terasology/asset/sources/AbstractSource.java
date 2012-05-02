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

package org.terasology.asset.sources;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.terasology.asset.AssetSource;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Immortius
 */
public abstract class AbstractSource implements AssetSource {
    private Logger logger = Logger.getLogger(getClass().getName());
    private String sourceId;
    private Map<AssetUri, URL> assets = Maps.newHashMap();
    private Multimap<AssetType, AssetUri> assetsByType = HashMultimap.create();

    public AbstractSource(String id) {
        this.sourceId = id;
    }

    @Override
    public String getSourceId() {
        return sourceId;
    }

    @Override
    public URL get(AssetUri uri) {
        return assets.get(uri);
    }

    @Override
    public Iterable<AssetUri> list() {
        return assets.keySet();
    }

    @Override
    public Iterable<AssetUri> list(AssetType type) {
        return assetsByType.get(type);
    }

    protected void clear() {
        assets.clear();
        assetsByType.clear();
    }

    protected void addItem(AssetUri uri, URL url) {
        assets.put(uri, url);
        assetsByType.put(uri.getAssetType(), uri);
    }

    protected AssetUri getUri(String relativePath) {
        String[] parts = relativePath.split("/", 2);
        if (parts.length > 1) {
            AssetType assetType = AssetType.getTypeForSubDir(parts[0]);
            if (assetType != null) {
                if (parts[1].contains(".")) {
                    parts[1] = parts[1].substring(0, parts[1].lastIndexOf('.'));
                }
                return new AssetUri(assetType, sourceId, parts[1]);
            }
        }
        return null;
    }

}
