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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetUri;
import org.terasology.naming.Name;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Immortius
 */
public class DirectorySource extends AbstractSource {

    private static final Logger logger = LoggerFactory.getLogger(DirectorySource.class);

    public DirectorySource(Name id, Path rootAssetsDirectory, Path rootOverridesDirectory, Path rootDeltaDirectory) {
        super(id);

        clear();
        if (Files.isDirectory(rootAssetsDirectory)) {
            scanAssets(rootAssetsDirectory, rootAssetsDirectory);
        }
        if (Files.isDirectory(rootOverridesDirectory)) {
            scanOverrides(rootOverridesDirectory, rootOverridesDirectory);
        }
        if (Files.isDirectory(rootDeltaDirectory)) {
            scanDeltas(rootDeltaDirectory, rootDeltaDirectory);
        }
    }

    private void scanOverrides(Path overrideDirectory, Path basePath) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(overrideDirectory)) {
            for (Path child : stream) {
                if (Files.isDirectory(child)) {
                    scanOverrides(child, basePath);
                } else if (Files.isRegularFile(child)) {
                    Path relativePath = basePath.relativize(child);
                    Path modulePath = relativePath.subpath(0, 1);
                    AssetUri uri = getUri(new Name(modulePath.toString()), modulePath.relativize(relativePath));
                    if (uri != null) {
                        try {
                            addOverride(uri, child.toUri().toURL());
                        } catch (MalformedURLException e) {
                            logger.warn("Failed to load override {}", child, e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to scan override path: {}", overrideDirectory, e);
        }

    }

    private void scanDeltas(Path deltaDirectory, Path basePath) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(deltaDirectory)) {
            for (Path child : stream) {
                if (Files.isDirectory(child)) {
                    scanDeltas(child, basePath);
                } else if (Files.isRegularFile(child)) {
                    Path relativePath = basePath.relativize(child);
                    Path modulePath = relativePath.subpath(0, 1);
                    AssetUri uri = getUri(new Name(modulePath.toString()), modulePath.relativize(relativePath));
                    if (uri != null) {
                        try {
                            setDelta(uri, child.toUri().toURL());
                        } catch (MalformedURLException e) {
                            logger.warn("Failed to load delta {}", child, e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to scan delta path: {}", deltaDirectory, e);
        }

    }

    private void scanAssets(Path path, Path basePath) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path child : stream) {
                if (Files.isDirectory(child)) {
                    scanAssets(child, basePath);
                } else if (Files.isRegularFile(child)) {
                    Path relativePath = basePath.relativize(child);
                    AssetUri uri = getUri(relativePath);
                    if (uri != null) {
                        try {
                            addItem(uri, child.toUri().toURL());
                        } catch (MalformedURLException e) {
                            logger.warn("Failed to load asset {}", relativePath, e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to scan assets path: {}", path, e);
        }
    }
}
