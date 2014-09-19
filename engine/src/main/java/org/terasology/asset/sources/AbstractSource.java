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

package org.terasology.asset.sources;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetSource;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.naming.Name;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Immortius
 */
public abstract class AbstractSource implements AssetSource {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSource.class);

    private Name sourceId;
    private SetMultimap<AssetUri, URL> assets = HashMultimap.create();
    private SetMultimap<AssetType, AssetUri> assetsByType = HashMultimap.create();
    private SetMultimap<AssetUri, URL> overrides = HashMultimap.create();
    private SetMultimap<AssetUri, URL> deltas = HashMultimap.create();

    public AbstractSource(Name id) {
        sourceId = id;
    }

    @Override
    public Name getSourceId() {
        return sourceId;
    }

    @Override
    public List<URL> get(AssetUri uri) {
        return Lists.newArrayList(assets.get(uri));
    }

    @Override
    public Iterable<AssetUri> list() {
        return assets.keySet();
    }

    @Override
    public Iterable<AssetUri> list(AssetType type) {
        return assetsByType.get(type);
    }

    @Override
    public List<URL> getOverride(AssetUri uri) {
        return Lists.newArrayList(overrides.get(uri));
    }

    @Override
    public List<URL> getDelta(AssetUri uri) {
        return Lists.newArrayList(deltas.get(uri));
    }

    @Override
    public Iterable<AssetUri> listOverrides() {
        return overrides.keySet();
    }

    protected void clear() {
        assets.clear();
        assetsByType.clear();
    }

    protected void addItem(AssetUri uri, URL url) {
        assets.put(uri, url);
        assetsByType.put(uri.getAssetType(), uri);
    }

    protected void addOverride(AssetUri uri, URL url) {
        logger.debug("Adding override url {} -> {}", uri, url);
        overrides.put(uri, url);
    }

    protected void setDelta(AssetUri uri, URL url) {
        logger.debug("Adding delta url {} -> {}", uri, url);
        deltas.put(uri, url);
    }

    protected AssetUri getUri(Path relativePath) {
        return getUri(sourceId, relativePath);
    }

    protected AssetUri getUri(Name moduleId, Path relativePath) {
        if (relativePath.getNameCount() > 1) {
            Path assetPath = relativePath.subpath(0, 1);
            Path filename = relativePath.getFileName();
            String extension = Files.getFileExtension(filename.toString());
            String nameOnly = filename.toString().substring(0, filename.toString().length() - extension.length() - 1);
            AssetType assetType = AssetType.getTypeFor(assetPath.toString(), extension);
            if (assetType != null) {
                return assetType.getUri(moduleId, nameOnly);
            }
        }
        return null;
    }

    protected AssetUri getUri(String relativePath) {
        return getUri(sourceId, relativePath);
    }

    protected AssetUri getUri(Name moduleId, String relativePath) {
        String[] parts = relativePath.split("/", 2);
        if (parts.length > 1) {
            int lastSepIndex = parts[1].lastIndexOf("/");
            if (lastSepIndex != -1) {
                parts[1] = parts[1].substring(lastSepIndex + 1);
            }
            int extensionSeparator = parts[1].lastIndexOf(".");
            if (extensionSeparator != -1) {
                String name = parts[1].substring(0, extensionSeparator);
                String extension = parts[1].substring(extensionSeparator + 1);
                AssetType assetType = AssetType.getTypeFor(parts[0], extension);
                if (assetType != null) {
                    return assetType.getUri(moduleId, name);
                }
            }
        }
        return null;
    }

}
