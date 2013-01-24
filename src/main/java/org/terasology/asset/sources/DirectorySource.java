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

package org.terasology.asset.sources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetUri;

import java.io.File;
import java.net.MalformedURLException;

/**
 * @author Immortius
 */
public class DirectorySource extends AbstractSource {

    private static final Logger logger = LoggerFactory.getLogger(DirectorySource.class);

    public DirectorySource(String id, File rootAssetsDirectory, File rootOverridesDirectory) {
        super(id);

        assert rootAssetsDirectory.isDirectory();
        assert rootOverridesDirectory.isDirectory();

        clear();
        if (rootAssetsDirectory.exists() && rootAssetsDirectory.isDirectory()) {
            scanAssets(rootAssetsDirectory, rootAssetsDirectory.getAbsolutePath());
        }
        if (rootOverridesDirectory.exists() && rootOverridesDirectory.isDirectory()) {
            scanOverrides(rootOverridesDirectory, rootOverridesDirectory.getAbsolutePath());
        }
    }

    private void scanOverrides(File overrideDirectory, String basePath) {
        for (File child : overrideDirectory.listFiles()) {
            if (child.isDirectory()) {
                scanOverrides(child, basePath);
            } else if (child.isFile()) {
                String key = child.getAbsolutePath();
                if (key.startsWith(basePath)) {
                    key = key.substring(basePath.length() + 1);
                    key = key.replace(File.separatorChar, '/');

                    int separatorIndex = key.indexOf('/');
                    if (separatorIndex == -1) {
                        continue;
                    }
                    String moduleName = key.substring(0, separatorIndex);
                    key = key.substring(separatorIndex + 1);

                    AssetUri uri = getUri(moduleName, key);
                    if (uri != null) {
                        try {
                            addOverride(uri, child.toURI().toURL());
                        } catch (MalformedURLException e) {
                            logger.warn("Failed to load override {}", key, e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void scanAssets(File file, String basePath) {
        for (File child : file.listFiles()) {
            if (child.isDirectory()) {
                scanAssets(child, basePath);
            } else if (child.isFile()) {
                String key = child.getAbsolutePath();

                if (key.startsWith(basePath)) { //strip down basepath
                    key = key.substring(basePath.length() + 1);
                    key = key.replace(File.separatorChar, '/');

                    AssetUri uri = getUri(key);

                    if (uri != null) {
                        try {
                            addItem(uri, child.toURI().toURL());
                        } catch (MalformedURLException e) {
                            logger.warn("Failed to load asset {}", key, e.getMessage());
                        }
                    }
                }
            }
        }

    }
}
