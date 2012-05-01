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

import com.google.common.io.Files;
import org.terasology.asset.AssetSource;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Immortius
 */
public class DirectorySource extends AbstractSource {

    private Logger logger = Logger.getLogger(getClass().getName());

    public DirectorySource(String id, File rootDirectory) {
        super(id);

        assert rootDirectory.isDirectory();

        try {
            loadAssetsFrom(rootDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Error loading assets: " + e.getMessage(), e);
        }
    }

    private void loadAssetsFrom(File directory) throws IOException {
        clear();
        scanFiles(directory, directory.getAbsolutePath());
    }

    private void scanFiles(File file, String basePath) {
        for (File child : file.listFiles()) {
            if (child.isDirectory()) {
                this.scanFiles(child, basePath);
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
                            logger.warning("Failed to load asset " + key + " - " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
