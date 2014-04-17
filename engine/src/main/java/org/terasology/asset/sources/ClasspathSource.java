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

import org.terasology.asset.AssetSource;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Collection;
import java.util.List;

/**
 * @author Immortius
 */
public class ClasspathSource implements AssetSource {

    private AssetSource source;

    public ClasspathSource(String id, URL sourceUrl, String baseAssetsPath, String baseOverridesPath, String baseDeltasPath) {
        try {
            Path codePath = Paths.get(sourceUrl.toURI());
            if (Files.isRegularFile(codePath)) {
                source = new ArchiveSource(id, codePath.toFile(), baseAssetsPath, baseOverridesPath, baseDeltasPath);
            } else {
                source = new DirectorySource(id, codePath.resolve(baseAssetsPath), codePath.resolve(baseOverridesPath), codePath.resolve(baseDeltasPath));
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Error loading assets: " + e.getMessage(), e);
        }
    }

    public ClasspathSource(String id, CodeSource cs, String baseAssetsPath, String baseOverridesPath, String baseDeltasPath) {
        if (cs == null) {
            throw new IllegalStateException("Can't access assets: CodeSource is null");
        }

        URL url = cs.getLocation();

        try {
            Path codePath = Paths.get(url.toURI());
            if (Files.isRegularFile(codePath)) {
                source = new ArchiveSource(id, codePath.toFile(), baseAssetsPath, baseOverridesPath, baseDeltasPath);
            } else {
                source = new DirectorySource(id, codePath.resolve(baseAssetsPath), codePath.resolve(baseOverridesPath), codePath.resolve(baseDeltasPath));
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Error loading assets: " + e.getMessage(), e);
        }
    }

    @Override
    public String getSourceId() {
        return source.getSourceId();
    }

    @Override
    public List<URL> get(AssetUri uri) {
        return source.get(uri);
    }

    @Override
    public Iterable<AssetUri> list() {
        return source.list();
    }

    @Override
    public Iterable<AssetUri> list(AssetType type) {
        return source.list(type);
    }

    @Override
    public List<URL> getOverride(AssetUri uri) {
        return source.getOverride(uri);
    }

    @Override
    public Iterable<AssetUri> listOverrides() {
        return source.listOverrides();
    }

    @Override
    public Collection<URL> getDelta(AssetUri uri) {
        return source.getDelta(uri);
    }
}
