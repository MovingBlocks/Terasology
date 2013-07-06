/*
 * Copyright 2013 Moving Blocks
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Immortius
 */
public class DirectorySource extends AbstractSource {

    private static final Logger logger = LoggerFactory.getLogger(DirectorySource.class);

    public DirectorySource(String id, Path rootAssetsDirectory, Path rootOverridesDirectory) {
        super(id);

        clear();
        if (Files.isDirectory(rootAssetsDirectory)) {
            scanAssets(rootAssetsDirectory, rootAssetsDirectory);
        }
        if (Files.isDirectory(rootOverridesDirectory)) {
            scanOverrides(rootOverridesDirectory, rootOverridesDirectory);
        }
    }

    private void scanOverrides(Path overrideDirectory, Path basePath) {
        try {
            for (Path child : Files.newDirectoryStream(overrideDirectory)) {
                if (Files.isDirectory(child)) {
                    scanOverrides(child, basePath);
                } else if (Files.isRegularFile(child)) {
                    Path relativePath = basePath.relativize(child);
                    Path modulePath = relativePath.subpath(0, 1);
                    AssetUri uri = getUri(modulePath.toString(), modulePath.relativize(child));
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

    private void scanAssets(Path path, Path basePath) {
        try {
            for (Path child : Files.newDirectoryStream(path)) {
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
